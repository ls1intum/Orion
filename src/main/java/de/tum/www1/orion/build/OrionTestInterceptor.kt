package de.tum.www1.orion.build

import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.execution.testframework.sm.ServiceMessageBuilder
import java.util.concurrent.atomic.AtomicInteger

private fun ProcessHandler.report(message: String) {
    notifyTextAvailable(message, ProcessOutputTypes.STDOUT)
}

private fun ProcessHandler.report(message: ServiceMessageBuilder) {
    notifyTextAvailable(message.toString(), ProcessOutputTypes.STDOUT)
}

class OrionTestInterceptor() : ArtemisTestParser {
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

    override fun onTestResult(success: Boolean, result: String) {
        if (result.contains(ArtemisTestParser.NOT_EXECUTED_STRING, true)) { return }
        val testName = "Test #" + testCtr.getAndIncrement()
        var builder = ServiceMessageBuilder.testStarted(testName)
        handler?.report(builder)
        if (!success) {
            builder = ServiceMessageBuilder.testFailed(testName)
                    .addAttribute("message", result)
            handler?.report(builder)
        } else {
            handler?.report(result)
        }
        builder = ServiceMessageBuilder.testFinished(testName)
        handler?.report(builder)
    }

    override fun attachToProcessHandler(handler: ProcessHandler) {
        this.handler = handler
    }

    private companion object {
        const val COMMAND_TESTING_STARTED = "enteredTheMatrix"
        private var testCtr: AtomicInteger = AtomicInteger(1)
    }
}
