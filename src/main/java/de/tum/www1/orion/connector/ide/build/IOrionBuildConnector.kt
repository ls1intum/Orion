package de.tum.www1.orion.connector.ide.build;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;

public interface IOrionBuildConnector {
    /**
     * This will build and test the focused repository locally using the language specific build/test agent
     */
    void buildAndTestLocally();

    /**
     * Notify external build started
     */
    void onBuildStarted(String exerciseInstructions);

    /**
     * Notify external build finished without any compile errors
     */
    void onBuildFinished();

    /**
     * Notify external build failed with compile errors
     *
     * @param buildLogsJsonString The build log errors. Will be parsed into {@link de.tum.www1.orion.dto.BuildLogFileErrorsDTO}
     */
    void onBuildFailed(String buildLogsJsonString);

    /**
     * Notify about incoming test result
     *
     * @param success True, if the test was successful, false otherwise
     * @param message Any message related to the test, which should be displayed on the console
     */
    void onTestResult(boolean success, String testName, String message);

    static IOrionBuildConnector getInstance(Project project) {
        return ServiceManager.getService(project, IOrionBuildConnector.class);
    }
}
