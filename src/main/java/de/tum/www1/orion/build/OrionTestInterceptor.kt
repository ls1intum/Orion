package de.tum.www1.orion.build

import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.execution.testframework.sm.ServiceMessageBuilder
import com.intellij.openapi.project.Project
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import de.tum.www1.orion.dto.BuildError
import de.tum.www1.orion.util.invokeOnEDTAndWait
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicInteger

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
    return "${error.timestamp.asBuildTimestamp()}  [${error.type.toUpperCase()}]\t\tfile://$this:${error.row + 1}:${error.column + 1}:  ${error.text}\n"
}

private fun BuildError.asLogMessage(): String {
    return "${this.timestamp.asBuildTimestamp()}  [${this.type.toUpperCase()}]\t\t${this.text}\n"
}

class OrionTestInterceptor(private val project: Project) : ArtemisTestParser {
    private var handler: ProcessHandler? = null

    override fun onTestingStarted() {
        testCtr = AtomicInteger(1)
        val builder = ServiceMessageBuilder(COMMAND_TESTING_STARTED)
        handler?.report(builder)
    }

    override fun onTestingFinished() {
        handler?.destroyProcess()
        this.handler = null
    }

    override fun onTestResult(success: Boolean, result: String?) {
        if (result?.contains(ArtemisTestParser.NOT_EXECUTED_STRING, true) == true) { return }
        val testName = "Test #" + testCtr.getAndIncrement()
        var builder = ServiceMessageBuilder.testStarted(testName)
        handler?.apply {
            report(builder)
            if (!success) {
                builder = ServiceMessageBuilder.testFailed(testName)
                        .addAttribute("message", result)
                report(builder)
                builder = ServiceMessageBuilder.testFinished(testName)
                report(builder)
            } else {
                builder = ServiceMessageBuilder.testFinished(testName)
                        .addAttribute("text", result ?: "$testName passed")
                report(builder)
            }
        }
    }

    override fun attachToProcessHandler(handler: ProcessHandler) {
        this.handler = handler
    }

    override fun onCompileError(file: String, error: BuildError) {
        // Search for the full file path in the local file system
        val localFilePath = invokeOnEDTAndWait {
            val potentialFiles = FilenameIndex.getFilesByName(project, file.split("/").last(), GlobalSearchScope.allScope(project))

            val localizedPath = file.replace('/', File.separatorChar)
            potentialFiles.takeIf { potentialFiles.isNotEmpty() }
                    ?.first { localFile -> localFile.virtualFile.path.contains(localizedPath) }
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

    private companion object {
        /**
         * This is not arbitrary, the test console actually expects this string after the whole testing process started
         */
        const val COMMAND_TESTING_STARTED = "enteredTheMatrix"
        private var testCtr: AtomicInteger = AtomicInteger(1)
    }
}
