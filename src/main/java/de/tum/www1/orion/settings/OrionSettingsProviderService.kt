package de.tum.www1.orion.settings

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.ProjectManager
import de.tum.www1.orion.settings.OrionSettingsProvider.KEYS.ARTEMIS_URL
import de.tum.www1.orion.settings.OrionSettingsProvider.KEYS.USER_AGENT
import de.tum.www1.orion.ui.browser.BrowserUIInitializationService
import de.tum.www1.orion.util.appService

class OrionSettingsProviderService : OrionSettingsProvider {
    private val properties: PropertiesComponent
            get() = PropertiesComponent.getInstance()

    override fun saveSetting(key: OrionSettingsProvider.KEYS, setting: String) {
        // Reload page if URL changed
        if ((key == ARTEMIS_URL || key == USER_AGENT) && getSetting(key) != setting) {
            properties.setValue(key.toString(), setting)
            appService(ProjectManager::class.java).openProjects.forEach { project ->
                project.service<BrowserUIInitializationService>().init()
            }
            return
        }

        properties.setValue(key.toString(), setting)
    }

    override fun saveSettings(settings: MutableMap<OrionSettingsProvider.KEYS, String>) {
        settings.forEach { saveSetting(it.key, it.value) }
    }

    override fun getSetting(key: OrionSettingsProvider.KEYS): String {
        return properties.getValue(key.toString()) ?: key.defaultValue
    }

    override fun isModified(settings: Map<OrionSettingsProvider.KEYS, String>): Boolean {
        return settings.any { properties.getValue(it.key.toString()) != it.value }
    }
}
