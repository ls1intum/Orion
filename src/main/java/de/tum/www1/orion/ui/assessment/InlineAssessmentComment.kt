package de.tum.www1.orion.ui.assessment

import com.google.gson.JsonSyntaxException
import com.intellij.collaboration.ui.codereview.diff.EditorComponentInlaysManager
import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileTypes.FileTypes
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.EditorTextField
import de.tum.www1.orion.dto.Feedback
import de.tum.www1.orion.dto.GradingInstruction
import de.tum.www1.orion.exercise.OrionAssessmentService
import de.tum.www1.orion.util.JsonUtils.gson
import de.tum.www1.orion.util.translate
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.*
import javax.swing.border.EmptyBorder

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

    // gradingInstruction of the feedback is exposed for the [StructuredGradingInstructionLink]
    var gradingInstruction: GradingInstruction?
        get() = feedback.gradingInstruction
        set(value) {
            feedback.gradingInstruction = value
            if (value != null) {
                textField.text = value.feedback
                spinner.value = value.credits
            }
            updateGui()
        }

    private val disposer: Disposable?
    private val project: Project
    private val coloredComponentList: List<JComponent>

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

        project = inlaysManager.editor.project!!

        // the text field must be an [EditorTextField], otherwise important keys like enter or delete will not get forwarded by IntelliJ
        textField = EditorTextField("", project, FileTypes.PLAIN_TEXT)
        textField.setOneLineMode(false)
        textField.border = null

        // only initialize the link now to avoid exceptions trying to set a not initialized textField
        gradingInstructionLink = StructuredGradingInstructionLink(this)

        // listener to handle dropping of structured grading instructions
        // for any inserted string it tries to parse it as a sgi. If it fails, nothing is done. If it succeeds, the sgi is handled
        textField.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                val newFragment = event.newFragment
                // basic performance improvement, don't try to parse if its hopeless
                if (newFragment.startsWith('{') && newFragment.endsWith('}')) {
                    try {
                        val instruction = gson().fromJson(newFragment.toString(), GradingInstruction::class.java)
                        // listeners should not change the text, queue the change instead
                        ApplicationManager.getApplication().invokeLater {
                            gradingInstruction = instruction
                            gradingInstructionLink.updateGui()
                        }
                    } catch (e: JsonSyntaxException) {
                        // ignore, if the parse failed, nothing needs to be done
                    }
                }
            }
        })

        // create a border of the background color, so we don't have to set the color manually
        val textPanel = JPanel()
        textPanel.border = EmptyBorder(4, 4, 4, 4)
        textPanel.layout = BorderLayout()
        textPanel.add(textField.component, BorderLayout.CENTER)

        // create a panel for the spinner and gradingInstructionLink
        val rightBar = JPanel()
        rightBar.layout = BorderLayout()
        rightBar.add(spinner, BorderLayout.CENTER)
        rightBar.add(gradingInstructionLink.component, BorderLayout.EAST)

        component.layout = BorderLayout()
        component.add(textPanel, BorderLayout.CENTER)
        component.add(rightBar, BorderLayout.EAST)
        component.add(buttonBar, BorderLayout.SOUTH)

        coloredComponentList =
            listOf(
                component,
                textPanel,
                spinner,
                buttonBar,
                rightBar,
                gradingInstructionLink.component,
                editButton,
                saveButton,
                deleteButton,
                cancelButton
            )

        resetValues()
        updateGui()
        updateColor()
        disposer = inlaysManager.insertAfter(feedback.line!!, component)
    }

    private fun updateGui() {
        textField.isViewer = !isEditable
        spinner.isEnabled = isEditable && (gradingInstruction?.usageCount ?: 0) == 0
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
        component.repaint()
    }

    private fun resetValues() {
        textField.text = feedback.detailText
        spinner.value = feedback.credits
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
        val color = when {
            spinnerValue > 0 -> Color(0xd4edda)
            spinnerValue < 0 -> Color(0xf8d7da)
            else -> Color(0xfff3cd)
        }

        coloredComponentList.forEach {
            it.background = color
        }
    }
}
