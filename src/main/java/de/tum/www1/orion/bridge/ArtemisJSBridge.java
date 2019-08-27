package de.tum.www1.orion.bridge;

import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import de.tum.www1.orion.ui.ConfirmPasswordSaveDialog;
import de.tum.www1.orion.util.ArtemisExerciseRegistry;
import de.tum.www1.orion.vcs.ArtemisGitUtil;
import de.tum.www1.orion.vcs.CredentialsService;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

import static com.intellij.openapi.application.ApplicationManager.getApplication;

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
            ProjectUtil.openOrImport(ArtemisGitUtil.Companion.setupExerciseDirPath(courseId, exerciseId, exerciseName), project, false);
        }
    }

    @Override
    public void addCommitAndPushAllChanges() {
        ArtemisGitUtil.Companion.submit(project);
    }

    @Override
    public void login(String username, String password) {
        getApplication().invokeLater(() -> {
            if (new ConfirmPasswordSaveDialog(project).showAndGet()) {
                ServiceManager.getService(CredentialsService.class).storeGitCredentials(username, password);
            }
        });
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

    private void runAfterLoaded(final Runnable task) {
        if (!artemisLoaded) {
            dispatchQueue.add(task);
        } else {
            Platform.runLater(task);
        }
    }
}
