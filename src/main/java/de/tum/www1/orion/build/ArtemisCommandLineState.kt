package de.tum.www1.orion.build

import com.intellij.execution.Executor
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil
import com.intellij.execution.testframework.sm.ServiceMessageBuilder
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties
import com.intellij.execution.ui.ConsoleView

class ArtemisCommandLineState(environment: ExecutionEnvironment) : CommandLineState(environment) {
    private lateinit var handler: ProcessHandler

    override fun startProcess(): ProcessHandler {
        val cmd = GeneralCommandLine()
        handler = ProcessHandlerFactory.getInstance().createProcessHandler(cmd)
        doWork()
        return handler
    }

    override fun createConsole(executor: Executor): ConsoleView? {
        val props = SMTRunnerConsoleProperties(environment.runProfile as ArtemisRunConfiguration, "Amazing framework", environment.executor)
        return SMTestRunnerConnectionUtil.createConsole(props)
    }

    private fun doWork() {
        handler.notifyTextAvailable("Test running started\n", ProcessOutputTypes.STDOUT)

        val builder = ServiceMessageBuilder("enteredTheMatrix")
        handler.notifyTextAvailable(builder.toString(), ProcessOutputTypes.STDOUT)
    }
}