package de.tum.www1.orion.ui.assessment

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.collaboration.ui.codereview.diff.EditorComponentInlaysManager
import de.tum.www1.orion.dto.Feedback
import de.tum.www1.orion.exercise.OrionAssessmentService
import de.tum.www1.orion.util.translate
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.*
import javax.swing.border.EmptyBorder

/**
 * An Inline feedback comment. Works fairly independently, forwards relevant changes to the [OrionAssessmentService]
 *
 * @property feedback that is shown by the comment or null if the comment is creating a new feedback
 * @property relativePath of the file the comment is shown it, required to generate new feedback
 * @property line the comment is shown in, required to generate new feedback
 * @param inlaysManager of the editor the comment should be shown in; the comment creates and shows itself to obtain its own disposer
 */
class InlineAssessmentComment(
    private var feedback: Feedback?,
    private val relativePath: String,
    private val line: Int,
    inlaysManager: EditorComponentInlaysManager
) {
    // status of the comment
    private var isEditable: Boolean = feedback == null
        set(value) {
            field = value
            updateGui()
        }
    private val disposer: Disposable?
    private val project: Project
    private val coloredComponentList: List<JComponent>

    var component: JComponent = JPanel()
    private var textArea: JTextArea = JTextArea(2, 0)
    private var spinner: JSpinner = JSpinner()
    private var buttonBar: JPanel = JPanel()

    private var editButton: JButton =
        createButton(translate("orion.exercise.assessment.edit"), AllIcons.Actions.Edit, this::edit)
    private var cancelButton: JButton =
        createButton(translate("orion.exercise.assessment.cancel"), AllIcons.Actions.Cancel, this::cancel)
    private var saveButton: JButton =
        createButton(translate("orion.exercise.assessment.save"), AllIcons.Actions.MenuSaveall, this::save)
    private var deleteButton: JButton =
        createButton(translate("orion.exercise.assessment.delete"), AllIcons.Actions.GC, this::delete)

    init {
        spinner.model = SpinnerNumberModel(0.0, null, null, 0.5)
        spinner.addChangeListener {
            updateColor()
        }

        // TextAreas don't have a border by default, wrap into an extra panel to get one
        val textPanel = JPanel()
        textPanel.border = EmptyBorder(4, 4, 4, 4)
        textPanel.layout = BorderLayout()
        textPanel.add(textArea, BorderLayout.CENTER)

        component.layout = BorderLayout()
        component.add(textPanel, BorderLayout.CENTER)
        component.add(spinner, BorderLayout.EAST)
        component.add(buttonBar, BorderLayout.SOUTH)

        project = inlaysManager.editor.project!!

        coloredComponentList =
            listOf(component, textPanel, spinner, buttonBar, editButton, saveButton, deleteButton, cancelButton)

        resetValues()
        updateGui()
        updateColor()
        disposer = inlaysManager.insertAfter(line, component)
    }

    private fun updateGui() {
        if (isEditable) {
            textArea.isEditable = true
            spinner.isEnabled = true

            buttonBar.removeAll()
            buttonBar.add(cancelButton)
            if (feedback != null) {
                buttonBar.add(deleteButton)
            }
            buttonBar.add(saveButton)
        } else {
            textArea.isEditable = false
            spinner.isEnabled = false

            buttonBar.removeAll()
            buttonBar.add(editButton)
        }
        component.repaint()
    }

    private fun resetValues() {
        textArea.text = feedback?.detailText ?: ""
        spinner.value = feedback?.credits ?: 0
    }

    private fun edit() {
        isEditable = true
    }

    private fun cancel() {
        if (feedback != null) {
            resetValues()
            isEditable = false
        } else {
            disposer?.let {
                Disposer.dispose(it)
            }
        }
    }

    private fun delete() {
        feedback?.let {
            project.service<OrionAssessmentService>().deleteFeedback(it)
        }
        disposer?.let {
            Disposer.dispose(it)
        }
    }

    private fun save() {
        // for unknown reasons the spinner value is an integer if it is not changed, requiring this parsing
        val spinnerValue = spinner.value.toString().toDouble()
        if (feedback != null) {
            feedback?.let {
                it.credits = spinnerValue
                it.detailText = textArea.text
            }
            project.service<OrionAssessmentService>().updateFeedback()
        } else {
            val newFeedback = Feedback(
                spinnerValue,
                textArea.text,
                "file:${relativePath}_line:$line",
                "File $relativePath at line ${line + 1}",
                "MANUAL",
                line,
                relativePath
            )
            feedback = newFeedback
            project.service<OrionAssessmentService>().addFeedback(newFeedback)
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
