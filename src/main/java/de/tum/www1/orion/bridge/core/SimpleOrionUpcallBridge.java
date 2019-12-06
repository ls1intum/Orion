package de.tum.www1.orion.bridge.core;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import de.tum.www1.orion.bridge.ArtemisConnector;
import de.tum.www1.orion.vcs.CredentialsService;

public abstract class SimpleOrionUpcallBridge extends ArtemisConnector implements ArtemisCoreUpcallBridge {
    private final Logger LOG = Logger.getInstance(this.getClass());

    protected final Project project;

    public SimpleOrionUpcallBridge(Project project) {
        this.project = project;
    }

    @Override
    public void login(String username, String password) {
        ServiceManager.getService(CredentialsService.class).storeGitCredentials(username, password);
    }

    @Override
    public void log(String message) {
        LOG.info(message);
    }
}
