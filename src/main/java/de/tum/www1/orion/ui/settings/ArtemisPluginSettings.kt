package de.tum.www1.orion.ui.settings

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.layout.panel
import de.tum.www1.orion.util.ArtemisSettingsProvider
import javax.swing.JComponent
import javax.swing.JPanel

class ArtemisPluginSettings(private val project: Project) : SearchableConfigurable {
    private lateinit var settingsPanel: JPanel
    private lateinit var projectPathField: TextFieldWithBrowseButton
    private lateinit var artemisUrl: String
    private val settings: Map<ArtemisSettingsProvider.KEYS, String>
        get() = mapOf(Pair(ArtemisSettingsProvider.KEYS.ARTEMIS_URL, artemisUrl),
                    Pair(ArtemisSettingsProvider.KEYS.PROJECT_BASE_DIR, projectPathField.text))

    override fun isModified(): Boolean = ServiceManager.getService(project, ArtemisSettingsProvider::class.java).isModified(settings)

    override fun getId(): String {
        return "de.tum.www1.orion.ui.settings";
    }

    override fun getDisplayName(): String {
        return "";
    }

    override fun apply() {
        ServiceManager.getService(project, ArtemisSettingsProvider::class.java).saveSettings(settings)
    }

    override fun createComponent(): JComponent? {
        val settings = ServiceManager.getService(ArtemisSettingsProvider::class.java)
        val currentArtemisUrl = settings.getSetting(ArtemisSettingsProvider.KEYS.ARTEMIS_URL)
        val currentProjectPath = settings.getSetting(ArtemisSettingsProvider.KEYS.PROJECT_BASE_DIR)
        artemisUrl = currentArtemisUrl
        settingsPanel = panel {
            row {
                label("ArTEMiS base URL:", bold = true)
            }
            row {
                label("The URL of the ArTEMiS homepage.\n" +
                        "Change this to change between different versions of ArTEMiS")
            }
            row {
                textField({ currentArtemisUrl }, { s -> artemisUrl = s })
            }
            row {
                label("ArTEMiS exercise path:", bold = true)
            }
            row {
                label("Where to store all imported exercises and projects.")
            }
            row {
                projectPathField = textFieldWithBrowseButton(
                        "ArTEMiS project path",
                        currentProjectPath,
                        null,
                        FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                        { it.path }
                )
            }
        }

        return settingsPanel
    }
}