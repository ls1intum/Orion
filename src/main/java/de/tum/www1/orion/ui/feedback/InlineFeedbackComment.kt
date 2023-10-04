package de.tum.www1.orion.ui.feedback

import com.intellij.collaboration.ui.codereview.diff.EditorComponentInlaysManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.fileTypes.FileTypes
import com.intellij.openapi.project.Project
import com.intellij.ui.EditorTextField
import de.tum.www1.orion.dto.Feedback
import de.tum.www1.orion.ui.util.ColorUtils
import de.tum.www1.orion.util.translate
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.border.TitledBorder

/**
 * A ui class that holds a not editable feedback comment
 */
class InlineFeedbackComment(
    private var feedback: Feedback,
    inlaysManager: EditorComponentInlaysManager
) {
    private var disposer: Disposable?
    private val project: Project
    private val coloredBackgroundComponentList: List<JComponent>
    private val coloredForegroundComponentList: List<Any>

    val component: JComponent = JPanel()
    private val textField: EditorTextField
    private val pointsTextField: EditorTextField
    private val buttonBar: JPanel = JPanel()

    init {
        project = inlaysManager.editor.project!!

        // the text field must be an [EditorTextField], otherwise important keys like enter or delete will not get forwarded by IntelliJ
        if (feedback.detailText != null) {
            textField = EditorTextField(feedback.detailText!!, project, FileTypes.PLAIN_TEXT)

        } else {
            textField = EditorTextField("", project, FileTypes.PLAIN_TEXT)

        }
        textField.isEnabled = false
        textField.setOneLineMode(false)
        textField.border = null

        // enter points
        pointsTextField = EditorTextField("  " + feedback.credits.toString() + "  ", project, FileTypes.PLAIN_TEXT)
        pointsTextField.isEnabled = false

        // create a border of the background color, so we don't have to set the color manually
        val textPanel = JPanel()
        textPanel.border = BorderFactory.createTitledBorder(
            BorderFactory.createEmptyBorder(25, 5, 5, 5),
            translate("orion.exercise.assessment.feedback")
        )
        textPanel.layout = BorderLayout()
        textPanel.add(textField.component, BorderLayout.CENTER)

        val pointsPanel = JPanel()
        pointsPanel.border = BorderFactory.createTitledBorder(
            BorderFactory.createEmptyBorder(25, 5, 5, 10),
            translate("orion.exercise.assessment.score")
        )
        pointsPanel.layout = BorderLayout()
        pointsPanel.add(pointsTextField, BorderLayout.CENTER)

        // create a for points
        val rightBar = JPanel()
        rightBar.isOpaque = false
        rightBar.layout = BoxLayout(rightBar, BoxLayout.LINE_AXIS)
        pointsPanel.alignmentX = Component.BOTTOM_ALIGNMENT
        rightBar.add(pointsPanel)

        buttonBar.isOpaque = false

        component.layout = BorderLayout()
        component.add(textPanel, BorderLayout.CENTER)
        component.add(rightBar, BorderLayout.EAST)
        component.add(buttonBar, BorderLayout.SOUTH)

        coloredBackgroundComponentList =
            listOf(
                component,
                textPanel,
                pointsPanel,
            )
        coloredForegroundComponentList = listOf(textPanel.border, pointsPanel.border)

        updateColor()
        disposer = inlaysManager.insertAfter(feedback.line!!, component)
    }

    private fun updateColor() {
        coloredBackgroundComponentList.forEach {
            it.background = ColorUtils.getFeedbackColor(feedback.credits)
        }
        coloredForegroundComponentList.forEach {
            (it as? TitledBorder)?.titleColor = ColorUtils.getFeedbackTextColor(feedback.credits)
            (it as? JComponent)?.foreground = ColorUtils.getFeedbackTextColor(feedback.credits)
        }
    }
}