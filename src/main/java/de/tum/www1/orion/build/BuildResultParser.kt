package de.tum.www1.orion.build

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.execution.testframework.sm.ServiceMessageBuilder
import com.intellij.openapi.components.ServiceManager

class BuildResultParser {
    fun foo() {
        val cmd = GeneralCommandLine()
        val handler = ServiceManager.getService(ProcessHandlerFactory::class.java).createProcessHandler(cmd)

//        val exec = DefaultRunExecutor.getRunExecutorInstance()
//        val props =
//        SMTestRunnerConnectionUtil.createAndAttachConsole("Random framework", handler, exec)

        handler.notifyTextAvailable("Test running started...", ProcessOutputTypes.STDOUT)
        val smb = ServiceMessageBuilder("enteredTheMatrix");
        handler.notifyTextAvailable(smb.toString(), ProcessOutputTypes.STDOUT)

        val suiteBuilder = ServiceMessageBuilder("suite started")
        handler.notifyTextAvailable(suiteBuilder.toString(), ProcessOutputTypes.STDOUT)
    }

    companion object {
        @JvmStatic
        fun getInstance(): BuildResultParser {
            return BuildResultParser()
        }
    }
}