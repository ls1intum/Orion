package de.tum.www1.artemis.plugin.intellij.vcs.impl;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.credentialStore.OneTimeString;
import com.intellij.ide.passwordSafe.PasswordSafe;
import de.tum.www1.artemis.plugin.intellij.vcs.CredentialsService;
import org.jetbrains.annotations.NotNull;

public class CredentialsServiceImpl implements CredentialsService {
    private static final String CREDENTIALS_KEY = "http://%s@repobruegge.in.tum.de";

    @Override
    public void storeGitCredentials(String username, OneTimeString password) {
        final CredentialAttributes attributes = getDefaultSafeAttributes(username);
        final Credentials credentials = new Credentials(username, password);

        PasswordSafe.getInstance().set(attributes, credentials);
    }

    @Override
    public void removeGitCredentials(String username) {
        final CredentialAttributes attributes = getDefaultSafeAttributes(username);

        PasswordSafe.getInstance().set(attributes, null);
    }

    @NotNull
    private CredentialAttributes getDefaultSafeAttributes(String username) {
        final String key = String.format(CREDENTIALS_KEY, username);
        final String serviceName = CredentialAttributesKt.generateServiceName("Git HTTP", key);
        return new CredentialAttributes(serviceName, key, CredentialsServiceImpl.class);
    }
}
