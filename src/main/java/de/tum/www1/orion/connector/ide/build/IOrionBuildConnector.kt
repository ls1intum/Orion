package de.tum.www1.orion.connector.ide.build

interface IOrionBuildConnector {
    /**
     * This will build and test the focused repository locally using the language specific build/test agent
     */
    fun buildAndTestLocally()

    /**
     * Notify external build started
     */
    fun onBuildStarted(exerciseInstructions: String)

    /**
     * Notify external build finished without any compile errors
     */
    fun onBuildFinished()

    /**
     * Notify external build failed with compile errors
     *
     * @param buildLogsJsonString The build log errors. Will be parsed into [de.tum.www1.orion.dto.BuildLogFileErrorsDTO]
     */
    fun onBuildFailed(buildLogsJsonString: String)

    /**
     * Notify about incoming test result
     *
     * @param success True, if the test was successful, false otherwise
     * @param message Any message related to the test, which should be displayed on the console
     */
    fun onTestResult(success: Boolean, testName: String, message: String)
}