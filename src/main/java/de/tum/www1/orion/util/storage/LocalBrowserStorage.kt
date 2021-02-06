package de.tum.www1.orion.util.storage

import de.tum.www1.orion.util.PasswordSafeUtils

class LocalBrowserStorage : LocalBrowserStorageProvider {

    override fun storeSecure(key: String, value: String) {
        PasswordSafeUtils.storeCredentials(key, value, SYS, javaClass)
    }

    override fun retrieveSecureValue(key: String): String =
        PasswordSafeUtils.getCredentials(key, SYS, javaClass)?.password.toString()

    override fun clear(key: String) {
        PasswordSafeUtils.removeCredentials(key, SYS, javaClass)
    }

    private companion object {
        const val SYS = "Orion Browser Storage"
    }
}
