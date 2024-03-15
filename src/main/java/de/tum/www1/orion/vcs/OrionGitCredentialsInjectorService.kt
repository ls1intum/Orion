package de.tum.www1.orion.vcs

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.components.service
import de.tum.www1.orion.settings.OrionSettingsProvider

class OrionGitCredentialsInjectorService : OrionGitCredentialsService {

    override fun storeGitCredentials(username: String, password: String) {
        val artemisGitUrl = makeKey(username)
        PasswordSafe.instance.set(
            CredentialAttributes(
                generateServiceName("Git HTTP", artemisGitUrl),
                artemisGitUrl,
                OrionGitCredentialsInjectorService::class.java
            ),
            Credentials(artemisGitUrl, password)
        )
    }

    override fun removeGitCredentials(username: String) {
        PasswordSafe.instance.set(getDefaultSafeAttributes(username), null)
    }

    private fun getDefaultSafeAttributes(username: String): CredentialAttributes {
        val artemisGitUrl = makeKey(username)
        return CredentialAttributes(
            generateServiceName("Git HTTP", artemisGitUrl),
            artemisGitUrl,
            OrionGitCredentialsInjectorService::class.java
        )
    }

    private fun makeKey(username: String): String {
        var url = service<OrionSettingsProvider>().getSetting(OrionSettingsProvider.KEYS.ARTEMIS_GIT_URL)
        url = if (!url.contains("://")) {
            "https://$username@$url"
        } else {
            url.replaceFirst("://", "://$username@")
        }
        return url
    }

}
