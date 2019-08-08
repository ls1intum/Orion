package de.tum.www1.artemis.plugin.intellij.vcs

import com.intellij.openapi.components.ServiceManager

interface CredentialsService {

    fun storeGitCredentials(username: String, password: String)

    fun removeGitCredentials(username: String)

    companion object {
        @JvmStatic
        val instance: CredentialsService
            get() = ServiceManager.getService(CredentialsService::class.java)
    }
}
