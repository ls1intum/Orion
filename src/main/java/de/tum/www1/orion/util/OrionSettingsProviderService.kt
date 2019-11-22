package de.tum.www1.orion.util

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import de.tum.www1.orion.ui.browser.Browser
import de.tum.www1.orion.util.OrionSettingsProvider.KEYS.ARTEMIS_URL

class OrionSettingsProviderService : OrionSettingsProvider {
    private val properties: PropertiesComponent
            get() = PropertiesComponent.getInstance()

    override fun saveSetting(project: Project, key: OrionSettingsProvider.KEYS, setting: String) {
        // Reload page if URL changed
        if (key == ARTEMIS_URL && getSetting(ARTEMIS_URL) != setting) {
            properties.setValue(key.toString(), setting)
            ToolWindowManager.getInstance(project).getToolWindow("Artemis").apply {
                if (!isVisible) {
                    show(null)
                }
            }
            Browser.getInstance().init()
            return
        }

        properties.setValue(key.toString(), setting)
    }

    override fun saveSettings(project: Project, settings: MutableMap<OrionSettingsProvider.KEYS, String>) {
        settings.forEach { saveSetting(project, it.key, it.value) }
    }

    override fun getSetting(key: OrionSettingsProvider.KEYS): String = properties.getValue(key.toString(), key.defaultValue)

    override fun isModified(settings: Map<OrionSettingsProvider.KEYS, String>): Boolean {
        return settings.any { properties.getValue(it.key.toString()) != it.value }
    }
}
