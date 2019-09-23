package de.tum.www1.orion.build

import com.intellij.execution.Executor
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.process.NopProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView
import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project

class ArtemisCommandLineState(private val project: Project, environment: ExecutionEnvironment) : CommandLineState(environment) {
    private lateinit var handler: ProcessHandler
    private lateinit var console: BaseTestsOutputConsoleView

    override fun startProcess(): ProcessHandler {
        handler = NopProcessHandler()
        val props = SMTRunnerConsoleProperties(environment.runProfile as ArtemisRunConfiguration, "Artemis Build Output", environment.executor)
        console = SMTestRunnerConnectionUtil.createAndAttachConsole("Artemis Build Output", handler, props)
        ServiceManager.getService(project, ArtemisTestParser::class.java).attachToProcessHandler(handler)

        return handler
    }

    override fun createConsole(executor: Executor): ConsoleView? {
        return console
    }


}