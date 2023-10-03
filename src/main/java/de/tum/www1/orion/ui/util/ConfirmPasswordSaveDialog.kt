package de.tum.www1.orion.ui.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.panel
import javax.swing.JPanel

@Deprecated("Saving the password is now confirmed using a modal in the Angular app")
class ConfirmPasswordSaveDialog(project: Project?) : DialogWrapper(project) {
    private lateinit var confirmationPanel: JPanel

    init {
        title = "Import Credentials Into your IDE"
        init()
        isOKActionEnabled = true
    }

    override fun createCenterPanel(): JPanel {
        confirmationPanel = panel {
            row {
                label(
                    "Do you want to save your Artemis credentials in your IDE?\n" +
                            "This makes importing and submitting exercises a lot easier!"
                )
            }
        }

        return confirmationPanel
    }
}
