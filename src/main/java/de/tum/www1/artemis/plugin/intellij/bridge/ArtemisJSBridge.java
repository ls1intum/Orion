package de.tum.www1.artemis.plugin.intellij.bridge;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import de.tum.www1.artemis.plugin.intellij.ui.ArtemisRouterService;
import de.tum.www1.artemis.plugin.intellij.ui.ConfirmPasswordSaveDialog;
import de.tum.www1.artemis.plugin.intellij.vcs.ArtemisGitUtil;
import de.tum.www1.artemis.plugin.intellij.vcs.CredentialsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.intellij.openapi.application.ApplicationManager.getApplication;

public class ArtemisJSBridge implements ArtemisBridge {
    private static final Logger LOG = LoggerFactory.getLogger(ArtemisJSBridge.class);

    private final Project myProject;

    public ArtemisJSBridge(Project project) {
        this.myProject = project;
    }

    @Override
    public void clone(String repository, String exerciseName, int exerciseId, int courseId) {
        ServiceManager.getService(myProject, ArtemisRouterService.class).onNewExercise(exerciseName, exerciseId, courseId);
        ArtemisGitUtil.Companion.clone(myProject, repository, exerciseName);
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
    public void log(String message) {
        LOG.debug(message);
    }
}
