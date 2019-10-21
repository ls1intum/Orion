package de.tum.www1.orion.build

import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import de.tum.www1.orion.dto.BuildError

interface OrionTestParser {
    /**
     * Notify the test process that testing has started
     */
    fun onTestingStarted()

    /**
     * Notify the test process that testing finished and all results can now be displayed
     */
    fun onTestingFinished()

    /**
     * Notify the test process that a new test result was received. The result will be displayed appropriately
     * in the test console incl. the given message
     *
     * @param success True, if the result was successful, false otherwise
     * @param result Message related to the result, explaining what went wrong, or why the result was successful
     */
    fun onTestResult(success: Boolean, result: String?)
    fun attachToProcessHandler(handler: ProcessHandler)
    fun onCompileError(file: String, error: BuildError)

    companion object {
        /**
         * Default value if a test was not executed
         */
        const val NOT_EXECUTED_STRING = "Test was not executed"

        @JvmStatic
        fun getInstance(project: Project): OrionTestParser {
            return ServiceManager.getService(project, OrionTestParser::class.java)
        }
    }
}
