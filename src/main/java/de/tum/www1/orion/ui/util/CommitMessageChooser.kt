package de.tum.www1.orion.ui.util

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.panel
import de.tum.www1.orion.settings.OrionBundle
import de.tum.www1.orion.settings.OrionSettingsProvider
import javax.swing.*

class CommitMessageChooser(val project: Project) :
    DialogWrapper(project) {
    private lateinit var commitMessagePanel: JPanel
    private lateinit var commitMessageField: JTextField
    private lateinit var commitMessageCheckbox: JCheckBox

    init {
        title = translate("orion.dialog.commitmessagechooser.title")
        init()
    }

    fun getCommitMessage(): String {
        val settings = service<OrionSettingsProvider>()
        var message = settings.getSetting(OrionSettingsProvider.KEYS.COMMIT_MESSAGE)

        if (!settings.getSetting(OrionSettingsProvider.KEYS.USE_DEFAULT).toBoolean()) {
            if (showAndGet()) {
                message = commitMessageField.text

                if (commitMessageCheckbox.isSelected) {
                    settings.saveSetting(OrionSettingsProvider.KEYS.COMMIT_MESSAGE, message)
                    settings.saveSetting(OrionSettingsProvider.KEYS.USE_DEFAULT, true.toString())
                }
            }

            return message
        }

        return message
    }

    override fun createCenterPanel(): JComponent {
        commitMessagePanel = panel {
            row {
                label("Chose a commit message")
            }
            row {
                commitMessageField = textField(
                    { "" },
                    {}
                ).component
            }
            row {
                commitMessageCheckbox = checkBox(
                    translate("orion.settings.commit.message.label"),
                    { false },
                    {}
                ).component
            }
        }

        return commitMessagePanel
    }

    override fun createActions(): Array<Action> {
        return arrayOf(okAction)
    }

    private fun translate(key: String) = OrionBundle.message(key)
}