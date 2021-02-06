package de.tum.www1.orion.ui.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.panel
import javax.swing.JPanel

@Deprecated("Saving the password is now confirmed using a modal in the Angular app")
class ConfirmPasswordSaveDialog(project: Project?) : DialogWrapper(project) {
    private lateinit var confirmationPanel: JPanel

    init {
        title = "Import Credentials Into IntelliJ"
        init()
        isOKActionEnabled = true
    }

    override fun createCenterPanel(): JPanel {
        confirmationPanel = panel {
            row {
                label(
                    "Do you want to save your ArTEMiS credentials in IntelliJ?\n" +
                            "This makes importing and submitting exercises a lot easier!"
                )
            }
        }

        return confirmationPanel
    }

}
