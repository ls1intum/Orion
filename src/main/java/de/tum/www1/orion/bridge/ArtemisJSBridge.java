package de.tum.www1.orion.bridge;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intellij.execution.RunManager;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import de.tum.www1.orion.build.ArtemisSubmitRunConfigurationType;
import de.tum.www1.orion.build.ArtemisTestParser;
import de.tum.www1.orion.dto.BuildLogErrorDTO;
import de.tum.www1.orion.util.ArtemisExerciseRegistry;
import de.tum.www1.orion.vcs.ArtemisGitUtil;
import de.tum.www1.orion.vcs.CredentialsService;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ArtemisJSBridge implements ArtemisBridge {
    private static final Logger LOG = LoggerFactory.getLogger(ArtemisJSBridge.class);

    private static final String DOWNCALL_BRIDGE = "window.javaDowncallBridge.";
    private static final String ON_EXERCISE_OPENED = DOWNCALL_BRIDGE + "onExerciseOpened(%d)";

    private final Project project;
    private WebEngine webEngine;
    private boolean artemisLoaded;

    /**
     * A queue used for storing jobs that should run as soon as the ArTEMiS webapp has been loaded. Until then, the tasks
     * are stored in this list.
     */
    private List<Runnable> dispatchQueue;

    public ArtemisJSBridge(Project project) {
        this.project = project;
        this.dispatchQueue = new LinkedList<>();
    }

    @Override
    public void clone(String repository, String exerciseName, int exerciseId, int courseId) {
        final ArtemisExerciseRegistry registry = ServiceManager.getService(project, ArtemisExerciseRegistry.class);
        if (!registry.alreadyImported(exerciseId)) {
            ServiceManager.getService(project, ArtemisExerciseRegistry.class).onNewExercise(courseId, exerciseId, exerciseName);
            ArtemisGitUtil.Companion.clone(project, repository, courseId, exerciseId, exerciseName);
        } else {
            ApplicationManager.getApplication().invokeLater(() -> ProjectUtil.openOrImport(ArtemisGitUtil.Companion.setupExerciseDirPath(courseId, exerciseId, exerciseName), project, false));
        }
    }

    @Override
    public void addCommitAndPushAllChanges() {
        ArtemisGitUtil.Companion.submit(project);
    }

    @Override
    public void login(String username, String password) {
        ServiceManager.getService(CredentialsService.class).storeGitCredentials(username, password);
    }

    @Override
    public void onOpenedExercise(int exerciseId) {
        runAfterLoaded(() -> webEngine.executeScript(String.format(ON_EXERCISE_OPENED, exerciseId)));
    }

    @Override
    public void artemisLoadedWith(WebEngine engine) {
        artemisLoaded = true;
        webEngine = engine;
        dispatchQueue.forEach(Platform::runLater);
    }

    @Override
    public void log(String message) {
        LOG.debug(message);
    }

    @Override
    public void onBuildStarted() {
        final var runManager = RunManager.getInstance(project);
        final var settings = runManager
                .createConfiguration("Build & Test on Artemis Server", ArtemisSubmitRunConfigurationType.class);
        ExecutionUtil.runConfiguration(settings, DefaultRunExecutor.getRunExecutorInstance());
    }

    @Override
    public void onBuildFinished() {
        ServiceManager.getService(project, ArtemisTestParser.class).onTestingFinished();
    }

    @Override
    public void onBuildFailed(String buildLogsJsonString) {
        final var failedLogsType = new TypeToken<Map<String, BuildLogErrorDTO>>() {}.getType();
        final Map<String, BuildLogErrorDTO> builErrors = new Gson().fromJson(buildLogsJsonString, failedLogsType);
        final var testParser = ServiceManager.getService(project, ArtemisTestParser.class);
        testParser.onTestResult(false, buildLogsJsonString);
        testParser.onTestingFinished();
    }

    @Override
    public void onTestResult(boolean success, String message) {
        ServiceManager.getService(project, ArtemisTestParser.class).onTestResult(success, message);
    }

    private void runAfterLoaded(final Runnable task) {
        if (!artemisLoaded) {
            dispatchQueue.add(task);
        } else {
            Platform.runLater(task);
        }
    }
}
