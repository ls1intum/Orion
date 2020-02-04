package de.tum.www1.orion.bridge.test;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.intellij.execution.RunManager;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import de.tum.www1.orion.bridge.ArtemisConnector;
import de.tum.www1.orion.build.OrionRunConfiguration;
import de.tum.www1.orion.build.OrionSubmitRunConfigurationType;
import de.tum.www1.orion.build.OrionTestParser;
import de.tum.www1.orion.dto.BuildError;
import de.tum.www1.orion.dto.BuildLogFileErrorsDTO;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrionTestResultReporter extends ArtemisConnector implements ArtemisTestResultReporter {
    private final Project project;

    public OrionTestResultReporter(Project project) {
        this.project = project;
    }

    @Override
    public void onBuildStarted(String exerciseInstructions) {
        // Only listen to the first execution result
        final var testParser = ServiceManager.getService(project, OrionTestParser.class);
        if (!testParser.isAttachedToProcess()) {
            final var runManager = RunManager.getInstance(project);
            final var settings = runManager
                    .createConfiguration("Build & Test on Artemis Server", OrionSubmitRunConfigurationType.class);
            ((OrionRunConfiguration) settings.getConfiguration()).setTriggeredInIDE(false);
            ExecutionUtil.runConfiguration(settings, DefaultRunExecutor.getRunExecutorInstance());
            testParser.parseTestTreeFrom(exerciseInstructions);
        }
    }

    @Override
    public void onBuildFinished() {
        ServiceManager.getService(project, OrionTestParser.class).onTestingFinished();
    }

    @Override
    public void onBuildFailed(String buildLogsJsonString) {
        final var mapType = new TypeToken<Map<String, List<BuildError>>>() {}.getType();
        final var allErrors = new JsonParser().parse(buildLogsJsonString).getAsJsonObject().get("errors");
        final Map<String, List<BuildError>> errors = new Gson().fromJson(allErrors, mapType);
        final var buildErrors = errors.entrySet().stream()
                .map(fileErrors -> new BuildLogFileErrorsDTO(fileErrors.getKey(), fileErrors.getValue()))
                .collect(Collectors.toList());
        final var testParser = ServiceManager.getService(project, OrionTestParser.class);
        testParser.onCompileError(buildErrors);
    }

    @Override
    public void onTestResult(boolean success, String testName, String message) {
        ServiceManager.getService(project, OrionTestParser.class).onTestResult(success, testName, message);
    }
}
