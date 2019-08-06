package de.tum.www1.artemis.plugin.intellij.vcs.impl

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.OneTimeString
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe
import de.tum.www1.artemis.plugin.intellij.vcs.CredentialsService

class CredentialsServiceImpl : CredentialsService {

    override fun storeGitCredentials(username: String, password: OneTimeString) {
        val attributes = getDefaultSafeAttributes(username)
        val credentials = Credentials(username, password)

        PasswordSafe.instance.set(attributes, credentials)
    }

    override fun removeGitCredentials(username: String) {
        val attributes = getDefaultSafeAttributes(username)

        PasswordSafe.instance.set(attributes, null)
    }

    private fun getDefaultSafeAttributes(username: String): CredentialAttributes {
        val key = CREDENTIALS_KEY.format(username)
        val serviceName = generateServiceName("Git HTTP", key)
        return CredentialAttributes(serviceName, key, CredentialsServiceImpl::class.java)
    }

    companion object {
        private const val CREDENTIALS_KEY = "http://%s@repobruegge.in.tum.de"
    }
}
