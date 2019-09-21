package de.tum.www1.orion.build

import com.intellij.execution.Executor
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.NopProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil
import com.intellij.execution.testframework.sm.ServiceMessageBuilder
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView
import com.intellij.execution.ui.ConsoleView

class ArtemisCommandLineState(environment: ExecutionEnvironment) : CommandLineState(environment) {
    private lateinit var handler: ProcessHandler
    private lateinit var console: BaseTestsOutputConsoleView

    override fun startProcess(): ProcessHandler {
        val cmd = GeneralCommandLine()
//        handler = ProcessHandlerFactory.getInstance().createProcessHandler(cmd)
//        handler = ArtemisProcessHandler()
        handler = NopProcessHandler()
        val props = SMTRunnerConsoleProperties(environment.runProfile as ArtemisRunConfiguration, "Amazing framework", environment.executor)
        console = SMTestRunnerConnectionUtil.createAndAttachConsole("Amazing framwork", handler, props)

        doWork()
        return handler
    }

    override fun createConsole(executor: Executor): ConsoleView? {
        return console
    }


    //    override fun createConsole(executor: Executor): ConsoleView? {
//        val props = SMTRunnerConsoleProperties(environment.runProfile as ArtemisRunConfiguration, "Amazing framework", environment.executor)
//        console = SMTestRunnerConnectionUtil.createAndAttachConsole("Amazing framwork", handler, props)
//
//
//        return console
//    }

    private fun doWork() {
        handler.notifyTextAvailable("Test running started\n", ProcessOutputTypes.STDOUT)

        val started = ServiceMessageBuilder.testSuiteStarted("Artemis test suite")
        handler.notifyTextAvailable(started.toString(), ProcessOutputTypes.STDOUT)

        val testStarted = ServiceMessageBuilder.testStarted("First test")
        handler.notifyTextAvailable(testStarted.toString(), ProcessOutputTypes.STDOUT)

        val testStopped = ServiceMessageBuilder.testFinished("First test")
        handler.notifyTextAvailable(testStopped.toString(), ProcessOutputTypes.STDOUT)

        val suiteStopped = ServiceMessageBuilder.testSuiteFinished("Artemis test suite")
        handler.notifyTextAvailable(suiteStopped.toString(), ProcessOutputTypes.STDOUT)

        ServiceMessageBuilder("")

        handler.destroyProcess()
    }
}