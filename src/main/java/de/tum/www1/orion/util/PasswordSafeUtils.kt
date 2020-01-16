package de.tum.www1.orion.util

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe

object PasswordSafeUtils {

    fun storeCredentials(username: String, password: String, subsystem: String, requestor: Class<*>? = null) {
        getDefaultSafeAttribute(username, subsystem, requestor).also {
            PasswordSafe.instance.set(it, Credentials(username, password))
        }
    }

    fun removeCredentials(username: String, subsystem: String, requestor: Class<*>?) {
        getDefaultSafeAttribute(username, subsystem, requestor).also {
            PasswordSafe.instance.set(it, null)
        }
    }

    fun getCredentials(username: String, subsystem: String, requestor: Class<*>?): Credentials? {
        return getDefaultSafeAttribute(username, subsystem, requestor).let {
            PasswordSafe.instance.get(it)
        }
    }

    private fun getDefaultSafeAttribute(username: String, subsystem: String, requestor: Class<*>?): CredentialAttributes {
        return CredentialAttributes(generateServiceName(subsystem, username), username, requestor)
    }
}