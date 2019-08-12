package de.tum.www1.artemis.plugin.intellij.vcs.bridge;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import de.tum.www1.artemis.plugin.intellij.vcs.ArtemisGitUtil;
import de.tum.www1.artemis.plugin.intellij.vcs.CredentialsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArtemisJSBridge implements ArtemisBridge {
    private static final Logger LOG = LoggerFactory.getLogger(ArtemisJSBridge.class);

    private final Project myProject;

    public ArtemisJSBridge(Project project) {
        this.myProject = project;
    }

    @Override
    public void clone(String repository, String exerciseName) {
        ArtemisGitUtil.Companion.clone(myProject, repository, exerciseName);
    }

    @Override
    public void login(String username, String password) {
        ServiceManager.getService(CredentialsService.class).storeGitCredentials(username, password);
    }

    public void log(String message) {
        LOG.debug(message);
    }
}
