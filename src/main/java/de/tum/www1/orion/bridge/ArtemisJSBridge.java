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
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.LocalFileSystem;
import de.tum.www1.orion.build.OrionRunConfiguration;
import de.tum.www1.orion.build.OrionSubmitRunConfigurationType;
import de.tum.www1.orion.build.OrionTestParser;
import de.tum.www1.orion.build.instructor.OrionInstructorBuildUtil;
import de.tum.www1.orion.dto.BuildError;
import de.tum.www1.orion.dto.BuildLogFileErrorsDTO;
import de.tum.www1.orion.dto.ProgrammingExerciseDTO;
import de.tum.www1.orion.dto.RepositoryType;
import de.tum.www1.orion.enumeration.ExerciseView;
import de.tum.www1.orion.util.OrionFileUtils;
import de.tum.www1.orion.util.OrionInstructorExerciseRegistry;
import de.tum.www1.orion.util.OrionProjectUtil;
import de.tum.www1.orion.util.OrionStudentExerciseRegistry;
import de.tum.www1.orion.vcs.CredentialsService;
import de.tum.www1.orion.vcs.OrionGitUtil;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class ArtemisJSBridge implements ArtemisBridge {
    private static final Logger LOG = LoggerFactory.getLogger(ArtemisJSBridge.class);

    private static final String DOWNCALL_BRIDGE = "window.javaDowncallBridge.";
    private static final String ON_EXERCISE_OPENED = DOWNCALL_BRIDGE + "onExerciseOpened(%d)";
    private static final String IS_CLONING = DOWNCALL_BRIDGE + "isCloning(%b)";
    private static final String IS_BUILDING = DOWNCALL_BRIDGE + "isBuilding(%b)";
    private  static final String TRIGGER_BUILD_FROM_IDE = DOWNCALL_BRIDGE + "startedBuildInIntelliJ(%d, %d)";
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
        final var registry = ServiceManager.getService(project, OrionStudentExerciseRegistry.class);
        if (!registry.alreadyImported(exerciseId)) {
            registry.onNewExercise(courseId, exerciseId, exerciseName);
            OrionGitUtil.INSTANCE.cloneAndOpenExercise(project, repository, courseId, exerciseId, exerciseName);
        } else {
            final var exercisePath = OrionFileUtils.INSTANCE.getExerciseFullPath(courseId, exerciseId, exerciseName, ExerciseView.STUDENT);
            ApplicationManager.getApplication().invokeLater(() -> ProjectUtil.openOrImport(exercisePath, project, false));
        }
    }

    @Override
    public void addCommitAndPushAllChanges() {
        OrionGitUtil.INSTANCE.submit(project, true);
    }

    @Override
    public void login(String username, String password) {
        ServiceManager.getService(CredentialsService.class).storeGitCredentials(username, password);
    }

    @Override
    public void onOpenedExercise(long opened) {
        runAfterLoaded(() -> webEngine.executeScript(String.format(ON_EXERCISE_OPENED, opened)));
    }

    @Override
    public void onOpenedExerciseAsInstructor(long exerciseId) {
        runAfterLoaded(() -> webEngine.executeScript(String.format(ON_EXERCISE_OPENED_INSTRUCTOR, exerciseId)));
    }

    @Override
    public void selectInstructorRepository(String repository) {
        final var parsedRepo = RepositoryType.valueOf(repository);
        ServiceManager.getService(project, OrionInstructorExerciseRegistry.class).setSelectedRepository(parsedRepo);
    }

    @Override
    public void submitInstructorRepository() {
        final var repository = ServiceManager.getService(project, OrionInstructorExerciseRegistry.class).getSelectedRepository();
        final var projectDir = new File(Objects.requireNonNull(project.getBasePath()));
        // Always works, since we always have our three base modules for instructors
        final var moduleDir = projectDir.listFiles((file, name) -> name.equals(repository.getDirectoryName()))[0];
        final var moduleFile = LocalFileSystem.getInstance().findFileByIoFile(moduleDir);
        final var module = ServiceManager.getService(project, ProjectFileIndex.class).getModuleForFile(moduleFile);

        OrionGitUtil.INSTANCE.submit(module);
    }

    @Override
    public void buildAndTestInstructorRepository() {
        ServiceManager.getService(project, OrionInstructorBuildUtil.class).runTestsLocally();
    }

    @Override
    public void isCloning(boolean cloning) {
        runAfterLoaded(() -> webEngine.executeScript(String.format(IS_CLONING, cloning)));
    }

    @Override
    public void isBuilding(boolean building) {
        runAfterLoaded(() -> webEngine.executeScript(String.format(IS_BUILDING, building)));
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

    @Override
    public void startedBuildInIntelliJ(long courseId, long exerciseId) {
        runAfterLoaded(() -> webEngine.executeScript(String.format(TRIGGER_BUILD_FROM_IDE, courseId, exerciseId)));
    }

    @Override
    public void editExercise(String exerciseJson) {
        final var exercise = new Gson().fromJson(exerciseJson, ProgrammingExerciseDTO.class);
        final var registry = ServiceManager.getService(project, OrionInstructorExerciseRegistry.class);
        if (!registry.alreadyImported(exercise.getId())) {
            final var newProject = OrionProjectUtil.INSTANCE.newEmptyProject(exercise.getCourse().getId(), exercise.getId(), exercise.getTitle(), ExerciseView.INSTRUCTOR);
            OrionGitUtil.INSTANCE.clone(project, exercise.getTemplateParticipation().getRepositoryUrl().toString(),
                    newProject.getBasePath(), newProject.getBasePath() + "/exercise", null);
            OrionGitUtil.INSTANCE.clone(project, exercise.getTestRepositoryUrl().toString(),
                    newProject.getBasePath(), newProject.getBasePath() + "/tests", null);
            OrionGitUtil.INSTANCE.clone(project, exercise.getSolutionParticipation().getRepositoryUrl().toString(),
                    newProject.getBasePath(), newProject.getBasePath() + "/solution", null);
//            OrionProjectUtil.INSTANCE.newModule(Objects.requireNonNull(newProject), "exercise");
//            OrionProjectUtil.INSTANCE.newModule(Objects.requireNonNull(newProject), "tests");
//            OrionProjectUtil.INSTANCE.newModule(Objects.requireNonNull(newProject), "solution");
            registry.onNewExercise(exercise);
        } else {
            final var exercisePath = OrionFileUtils.INSTANCE.getExerciseFullPath(exercise, ExerciseView.INSTRUCTOR);
            ApplicationManager.getApplication().invokeLater(() -> ProjectUtil.openOrImport(exercisePath, project, false));
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
