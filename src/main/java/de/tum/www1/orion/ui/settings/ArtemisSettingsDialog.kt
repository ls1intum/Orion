package de.tum.www1.orion.ui.settings

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.panel
import javax.swing.JComponent
import javax.swing.JPanel

class ArtemisSettingsDialog : DialogWrapper(null) {
    private lateinit var settingsPanel: JPanel
    lateinit var artemisUrl: String
    lateinit var projectPath: String

    init {
        title = "OrION settings"
        init()
        isOKActionEnabled = true
    }

    override fun createCenterPanel(): JComponent? {
        val settings = ServiceManager.getService(ArtemisSettingsProvider::class.java)
        val currentArtemisUrl = settings.getSetting(ArtemisSettingsProvider.KEYS.ARTEMIS_URL)
        val currentProjectPath = settings.getSetting(ArtemisSettingsProvider.KEYS.PROJECT_BASE_DIR)
        artemisUrl = currentArtemisUrl
        projectPath = currentProjectPath
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
                textFieldWithBrowseButton(
                        "ArTEMiS project path",
                        currentProjectPath,
                        null,
                        FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                        { vf -> projectPath = vf.path; projectPath }
                )
            }
        }

        return settingsPanel
    }
}
