package de.tum.www1.orion.ui.assessment

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.ui.codereview.diff.EditorComponentInlaysManager
import de.tum.www1.orion.dto.Feedback
import de.tum.www1.orion.exercise.OrionAssessmentService
import de.tum.www1.orion.util.translate
import java.awt.BorderLayout
import javax.swing.*

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

    var component: JComponent = JPanel()
    private var textArea: JEditorPane = JEditorPane()
    private var spinner: JSpinner = JSpinner()
    private var buttonBar: JPanel = JPanel()

    private var edit: JButton = JButton(translate("orion.exercise.assessment.edit"))
    private var cancel: JButton = JButton(translate("orion.exercise.assessment.cancel"))
    private var save: JButton = JButton(translate("orion.exercise.assessment.save"))
    private var delete: JButton = JButton(translate("orion.exercise.assessment.delete"))

    init {
        edit.addActionListener {
            isEditable = true
        }

        cancel.addActionListener {
            cancel()
        }

        delete.addActionListener {
            delete()
        }

        save.addActionListener {
            save()
        }

        spinner.model = SpinnerNumberModel(0.0, null, null, 0.5)

        component.layout = BorderLayout()
        component.add(textArea, BorderLayout.CENTER)
        component.add(spinner, BorderLayout.EAST)
        component.add(buttonBar, BorderLayout.SOUTH)

        project = inlaysManager.editor.project!!

        resetValues()
        updateGui()
        disposer = inlaysManager.insertAfter(line, component)
    }

    private fun updateGui() {
        if (isEditable) {
            textArea.isEditable = true
            spinner.isEnabled = true

            buttonBar.removeAll()
            buttonBar.add(cancel)
            if (feedback != null) {
                buttonBar.add(delete)
            }
            buttonBar.add(save)
        } else {
            textArea.isEditable = false
            spinner.isEnabled = false

            buttonBar.removeAll()
            buttonBar.add(edit)
        }
        component.repaint()
    }

    private fun resetValues() {
        textArea.text = feedback?.detailText ?: ""
        spinner.value = feedback?.credits ?: 0
    }

    private fun cancel() {
        if (feedback != null) {
            resetValues()
            isEditable = false
        } else {
            disposer?.dispose()
        }
    }

    private fun delete() {
        feedback?.let {
            project.service<OrionAssessmentService>().deleteFeedback(it)
            disposer?.dispose()
        }
    }

    private fun save() {
        if (feedback != null) {
            feedback?.let {
                it.credits = spinner.value as Double
                it.detailText = textArea.text
            }
            project.service<OrionAssessmentService>().updateFeedback()
        } else {
            val newFeedback = Feedback(
                spinner.value as Double,
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
}
