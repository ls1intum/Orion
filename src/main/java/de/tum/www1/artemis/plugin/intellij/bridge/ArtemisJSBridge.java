package de.tum.www1.artemis.plugin.intellij.bridge;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import de.tum.www1.artemis.plugin.intellij.ui.ConfirmPasswordSaveDialog;
import de.tum.www1.artemis.plugin.intellij.util.ArtemisExerciseRegistry;
import de.tum.www1.artemis.plugin.intellij.vcs.ArtemisGitUtil;
import de.tum.www1.artemis.plugin.intellij.vcs.CredentialsService;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

import static com.intellij.openapi.application.ApplicationManager.getApplication;

public class ArtemisJSBridge implements ArtemisBridge {
    private static final Logger LOG = LoggerFactory.getLogger(ArtemisJSBridge.class);

    private final Project myProject;
    private WebEngine myEngine;
    private boolean artemisLoaded;
    private List<Runnable> dispatchQueue;

    public ArtemisJSBridge(Project project) {
        this.myProject = project;
        this.dispatchQueue = new LinkedList<>();
    }

    @Override
    public void clone(String repository, String exerciseName, int exerciseId, int courseId) {
        ServiceManager.getService(myProject, ArtemisExerciseRegistry.class).onNewExercise(courseId, exerciseId, exerciseName);
        ArtemisGitUtil.Companion.clone(myProject, repository, courseId, exerciseId, exerciseName);
    }

    @Override
    public void addCommitAndPushAllChanges() {
        ArtemisGitUtil.Companion.submit(myProject);
    }

    @Override
    public void login(String username, String password) {
        getApplication().invokeLater(() -> {
            if (new ConfirmPasswordSaveDialog(myProject).showAndGet()) {
                ServiceManager.getService(CredentialsService.class).storeGitCredentials(username, password);
            }
        });
    }

    @Override
    public void onOpenedExercise(int exerciseId) {
        runAfterLoaded(() -> myEngine.executeScript(String.format("window.javaDowncallBridge.onExerciseOpened(%d)", exerciseId)));
    }

    @Override
    public void artemisLoadedWith(WebEngine engine) {
        artemisLoaded = true;
        myEngine = engine;
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
            task.run();
        }
    }
}
