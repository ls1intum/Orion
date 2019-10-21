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

class OrionCommandLineState(private val project: Project, environment: ExecutionEnvironment) : CommandLineState(environment) {
    private lateinit var handler: ProcessHandler
    private lateinit var console: BaseTestsOutputConsoleView

    override fun startProcess(): ProcessHandler {
        // Does nothing, just idles and waits for process termination
        handler = NopProcessHandler()
        val props = SMTRunnerConsoleProperties(environment.runProfile as OrionRunConfiguration, "Artemis Build Output", environment.executor)
        // Console in the bottom tool window displaying all test results
        console = SMTestRunnerConnectionUtil.createAndAttachConsole("Artemis Build Output", handler, props)
        val testParser = ServiceManager.getService(project, OrionTestParser::class.java)
        testParser.attachToProcessHandler(handler)
        testParser.onTestingStarted()

        return handler
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