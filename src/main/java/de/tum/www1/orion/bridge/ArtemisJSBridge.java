package de.tum.www1.orion.bridge;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.intellij.execution.RunManager;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import de.tum.www1.orion.build.OrionSubmitRunConfigurationType;
import de.tum.www1.orion.build.OrionTestParser;
import de.tum.www1.orion.dto.BuildError;
import de.tum.www1.orion.dto.BuildLogFileErrorsDTO;
import de.tum.www1.orion.dto.ProgrammingExerciseDTO;
import de.tum.www1.orion.util.OrionInstructorExerciseRegistry;
import de.tum.www1.orion.util.OrionStudentExerciseRegistry;
import de.tum.www1.orion.vcs.CredentialsService;
import de.tum.www1.orion.vcs.OrionGitUtil;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

import static de.tum.www1.orion.util.UtilsKt.setupExerciseDirPath;

public class ArtemisJSBridge implements ArtemisBridge {
    private static final Logger LOG = LoggerFactory.getLogger(ArtemisJSBridge.class);

    private static final String DOWNCALL_BRIDGE = "window.javaDowncallBridge.";
    private static final String ON_EXERCISE_OPENED = DOWNCALL_BRIDGE + "onExerciseOpened(%d)";
    private static final String ON_EXERCISE_OPENED_INSTRUCTOR = DOWNCALL_BRIDGE + "onExerciseOpenedAsInstructor(%d)";

    private final Project project;
    private WebEngine webEngine;
    private boolean artemisLoaded;

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
    public void clone(String repository, String exerciseName, int exerciseId, int courseId) {
        final OrionStudentExerciseRegistry registry = ServiceManager.getService(project, OrionStudentExerciseRegistry.class);
        if (!registry.alreadyImported(exerciseId)) {
            ServiceManager.getService(project, OrionStudentExerciseRegistry.class).onNewExercise(courseId, exerciseId, exerciseName);
            OrionGitUtil.Companion.cloneAndOpenExercise(project, repository, courseId, exerciseId, exerciseName);
        } else {
            ApplicationManager.getApplication().invokeLater(() -> ProjectUtil.openOrImport(setupExerciseDirPath(courseId, exerciseId, exerciseName), project, false));
        }
    }

    @Override
    public void addCommitAndPushAllChanges() {
        OrionGitUtil.Companion.submit(project);
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
    public void onOpenedExerciseAsInstructor(int exerciseId) {
        runAfterLoaded(() -> webEngine.executeScript(String.format(ON_EXERCISE_OPENED_INSTRUCTOR, exerciseId)));
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
                .createConfiguration("Build & Test on Artemis Server", OrionSubmitRunConfigurationType.class);
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

    @Override
    public void editExercise(String exerciseJson) {
        final var exercise = new Gson().fromJson(exerciseJson, ProgrammingExerciseDTO.class);
        final var registry = ServiceManager.getService(project, OrionInstructorExerciseRegistry.class);
        if (!registry.alreadyImported(exercise.getId())) {
            ServiceManager.getService(project, OrionInstructorExerciseRegistry.class).onNewExercise(exercise);
        } else {
            ApplicationManager.getApplication().invokeLater(() -> ProjectUtil.openOrImport(setupExerciseDirPath(exercise.getCourse().getId(), exercise.getId(), exercise.getTitle()), project, false));
        }
    }

    private void runAfterLoaded(final Runnable task) {
        if (!artemisLoaded) {
            dispatchQueue.add(task);
        } else {
            Platform.runLater(task);
        }
    }
}
