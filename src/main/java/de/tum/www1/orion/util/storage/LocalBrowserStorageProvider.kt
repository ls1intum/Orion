package de.tum.www1.orion.util.storage

import com.intellij.openapi.components.ServiceManager

interface LocalBrowserStorageProvider {
    fun storeSecure(key: String, value: String)

    fun retrieveSecureValue(key: String): String?

    fun clear(key: String)

    companion object {
        @JvmStatic
        val instance: LocalBrowserStorageProvider
            get() = ServiceManager.getService(LocalBrowserStorageProvider::class.java)
    }
}
