package de.tum.www1.orion.util

import com.intellij.ide.util.PropertiesComponent

class ArtemisSettingsProviderService : ArtemisSettingsProvider {
    private val properties: PropertiesComponent
            get() = PropertiesComponent.getInstance()

    override fun saveSetting(key: ArtemisSettingsProvider.KEYS, setting: String) = properties.setValue(key.toString(), setting)

    override fun saveSettings(settings: MutableMap<ArtemisSettingsProvider.KEYS, String>) {
        settings.forEach { saveSetting(it.key, it.value) }
    }

    override fun getSetting(key: ArtemisSettingsProvider.KEYS): String = properties.getValue(key.toString(), key.defaultValue)

    override fun isModified(settings: Map<ArtemisSettingsProvider.KEYS, String>): Boolean {
        return settings.any { properties.getValue(it.key.toString()) != it.value }
    }
}
