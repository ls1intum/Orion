package de.tum.www1.orion.bridge;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.intellij.execution.RunManager;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import de.tum.www1.orion.build.OrionRunConfiguration;
import de.tum.www1.orion.build.OrionSubmitRunConfigurationType;
import de.tum.www1.orion.build.OrionTestParser;
import de.tum.www1.orion.dto.BuildError;
import de.tum.www1.orion.dto.BuildLogFileErrorsDTO;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

public class ArtemisJSBridge implements ArtemisBridge {
    private final Project project;

    /**
     * A queue used for storing jobs that should run as soon as the ArTEMiS webapp has been loaded. Until then, the tasks
     * are stored in this list.
     */
    private Queue<Runnable> dispatchQueue;

    public ArtemisJSBridge(Project project) {
        this.project = project;
        this.dispatchQueue = new LinkedList<>();
    }

    @Override
    public void onBuildStarted() {
        final var runManager = RunManager.getInstance(project);
        final var settings = runManager
                .createConfiguration("Build & Test on Artemis Server", OrionSubmitRunConfigurationType.class);
        ((OrionRunConfiguration) settings.getConfiguration()).setTriggeredInIDE(false);
        ExecutionUtil.runConfiguration(settings, DefaultRunExecutor.getRunExecutorInstance());
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
        buildErrors.forEach(fileErrors -> fileErrors.getErrors().forEach(error -> testParser.onCompileError(fileErrors.getFileName(), error)));
        testParser.onTestingFinished();
    }

    @Override
    public void onTestResult(boolean success, String message) {
        ServiceManager.getService(project, OrionTestParser.class).onTestResult(success, message);
    }

}
