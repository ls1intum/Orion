package de.tum.www1.artemis.plugin.intellij.bridge;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import de.tum.www1.artemis.plugin.intellij.ui.ArtemisRouterService;
import de.tum.www1.artemis.plugin.intellij.vcs.ArtemisGitUtil;
import de.tum.www1.artemis.plugin.intellij.vcs.CredentialsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class ArtemisJSBridge implements ArtemisBridge {
    private static final Logger LOG = LoggerFactory.getLogger(ArtemisJSBridge.class);

    private final Project myProject;

    public ArtemisJSBridge(Project project) {
        this.myProject = project;
    }

    @Override
    public void clone(String repository, String exerciseName, int exerciseId, int courseId) {
        ArtemisGitUtil.Companion.clone(myProject, repository, exerciseName);
        ServiceManager.getService(myProject, ArtemisRouterService.class).onNewExercise(exerciseName, exerciseId, courseId);
    }

    @Override
    public void addCommitAndPushAllChanges() {
        final Collection<VirtualFile> changes = ArtemisGitUtil.Companion.getAllUntracked(myProject);
        ArtemisGitUtil.Companion.addAll(myProject, changes);
        ArtemisGitUtil.Companion.commitAll(myProject);
        ArtemisGitUtil.Companion.push(myProject);
    }

    @Override
    public void login(String username, String password) {
        ServiceManager.getService(CredentialsService.class).storeGitCredentials(username, password);
    }

    @Override
    public void log(String message) {
        LOG.debug(message);
    }
}
