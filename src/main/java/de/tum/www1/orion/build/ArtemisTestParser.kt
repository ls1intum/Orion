package de.tum.www1.orion.build

import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project

interface ArtemisTestParser {
    fun onTestingStarted()
    fun onTestingFinished()
    fun onTestResult(success: Boolean, result: String)
    fun attachToProcessHandler(handler: ProcessHandler)

    companion object {
        const val NOT_EXECUTED_STRING = "Test was not executed"

        @JvmStatic
        fun getInstance(project: Project): ArtemisTestParser {
            return ServiceManager.getService(project, ArtemisTestParser::class.java)
        }
    }
}
