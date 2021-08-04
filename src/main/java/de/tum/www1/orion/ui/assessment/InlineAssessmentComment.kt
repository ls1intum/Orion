package de.tum.www1.orion.ui.assessment

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.ui.codereview.diff.EditorComponentInlaysManager
import de.tum.www1.orion.dto.Feedback
import de.tum.www1.orion.exercise.OrionAssessmentService
import de.tum.www1.orion.exercise.OrionJavaTutorProjectCreator
import java.awt.BorderLayout
import java.nio.file.Paths
import javax.swing.*

class InlineAssessmentComment(
    private var feedback: Feedback?,
    inlaysManager: EditorComponentInlaysManager,
    private val line: Int
) {
    private var isEditable: Boolean = feedback == null
        set(value) {
            field = value
            updateGui()
        }
    private val disposer: Disposable?
    private val project: Project
    private var path: String

    var component: JComponent = JPanel()
    private var textArea: JEditorPane = JEditorPane()
    private var spinner: JSpinner = JSpinner()
    private var buttonBar: JPanel = JPanel()

    private var edit: JButton = JButton("edit")
    private var cancel: JButton = JButton("cancel")
    private var save: JButton = JButton("save")
    private var delete: JButton = JButton("delete")

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

        spinner.model = SpinnerNumberModel(0, null, null, 0.5)

        component.layout = BorderLayout()
        component.add(textArea, BorderLayout.CENTER)
        component.add(spinner, BorderLayout.EAST)
        component.add(buttonBar, BorderLayout.SOUTH)

        project = inlaysManager.editor.project!!
        path = Paths.get(project.basePath!!, OrionJavaTutorProjectCreator.ASSIGNMENT)
            .relativize(inlaysManager.editor.virtualFile.toNioPath()).joinToString("/")

        resetValues()
        updateGui()
        disposer = inlaysManager.insertAfter(line, component)
    }

    private fun updateGui() {
        if (isEditable) {
            textArea.isEditable = true
            (spinner.editor as? JSpinner.DefaultEditor)?.textField?.isEditable = true

            buttonBar.removeAll()
            buttonBar.add(cancel)
            if (feedback != null) {
                buttonBar.add(delete)
            }
            buttonBar.add(save)
        } else {
            textArea.isEditable = false
            (spinner.editor as? JSpinner.DefaultEditor)?.textField?.isEditable = false

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
                it.credits = spinner.value as Int
                it.detailText = textArea.text
            }
            project.service<OrionAssessmentService>().updateFeedback()
        } else {
            val newFeedback = Feedback(
                spinner.value as Int,
                textArea.text,
                "file:${path}_line:$line",
                "File $path at line ${line + 1}",
                "MANUAL",
                line,
                path
            )
            feedback = newFeedback
            project.service<OrionAssessmentService>().addFeedback(newFeedback)
        }
        isEditable = false
    }
}
