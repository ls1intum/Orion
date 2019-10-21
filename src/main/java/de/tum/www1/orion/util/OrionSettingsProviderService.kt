package de.tum.www1.orion.util

import com.intellij.ide.util.PropertiesComponent
import de.tum.www1.orion.ui.browser.Browser

class OrionSettingsProviderService : OrionSettingsProvider {
    private val properties: PropertiesComponent
            get() = PropertiesComponent.getInstance()

    override fun saveSetting(key: OrionSettingsProvider.KEYS, setting: String) {
        properties.setValue(key.toString(), setting)
    }

    override fun saveSettings(settings: MutableMap<OrionSettingsProvider.KEYS, String>) {
        settings.forEach { saveSetting(it.key, it.value) }

        // Reload website, etc.
        Browser.getInstance().init()
    }

    override fun getSetting(key: OrionSettingsProvider.KEYS): String = properties.getValue(key.toString(), key.defaultValue)

    override fun isModified(settings: Map<OrionSettingsProvider.KEYS, String>): Boolean {
        return settings.any { properties.getValue(it.key.toString()) != it.value }
    }
}
