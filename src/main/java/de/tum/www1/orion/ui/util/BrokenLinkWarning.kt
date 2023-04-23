package de.tum.www1.orion.ui.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.panel
import de.tum.www1.orion.util.translate
import javax.swing.JComponent

/**
 * Offers the user to relink an exercise to the global Orion registry
 *
 * @param project project to show the dialog in
 */
class BrokenLinkWarning(project: Project?) : DialogWrapper(project) {

    init {
        title = translate("orion.dialog.brokenlink.title")
        setOKButtonText("Yes")
        setCancelButtonText("No")
        init()
        isOKActionEnabled = true
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            row {
                label(
                    translate("orion.dialog.brokenlink.label")).bold()
            }
            row { label(translate("orion.dialog.brokenlink.question")) }
        }
    }
}