package de.tum.www1.orion.ui.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.panel
import de.tum.www1.orion.util.translate
import javax.swing.JComponent

/**
 * Requests a yes/no confirmation with the title and description defined by the translationKey, expected to be orion.dialog.translationKey.title and .label
 *
 * @param project project to show the dialog in
 */
class YesNoChooser(project: Project, private val translationKey: String) : DialogWrapper(project) {
    init {
        title = translate("orion.dialog.$translationKey.title")
        init()
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            row {
                label(
                    translate("orion.dialog.$translationKey.label"),
                    bold = true
                )
            }
        }
    }
}
