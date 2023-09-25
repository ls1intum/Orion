package de.tum.www1.orion.ui.feedback

import com.intellij.collaboration.ui.codereview.diff.EditorComponentInlaysManager
import com.intellij.diff.util.FileEditorBase
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.vfs.VirtualFile
import de.tum.www1.orion.exercise.OrionFeedbackService
import de.tum.www1.orion.util.OrionAssessmentUtils
import de.tum.www1.orion.util.translate
import javax.swing.JComponent
import javax.swing.JLabel

/**
 * A editor for Feedback comments providing a view for students to review feedback with maintaining the edit ability of their code.
 */
class OrionFeedbackCommentEditor(
    private var myEditor: Editor,
    private val relativePath: String,
    private val file: VirtualFile
) : FileEditorBase() {
    private val headerLabel: JLabel =
        OrionAssessmentUtils.createHeader(translate("orion.exercise.feedbackModeLoading").uppercase())

    init {
        myEditor.headerComponent = headerLabel

        initializeFeedback()
    }

    override fun getComponent(): JComponent = myEditor.component

    override fun getName(): String = translate("orion.exercise.feedback")

    override fun getPreferredFocusedComponent(): JComponent? = null

    // needed to avoid deprecation warning; Should return the file for which the provider was called, note however this editor is showing a different file
    override fun getFile(): VirtualFile = file

    /**
     * Requests the [OrionFeedbackService] for feedback comments for the opened file.
     * If successful, adds the returned feedback comments as well as the gutter icons to create new comments to the editor
     * If not, does nothing. Relies on the [OrionFeedbackService] to be called again if feedback becomes available
     */
    fun initializeFeedback() {
        // request feedback, if not yet initialized, abort
        val feedback = myEditor.project?.service<OrionFeedbackService>()?.getFeedbackFor(relativePath) ?: return
        val editorImpl = myEditor as? EditorImpl ?: return
        // inlays manager that manages the inline comments
        val inlaysManager = EditorComponentInlaysManager(editorImpl)
        // add feedback
        feedback.forEach {
            InlineFeedbackComment(it, inlaysManager)
        }
        // remove loading text
        headerLabel.text = translate("orion.exercise.feedbackMode").uppercase()
    }

    override fun dispose() {
        super.dispose()
        EditorFactory.getInstance().releaseEditor(myEditor)
    }
}