package de.tum.www1.artemis.plugin.intellij.vcs

import com.intellij.credentialStore.OneTimeString
import com.intellij.openapi.components.ServiceManager

interface CredentialsService {

    fun storeGitCredentials(username: String, password: OneTimeString)

    fun removeGitCredentials(username: String)

    companion object {
        @JvmStatic
        val instance: CredentialsService
            get() = ServiceManager.getService(CredentialsService::class.java)
    }
}
