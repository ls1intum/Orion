package de.tum.www1.artemis.plugin.intellij.vcs;

import com.intellij.credentialStore.OneTimeString;
import com.intellij.openapi.components.ServiceManager;

public interface CredentialsService {
    static CredentialsService getInstance() {
        return ServiceManager.getService(CredentialsService.class);
    }

    void storeGitCredentials(String username, OneTimeString password);

    void removeGitCredentials(String username);
}
