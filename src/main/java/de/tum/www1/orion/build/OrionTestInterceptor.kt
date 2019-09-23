package de.tum.www1.orion.build

import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.execution.testframework.sm.ServiceMessageBuilder
import com.intellij.openapi.project.Project

private fun ProcessHandler.report(message: String) {
    notifyTextAvailable(message, ProcessOutputTypes.STDOUT)
}

private fun ProcessHandler.report(message: ServiceMessageBuilder) {
    notifyTextAvailable(message.toString(), ProcessOutputTypes.STDOUT)
}

class OrionTestInterceptor(private val project: Project) : ArtemisTestParser {
    private var handler: ProcessHandler? = null
    private var testCtr = 1

    override fun onTestingStarted() {
        testCtr = 1
        val builder = ServiceMessageBuilder(COMMAND_TESTING_STARTED)
        handler?.report(builder)
    }

    override fun onTestingFinished() {
        handler?.destroyProcess()
        this.handler = null
    }

    override fun onTestResult(success: Boolean, result: String) {
        val testName = "Test #" + testCtr++
        var builder = ServiceMessageBuilder.testStarted(testName)
        handler?.report(builder)
        handler?.report(result)
        if (!success) {
            builder = ServiceMessageBuilder.testFailed(testName)
            handler?.report(builder)
        }
        builder = ServiceMessageBuilder.testFinished(testName)
        handler?.report(builder)
    }

    override fun attachToProcessHandler(handler: ProcessHandler) {
        this.handler = handler
    }

    private companion object {
        const val COMMAND_TESTING_STARTED = "enteredTheMatrix"
    }
}
