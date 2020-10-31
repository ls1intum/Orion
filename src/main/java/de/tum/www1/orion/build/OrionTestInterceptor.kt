package de.tum.www1.orion.build

import com.intellij.execution.Platform
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.execution.testframework.sm.ServiceMessageBuilder
import com.intellij.openapi.project.Project
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.runInEdtAndGet
import de.tum.www1.orion.dto.BuildError
import de.tum.www1.orion.dto.BuildLogFileErrorsDTO
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private data class Task(val name: String, val level: Int, val testResults: MutableList<TestResult> = mutableListOf(),
                        val subtasks: MutableList<Task> = mutableListOf(), val parent: Task? = null)

private data class TestResult(val testName: String, val result: String?, val successful: Boolean) {
    fun report(handler: ProcessHandler?, index: Int, anonymized: Boolean = true, prefix: String? = null) {
        val reportedName = prefix + (if (anonymized) "Test #${index}" else testName)

        handler?.report(ServiceMessageBuilder.testStarted(reportedName))
        if (!successful) {
            handler?.report(ServiceMessageBuilder.testFailed(reportedName).addAttribute("message", result))
            handler?.report(ServiceMessageBuilder.testFinished(reportedName))
        } else {
            handler?.report(ServiceMessageBuilder.testFinished(reportedName)
                    .addAttribute("text", result ?: "$reportedName passed"))
        }
    }
}

class OrionTestInterceptor(private val project: Project) : OrionTestParser {
    private var handler: ProcessHandler? = null
    // test name -> task
    private lateinit var testMappings: MutableMap<String, Task>
    private lateinit var testTreeRoot: Task

    override val isAttachedToProcess: Boolean
        get() = handler != null

    override fun onTestingStarted() {
        val builder = ServiceMessageBuilder(COMMAND_TESTING_STARTED)
        handler?.report(builder)
    }

    override fun onTestingFinished() {
        // publish results
        testTreeRoot.subtasks.forEach { publishTestTree(it) }

        handler?.destroyProcess()
    }

    private fun publishTestTree(task: Task) {
        handler?.report(ServiceMessageBuilder.testSuiteStarted(task.name))

        var ctr = 1
        task.testResults.toHashSet().forEach{ it.report(handler, ctr++, true, ">> ") }
        task.subtasks.forEach { publishTestTree(it) }

        handler?.report(ServiceMessageBuilder.testSuiteFinished(task.name))
    }

    override fun onTestResult(success: Boolean, testName: String, result: String?) {
        if (result?.contains(OrionTestParser.NOT_EXECUTED_STRING, true) == true) { return }
        val testResult = TestResult(testName, result, success)
        testMappings[testName]?.testResults?.add(testResult)
    }

    override fun attachToProcessHandler(handler: ProcessHandler) {
        this.handler = handler
    }

    override fun detachProcessHandler() {
        this.handler = null
    }

    override fun onCompileError(errors: List<BuildLogFileErrorsDTO>) {
        errors.forEach { fileErrors ->
            fileErrors.errors
                    .toHashSet()
                    .forEach { error ->
                // Search for the full file path in the local file system
                val localFilePath = runInEdtAndGet {
                    val potentialFiles = FilenameIndex.getFilesByName(project, fileErrors.fileName.split("/").last(), GlobalSearchScope.allScope(project))

                    potentialFiles.takeIf { potentialFiles.isNotEmpty() }
                            ?.first { localFile -> localFile.virtualFile.path.contains(fileErrors.fileName) }
                            ?.virtualFile?.path
                }

                val buildTest = ServiceMessageBuilder.testStarted("Compile & Build Error")
                val buildFinished = ServiceMessageBuilder.testFailed("Compile & Build Error")
                handler?.report(buildTest)
                if (localFilePath != null) {
                    buildFinished.addAttribute("message", localFilePath.asFileBuildError(error))
                } else {
                    buildFinished.addAttribute("message", error.asLogMessage())
                }
                handler?.report(buildFinished)
            }
        }

        handler?.destroyProcess()
    }

    override fun parseTestTreeFrom(instructions: String) {
        testMappings = mutableMapOf()
        val tasks = instructions.split("\n").filter { it.contains("[task]") }
        testTreeRoot = Task("Artemis Tests", -1)
        buildTestNodes(testTreeRoot, tasksFromInstructions = tasks.toMutableList())
    }

    private fun buildTestNodes(parentNode: Task?, previousNode: Task? = null, tasksFromInstructions: MutableList<String>) {
        if (tasksFromInstructions.isEmpty()) return
        val taskFromInstructions = tasksFromInstructions.removeAt(0)
        val level = taskFromInstructions.levelOfIndentation
        val taskName = taskFromInstructions.replace(Regex("(.*\\[task\\]\\[)([^\\[\\]]+)(.*)"), "$2")
        val testNames = taskFromInstructions
                .replace(Regex("(.*\\[task\\]\\[)([^\\[\\]]+\\]\\()(.*)(\\).*)"), "$3")
                .replace(" ", "")
                .split(",")
        val parent = when {
            // child of previous node
            previousNode?.level ?: Int.MAX_VALUE < level -> previousNode
            // same level as parent of previous node
            previousNode?.level ?: Int.MIN_VALUE > level -> parentNode!!.parent
            // same level as previous node
            else -> parentNode
        }
        val currentNode = Task(taskName, level, parent = parent)
        parent?.subtasks?.add(currentNode)
        testNames.forEach { testMappings[it] = currentNode }

        buildTestNodes(parent, currentNode, tasksFromInstructions)
    }

    private companion object {
        /**
         * This is not arbitrary, the test console actually expects this string after the whole testing process started
         */
        const val COMMAND_TESTING_STARTED = "enteredTheMatrix"
    }
}

private fun ProcessHandler.report(message: String) {
    notifyTextAvailable(message, ProcessOutputTypes.STDOUT)
}

private fun ProcessHandler.report(message: ServiceMessageBuilder) {
    notifyTextAvailable(message.toString(), ProcessOutputTypes.STDOUT)
}

private fun ProcessHandler.error(message: String) {
    notifyTextAvailable(message, ProcessOutputTypes.STDERR)
}

private fun Long.asBuildTimestamp(): String {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}

private fun String.asFileBuildError(error: BuildError): String {
    val prefix = if (Platform.current() == Platform.UNIX) "file://" else "file:///"
    return "${error.timestamp.asBuildTimestamp()}  [${error.type.toUpperCase()}]\t\t$prefix$this:${error.row + 1}:${error.column + 1}:  ${error.text}\n"
}

private fun BuildError.asLogMessage(): String {
    return "${this.timestamp.asBuildTimestamp()}  [${this.type.toUpperCase()}]\t\t${this.text}\n"
}

private val String.levelOfIndentation: Int
    get() {
        if (!this.startsWith(" ") && !this.startsWith("\t")) return 0
        return this.takeWhile { it == ' ' || it == '\t' }.length
    }
