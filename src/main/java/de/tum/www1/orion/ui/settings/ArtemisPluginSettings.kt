package de.tum.www1.orion.ui.settings

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.layout.panel
import de.tum.www1.orion.util.ArtemisSettingsProvider
import de.tum.www1.orion.util.settings.OrionBundle
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class ArtemisPluginSettings(private val project: Project) : SearchableConfigurable {
    private lateinit var settingsPanel: JPanel
    private lateinit var projectPathField: TextFieldWithBrowseButton
    private lateinit var artemisUrlField: JTextField
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
                label(translate("orion.settings.url.title"), bold = true)
            }
            row {
                label(translate("orion.settings.url.label"))
            }
            row {
                artemisUrlField = textField({ currentArtemisUrl }, { s -> artemisUrl = s }).component
            }
            row {
                label(translate("orion.settings.path.title"), bold = true)
            }
            row {
                label(translate("orion.settings.path.label"))
            }
            row {
                projectPathField = textFieldWithBrowseButton(
                        translate("orion.settings.path.browser.title"),
                        currentProjectPath,
                        null,
                        FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                        { it.path }
                )
            }
        }

        artemisUrlField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(p0: DocumentEvent?) {
                artemisUrl = artemisUrlField.text
            }

            override fun removeUpdate(p0: DocumentEvent?) {
            }

            override fun changedUpdate(p0: DocumentEvent?) {
            }
        })

        return settingsPanel
    }

    private fun translate(key: String) = OrionBundle.message(key)
}