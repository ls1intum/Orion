package de.tum.www1.orion.ui.util

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.layout.panel
import de.tum.www1.orion.util.OrionSettingsProvider
import de.tum.www1.orion.util.appService
import de.tum.www1.orion.util.settings.OrionBundle
import javax.swing.JComponent
import javax.swing.JPanel

class ImportPathChooser(project: Project?) : DialogWrapper(project) {
    private lateinit var pathChooserPanel: JPanel
    private lateinit var chosenPathField: TextFieldWithBrowseButton
    val chosenPath: String
        get() = chosenPathField.text

    init {
        title = translate("orion.dialog.pathchooser.title")
        init()
        isOKActionEnabled = true
    }

    override fun createCenterPanel(): JComponent? {
        val settings = appService(OrionSettingsProvider::class.java)
        val currentProjectPath = settings.getSetting(OrionSettingsProvider.KEYS.PROJECT_BASE_DIR)

        pathChooserPanel = panel {
            row {
                label("Where do you want to save the imported exercise?")
            }
            row {
                chosenPathField = textFieldWithBrowseButton("Select a directory",
                        currentProjectPath,
                        null,
                        FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                        { it.path }
                )
            }
        }

        return pathChooserPanel
    }

    private fun translate(key: String) = OrionBundle.message(key)
}