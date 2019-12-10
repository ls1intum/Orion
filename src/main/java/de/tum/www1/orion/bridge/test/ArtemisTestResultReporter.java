package de.tum.www1.orion.bridge.test;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import de.tum.www1.orion.bridge.JavaUpcallBridge;
import org.jetbrains.annotations.NotNull;

public interface ArtemisTestResultReporter extends JavaUpcallBridge {
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

    static ArtemisTestResultReporter getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, ArtemisTestResultReporter.class);
    }
}
