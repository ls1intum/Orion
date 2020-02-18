package de.tum.www1.orion.connector.ide.shared;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import de.tum.www1.orion.connector.ide.OrionConnector;
import de.tum.www1.orion.vcs.CredentialsService;
import org.slf4j.LoggerFactory;

public class OrionSharedUtilConnector extends OrionConnector implements IOrionSharedUtilConnector {

    public OrionSharedUtilConnector(Project project) {
        super(project);
    }

    @Override
    public void login(String username, String password) {
        ServiceManager.getService(CredentialsService.class).storeGitCredentials(username, password);
    }

    @Override
    public void log(String message) {
        LoggerFactory.getLogger(OrionSharedUtilConnector.class).info(message);
    }
}
