package de.tum.www1.orion.settings

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.ProjectManager
import de.tum.www1.orion.settings.OrionSettingsProvider.KEYS.ARTEMIS_URL
import de.tum.www1.orion.settings.OrionSettingsProvider.KEYS.USER_AGENT
import de.tum.www1.orion.ui.browser.Browser
import de.tum.www1.orion.util.appService

class OrionSettingsProviderService : OrionSettingsProvider {
    private val properties: PropertiesComponent
            get() = PropertiesComponent.getInstance()

    override fun saveSetting(key: OrionSettingsProvider.KEYS, setting: String) {
        // Reload page if URL changed
        if (key == ARTEMIS_URL && getSetting(ARTEMIS_URL) != setting || (key == USER_AGENT && getSetting(USER_AGENT) != setting)) {
            properties.setValue(key.toString(), setting)
            appService(ProjectManager::class.java).openProjects.forEach { project ->
                /*ToolWindowManager.getInstance(project).getToolWindow("Artemis")?.apply {
                    if (!isVisible) {
                        show(null)
                    }
                }*/
                project.service<Browser>().init()
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

    override fun initSettings() {
    }

    private fun initUserAgent() {
        val currentAgent = "Mozilla/5.0 (KHTML, like Gecko) JavaFX/10 Orion/1.0.1"
        saveSetting(USER_AGENT, currentAgent)
    }
}
