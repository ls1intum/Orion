package de.tum.www1.orion.ui.settings

import com.intellij.openapi.components.service
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.io.toNioPathOrNull
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import de.tum.www1.orion.settings.OrionSettingsProvider
import de.tum.www1.orion.ui.browser.BrowserUIInitializationService
import de.tum.www1.orion.util.translate
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * Provides the UI element to edit the plugin settings, shown at Tools -> Orion.
 * Settings are managed by [OrionSettingsProvider]
 *
 * @property project project the settings ui belongs to
 */
class OrionPluginSettings(private val project: Project) : SearchableConfigurable {
    private lateinit var settingsPanel: JPanel
    private lateinit var projectPathField: TextFieldWithBrowseButton
    private lateinit var instructorPathField: TextFieldWithBrowseButton
    private lateinit var tutorPathField: TextFieldWithBrowseButton
    private lateinit var artemisUrlField: JTextField
    private lateinit var artemisGitUrlField: JTextField
    private lateinit var commitMessageField: JTextField
    private lateinit var useDefaultBox: JCheckBox
    private lateinit var userAgentField: JTextField
    private val settings: Map<OrionSettingsProvider.KEYS, String>
        get() = mapOf(
            Pair(OrionSettingsProvider.KEYS.ARTEMIS_URL, artemisUrlField.text.removeSuffix("/")),
            Pair(OrionSettingsProvider.KEYS.ARTEMIS_GIT_URL, artemisGitUrlField.text.removeSuffix("/")),
            Pair(OrionSettingsProvider.KEYS.PROJECT_BASE_DIR, projectPathField.text),
            Pair(OrionSettingsProvider.KEYS.TUTOR_BASE_DIR, tutorPathField.text),
            Pair(OrionSettingsProvider.KEYS.INSTRUCTOR_BASE_DIR, instructorPathField.text),
            Pair(OrionSettingsProvider.KEYS.COMMIT_MESSAGE, commitMessageField.text),
            Pair(OrionSettingsProvider.KEYS.USE_DEFAULT, useDefaultBox.isSelected.toString()),
            Pair(OrionSettingsProvider.KEYS.USER_AGENT, userAgentField.text)
        )

    override fun isModified(): Boolean = service<OrionSettingsProvider>().isModified(settings)

    override fun getId(): String {
        return "de.tum.www1.orion.ui.settings"
    }

    override fun getDisplayName(): String {
        return ""
    }

    override fun apply() {
        service<OrionSettingsProvider>().saveSettings(settings)
    }

    override fun createComponent(): JComponent {
        val settings = service<OrionSettingsProvider>()
        val currentArtemisUrl = settings.getSetting(OrionSettingsProvider.KEYS.ARTEMIS_URL)
        val currentArtemisGitUrl = settings.getSetting(OrionSettingsProvider.KEYS.ARTEMIS_GIT_URL)
        val currentProjectPath = settings.getSetting(OrionSettingsProvider.KEYS.PROJECT_BASE_DIR)
        val currentTutorPath = settings.getSetting(OrionSettingsProvider.KEYS.TUTOR_BASE_DIR)
        val currentInstructorPath = settings.getSetting(OrionSettingsProvider.KEYS.INSTRUCTOR_BASE_DIR)
        val currentCommitMessage = settings.getSetting(OrionSettingsProvider.KEYS.COMMIT_MESSAGE)
        val currentUseDefault = settings.getSetting(OrionSettingsProvider.KEYS.USE_DEFAULT)
        settingsPanel = panel {
            row {
                label(translate("orion.settings.url.title")).bold()
            }
            row {
                label(translate("orion.settings.url.label"))
            }
            row {
                artemisUrlField = textField().bindText({ currentArtemisUrl }, {}).align(Align.FILL).component
            }
            row {
                label(translate("orion.settings.giturl.label"))
            }
            row {
                artemisGitUrlField = textField().bindText({ currentArtemisGitUrl }, {}).align(Align.FILL).component
            }
            row {
                label(translate("orion.settings.path.title")).bold()
            }
            row {
                label(translate("orion.settings.path.label"))
            }
            row {
                projectPathField = textFieldWithBrowseButton(
                    translate("orion.settings.path.browser.title"),
                    null,
                    FileChooserDescriptorFactory.createSingleFolderDescriptor()
                ).bindText({ currentProjectPath }) { it.toNioPathOrNull() }.align(Align.FILL).component
            }
            row {
                label(translate("orion.settings.tutorPath.label"))
            }
            row {
                tutorPathField = textFieldWithBrowseButton(
                    translate("orion.settings.tutorPath.browser.title"),
                    null,
                    FileChooserDescriptorFactory.createSingleFolderDescriptor()
                ).bindText({ currentTutorPath }) { it.toNioPathOrNull() }.align(Align.FILL).component
            }
            row {
                label(translate("orion.settings.instructorPath.label"))
            }
            row {
                instructorPathField = textFieldWithBrowseButton(
                    translate("orion.settings.instructorPath.browser.title"),
                    null,
                    FileChooserDescriptorFactory.createSingleFolderDescriptor()
                ).bindText({ currentInstructorPath }) { it.toNioPathOrNull() }.align(Align.FILL).component
            }
            row {
                label(translate("orion.settings.commit.message.title")).bold()
            }
            row {
                commitMessageField = textField().bindText({ currentCommitMessage }, {}).component
            }
            row {
                useDefaultBox = checkBox(
                    translate("orion.settings.commit.message.label")
                ).bindSelected(
                    { currentUseDefault.toBoolean() }, {}).component
            }
            row {
                label(translate("orion.settings.browser.debugActions")).bold()
            }
            row {
                label("User Agent")
                button("Reset") {
                    userAgentField.text = OrionSettingsProvider.KEYS.USER_AGENT.defaultValue
                }
                userAgentField =
                    textField().bindText(
                        { settings.getSetting(OrionSettingsProvider.KEYS.USER_AGENT) },
                        {}).component
            }
            row {
                button(translate("orion.settings.browser.button.reload")) {
                    project.service<BrowserUIInitializationService>().init()
                }

            }
        }
        artemisUrlField.toolTipText = translate("orion.settings.url.tooltip")
        artemisGitUrlField.toolTipText = translate("orion.settings.url.tooltip")

        return settingsPanel
    }
}
