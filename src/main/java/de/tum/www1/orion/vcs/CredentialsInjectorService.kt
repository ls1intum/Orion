package de.tum.www1.orion.vcs

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe

class CredentialsInjectorService : CredentialsService {

    override fun storeGitCredentials(username: String, password: String) {
        makeKeys(username)
                .map { Pair(CredentialAttributes(generateServiceName("Git HTTP", it), it, CredentialsInjectorService::class.java),
                        Credentials(it, password)) }
                .forEach { PasswordSafe.instance.set(it.first, it.second) }
    }

    override fun removeGitCredentials(username: String) {
        getDefaultSafeAttributes(username).forEach {
            PasswordSafe.instance.set(it, null)
        }
    }

    private fun getDefaultSafeAttributes(username: String): List<CredentialAttributes> {
        return makeKeys(username)
                .map { Pair(generateServiceName("Git HTTP", it), it) }
                .map { CredentialAttributes(it.first, it.second, CredentialsInjectorService::class.java) }
                .toList()
    }

    private fun makeKeys(username: String): List<String> {
        return listOf(NEW_REPO_HOST.format(username), OLD_REPO_HOST.format(username))
    }

    companion object {
        private const val NEW_REPO_HOST = "http://%s@bitbucket.ase.in.tum.de"
        private const val OLD_REPO_HOST = "http://%s@repobruegge.in.tum.de"
    }
}
