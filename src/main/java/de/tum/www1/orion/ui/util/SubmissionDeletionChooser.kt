package de.tum.www1.orion.ui.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.panel
import de.tum.www1.orion.util.translate
import javax.swing.JComponent

/**
 * Informs the user of the deletion of the current submission and allow to cancel the operation
 *
 * @param project project to show the dialog in
 */
class SubmissionDeletionChooser(project: Project?) : DialogWrapper(project) {

    init {
        title = translate("orion.dialog.submissiondeletion.title")
        init()
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            row {
                label(
                    translate("orion.dialog.submissiondeletion.label"),
                    bold = true
                )
            }
        }
    }
}