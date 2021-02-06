package de.tum.www1.orion.util

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe

object PasswordSafeUtils {

    fun storeCredentials(username: String, password: String, subsystem: String, requester: Class<*>? = null) {
        getDefaultSafeAttribute(username, subsystem, requester).also {
            PasswordSafe.instance.set(it, Credentials(username, password))
        }
    }

    fun removeCredentials(username: String, subsystem: String, requester: Class<*>?) {
        getDefaultSafeAttribute(username, subsystem, requester).also {
            PasswordSafe.instance.set(it, null)
        }
    }

    fun getCredentials(username: String, subsystem: String, requester: Class<*>?): Credentials? {
        return getDefaultSafeAttribute(username, subsystem, requester).let {
            PasswordSafe.instance.get(it)
        }
    }

    private fun getDefaultSafeAttribute(
        username: String,
        subsystem: String,
        requester: Class<*>?
    ): CredentialAttributes {
        return CredentialAttributes(generateServiceName(subsystem, username), username, requester)
    }
}