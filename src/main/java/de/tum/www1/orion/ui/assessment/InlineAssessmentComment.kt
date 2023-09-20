package de.tum.www1.orion.ui.assessment

import com.intellij.collaboration.ui.codereview.diff.EditorComponentInlaysManager
import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.fileTypes.FileTypes
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.EditorTextField
import com.intellij.ui.JBColor
import de.tum.www1.orion.dto.Feedback
import de.tum.www1.orion.exercise.OrionAssessmentService
import de.tum.www1.orion.util.translate
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import javax.swing.*
import javax.swing.border.TitledBorder

/**
 * An Inline assessment feedback comment. Works fairly independently, forwards relevant changes to the [OrionAssessmentService]
 *
 * @property feedback that is shown by the comment. To create a new comment, use [OrionAssessmentService.addFeedbackCommentIfPossible]
 * @param inlaysManager of the editor the comment should be shown in; the comment creates and shows itself to obtain its own disposer
 */
class InlineAssessmentComment(
    private var feedback: Feedback,
    inlaysManager: EditorComponentInlaysManager
) {
    private var newFeedback = false
    private var isEditable: Boolean = false
        set(value) {
            field = value
            updateGui()
        }

    private val disposer: Disposable?
    private val project: Project
    private val coloredBackgroundComponentList: List<JComponent>
    private val coloredForegroundComponentList: List<Any>

    val component: JComponent = JPanel()
    private val textField: EditorTextField
    private val spinner: JSpinner = JSpinner()
    private val buttonBar: JPanel = JPanel()
    private val gradingInstructionLink: StructuredGradingInstructionLink

    private val editButton: JButton =
        createButton(translate("orion.exercise.assessment.edit"), AllIcons.Actions.Edit, this::edit)
    private val cancelButton: JButton =
        createButton(translate("orion.exercise.assessment.cancel"), AllIcons.Actions.Cancel, this::cancel)
    private val saveButton: JButton =
        createButton(translate("orion.exercise.assessment.save"), AllIcons.Actions.MenuSaveall, this::save)
    private val deleteButton: JButton =
        createButton(translate("orion.exercise.assessment.delete"), AllIcons.Actions.GC, this::delete)

    /**
     * Secondary constructor to create a new comment. Creates a feedback with default values and sets [newFeedback] to true
     *
     * @property relativePath of the file the comment is shown it
     * @property line the comment is shown in
     */
    constructor(relativePath: String, line: Int, inlaysManager: EditorComponentInlaysManager) : this(
        Feedback(
            0.0,
            "",
            "file:${relativePath}_line:$line",
            "File $relativePath at line ${line + 1}",
            "MANUAL",
            null,
            line,
            relativePath
        ), inlaysManager
    ) {
        newFeedback = true
        isEditable = true
    }

    init {
        spinner.model = SpinnerNumberModel(0.0, null, null, 0.5)
        spinner.addChangeListener {
            updateColor()
        }
        spinner.dropTarget = null
        spinner.border = null

        project = inlaysManager.editor.project!!

        // the text field must be an [EditorTextField], otherwise important keys like enter or delete will not get forwarded by IntelliJ
        textField = EditorTextField("", project, FileTypes.PLAIN_TEXT)
        textField.setOneLineMode(false)
        textField.border = null

        // initialize components for Structured Grading Instructions
        val gradingInstructionLabel = JLabel()
        gradingInstructionLabel.toolTipText = translate("orion.exercise.assessment.sgi.tooltip")
        gradingInstructionLink = StructuredGradingInstructionLink(gradingInstructionLabel, spinner, this, textField)

        // create a border of the background color, so we don't have to set the color manually
        val textPanel = JPanel()
        textPanel.border = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(4, 1, 4, 2), translate("orion.exercise.assessment.feedback"))
        textPanel.layout = BorderLayout()
        textPanel.add(gradingInstructionLabel, BorderLayout.NORTH)
        textPanel.add(textField.component, BorderLayout.CENTER)

        val spinnerPanel = JPanel()
        spinnerPanel.border = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(4,1,4,2), translate("orion.exercise.assessment.score"))
        spinnerPanel.layout = BorderLayout()
        spinnerPanel.add(spinner, BorderLayout.CENTER)

        // create a panel for the spinner and gradingInstructionLink
        val rightBar = JPanel()
        rightBar.isOpaque = false
        rightBar.layout = BoxLayout(rightBar, BoxLayout.LINE_AXIS)
        spinnerPanel.alignmentX = Component.BOTTOM_ALIGNMENT
        gradingInstructionLink.component.alignmentX = Component.BOTTOM_ALIGNMENT
        rightBar.add(spinnerPanel)
        rightBar.add(gradingInstructionLink.component)

        buttonBar.isOpaque = false

        component.layout = BorderLayout()
        component.add(textPanel, BorderLayout.CENTER)
        component.add(rightBar, BorderLayout.EAST)
        component.add(buttonBar, BorderLayout.SOUTH)

        coloredBackgroundComponentList =
            listOf(
                component,
                textPanel,
                spinnerPanel,
                gradingInstructionLink.component,
                editButton,
                saveButton,
                deleteButton,
                cancelButton
            )
        coloredForegroundComponentList = listOf(textPanel.border, spinnerPanel.border, gradingInstructionLabel)

        resetValues()
        updateGui()
        updateColor()
        disposer = inlaysManager.insertAfter(feedback.line!!, component)
    }

    private fun updateGui() {
        textField.isViewer = !isEditable
        spinner.isEnabled = isEditable && gradingInstructionLink.gradingInstruction == null
        gradingInstructionLink.isEnabled = isEditable
        buttonBar.removeAll()

        if (isEditable) {
            buttonBar.add(cancelButton)
            if (!newFeedback) {
                buttonBar.add(deleteButton)
            }
            buttonBar.add(saveButton)
        } else {
            buttonBar.add(editButton)
        }
        component.revalidate()
        component.repaint()
    }

    private fun resetValues() {
        textField.text = feedback.detailText
        spinner.value = feedback.credits
        gradingInstructionLink.gradingInstruction = feedback.gradingInstruction
    }

    private fun edit() {
        isEditable = true
    }

    private fun cancel() {
        if (!newFeedback) {
            resetValues()
            isEditable = false
        } else {
            project.service<OrionAssessmentService>().deletePendingFeedback(feedback.path!!, feedback.line!!)
            disposer?.let {
                Disposer.dispose(it)
            }
        }
    }

    private fun delete() {
        project.service<OrionAssessmentService>().deleteFeedback(feedback)
        disposer?.let {
            Disposer.dispose(it)
        }
    }

    private fun save() {
        // for unknown reasons the spinner value is an integer if it is not changed, requiring this parsing
        val spinnerValue = spinner.value.toString().toDouble()
        feedback.credits = spinnerValue
        feedback.detailText = textField.text
        feedback.gradingInstruction = gradingInstructionLink.gradingInstruction
        if (!newFeedback) {
            project.service<OrionAssessmentService>().updateFeedback()
        } else {
            newFeedback = false
            project.service<OrionAssessmentService>().addFeedback(feedback)
        }
        isEditable = false
    }

    private fun createButton(label: String, icon: Icon, action: () -> Unit): JButton {
        val button = JButton(label, icon)
        button.addActionListener {
            action()
        }
        return button
    }

    private fun updateColor() {
        val spinnerValue = spinner.value.toString().toDouble()

        // colors are the same as in Artemis
        val color = when {
            spinnerValue > 0 -> JBColor(0xd4edda, 0x00231a)
            spinnerValue < 0 -> JBColor(0xf8d7da, 0x370b07)
            else -> JBColor(0xfff3cd, 0x362203)
        }
        val textColor = when {
            spinnerValue > 0 -> JBColor(0x186429, 0x8cb294)
            spinnerValue < 0 -> JBColor(0x842029, 0xc29094)
            else -> JBColor(0x664d03, 0xb3a681)
        }

        coloredBackgroundComponentList.forEach {
            it.background = color
        }
        coloredForegroundComponentList.forEach {
            (it as? TitledBorder)?.titleColor = textColor
            (it as? JComponent)?.foreground = textColor
        }
    }
}
