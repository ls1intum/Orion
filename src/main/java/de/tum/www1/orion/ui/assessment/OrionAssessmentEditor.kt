package de.tum.www1.orion.ui.assessment

import com.intellij.collaboration.ui.codereview.diff.EditorComponentInlaysManager
import com.intellij.diff.util.FileEditorBase
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.vfs.VirtualFile
import de.tum.www1.orion.exercise.OrionAssessmentService
import de.tum.www1.orion.util.OrionAssessmentUtils.createHeader
import de.tum.www1.orion.util.translate
import javax.swing.JComponent
import javax.swing.JLabel

/**
 * FileEditor showing the contents of the given editor with the additional ability to show feedback comments and gutter icons to create them
 *
 * @property myEditor content to show, a read-only view of the student submission version of the file
 * @property relativePath of the opened file, relative to the assignment folder. Matches the way Artemis denotes files in feedback; passed over to any newly created feedback comment
 * @property file whose student submission is opened, see [getFile]
 */
class OrionAssessmentEditor(
    private var myEditor: Editor,
    private val relativePath: String,
    private val file: VirtualFile
) : FileEditorBase() {
    private val headerLabel: JLabel = createHeader(translate("orion.exercise.assessmentModeLoading").uppercase())

    init {
        myEditor.headerComponent = headerLabel

        initializeFeedback()
    }

    override fun getComponent(): JComponent = myEditor.component

    override fun getName(): String = translate("orion.exercise.assessment")

    override fun getPreferredFocusedComponent(): JComponent? = null

    // needed to avoid deprecation warning; Should return the file for which the provider was called, note however this editor is showing a different file
    override fun getFile(): VirtualFile = file

    /**
     * Requests the [OrionAssessmentService] for feedback comments for the opened file.
     * If successful, adds the returned feedback comments as well as the gutter icons to create new comments to the editor
     * If not, does nothing. Relies on the [OrionAssessmentService] to be called again if feedback becomes available
     */
    fun initializeFeedback() {
        // request feedback, if not yet initialized, abort
        val feedback = myEditor.project?.service<OrionAssessmentService>()?.getFeedbackFor(relativePath) ?: return

        val editorImpl = myEditor as? EditorImpl ?: return
        // inlays manager that manages the inline comments
        val inlaysManager = EditorComponentInlaysManager(editorImpl)
        // add feedback
        feedback.forEach {
            InlineAssessmentComment(it, inlaysManager)
        }
        // add gutter icons
        OrionGutterIconController(relativePath, inlaysManager)

        // remove loading text
        headerLabel.text = translate("orion.exercise.assessmentMode").uppercase()
    }

    override fun dispose() {
        super.dispose()
        EditorFactory.getInstance().releaseEditor(myEditor)
    }
}
