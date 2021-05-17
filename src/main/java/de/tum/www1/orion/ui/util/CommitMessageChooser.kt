package de.tum.www1.orion.ui.util

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.panel
import de.tum.www1.orion.settings.OrionBundle
import de.tum.www1.orion.settings.OrionSettingsProvider
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * This class provides a dialog to choose a commit message
 */
class CommitMessageChooser(val project: Project) :
    DialogWrapper(project) {
    private lateinit var commitMessagePanel: JPanel
    private lateinit var commitMessageField: JTextField
    private val settings = service<OrionSettingsProvider>()

    init {
        title = translate("orion.dialog.commitmessagechooser.title")
        init()
    }

    /**
     * This method retrieves a default commit message if the setting is set, otherwise
     * the dialog gets shown
     *
     * @return the commit message or null on cancel
     */
    fun getCommitMessage(): String? {
        if (!settings.getSetting(OrionSettingsProvider.KEYS.USE_DEFAULT).toBoolean()) {
            if (showAndGet()) {
                return commitMessageField.text
            }

            return null
        }

        return settings.getSetting(OrionSettingsProvider.KEYS.COMMIT_MESSAGE)
    }

    override fun createCenterPanel(): JComponent {
        commitMessagePanel = panel {
            row {
                label("Chose a commit message")
            }
            row {
                commitMessageField = textField(
                    { settings.getSetting(OrionSettingsProvider.KEYS.COMMIT_MESSAGE) },
                    {}
                ).component
            }
        }

        setDoNotAskOption(DoNotAsk())

        return commitMessagePanel
    }

    private fun translate(key: String) = OrionBundle.message(key)

    /**
     * The doNotAsk again checkbox
     */
    inner class DoNotAsk : DoNotAskOption.Adapter() {
        override fun rememberChoice(isSelected: Boolean, exitCode: Int) {
            if (exitCode == OK_EXIT_CODE && isSelected) {
                settings.saveSetting(OrionSettingsProvider.KEYS.COMMIT_MESSAGE, commitMessageField.text)
                settings.saveSetting(OrionSettingsProvider.KEYS.USE_DEFAULT, true.toString())
            }
        }

        override fun getDoNotShowMessage(): String {
            return translate("orion.settings.commit.message.label")
        }
    }
}