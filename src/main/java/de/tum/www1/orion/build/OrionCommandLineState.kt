package de.tum.www1.orion.build

import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.process.NopProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView
import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import de.tum.www1.orion.bridge.ArtemisBridge
import de.tum.www1.orion.util.registry.OrionExerciseRegistry
import de.tum.www1.orion.vcs.OrionGitUtil

class OrionCommandLineState(private val project: Project, environment: ExecutionEnvironment) : CommandLineState(environment) {
    private lateinit var handler: ProcessHandler
    private lateinit var console: BaseTestsOutputConsoleView

    override fun startProcess(): ProcessHandler {
        // Does nothing, just idles and waits for process termination
        handler = OrionBuildProcessHandler(project)
        val props = SMTRunnerConsoleProperties(environment.runProfile as OrionRunConfiguration, "Artemis Build Output", environment.executor)
        // Console in the bottom tool window displaying all test results
        console = SMTestRunnerConnectionUtil.createAndAttachConsole("Artemis Build Output", handler, props)
        val testParser = ServiceManager.getService(project, OrionTestParser::class.java)
        testParser.attachToProcessHandler(handler)
        testParser.onTestingStarted()

        prepareForInternalRun()

        return handler
    }

    private fun prepareForInternalRun() {
        val runConfiguration = environment.runnerAndConfigurationSettings?.configuration as OrionRunConfiguration
        if (runConfiguration.triggeredInIDE) {
            OrionGitUtil.submit(project)
            ServiceManager.getService(project, OrionExerciseRegistry::class.java).exerciseInfo?.let {
                ServiceManager.getService(project, ArtemisBridge::class.java)
                        .startedBuildInIntelliJ(it.courseId, it.exerciseId)
            }
        } else {
            // Set to true for follow-up runs originating from IntelliJ
            runConfiguration.triggeredInIDE = true
        }
    }

    override fun createConsole(executor: Executor): ConsoleView? {
        return console
    }

    override fun execute(executor: Executor, runner: ProgramRunner<*>): ExecutionResult {
        val processHandler = startProcess()
        val console = createConsole(executor)

        return DefaultExecutionResult(console, processHandler, *createActions(console, processHandler, executor))
    }
}

class OrionBuildProcessHandler(project: Project) : NopProcessHandler() {
    private val jsBridge: ArtemisBridge = ServiceManager.getService(project, ArtemisBridge::class.java)

    override fun startNotify() {
        super.startNotify()
        jsBridge.isBuilding(true)
    }

    override fun notifyProcessTerminated(exitCode: Int) {
        super.notifyProcessTerminated(exitCode)
        jsBridge.isBuilding(false)
    }
}