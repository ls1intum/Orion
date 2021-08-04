package de.tum.www1.orion.ui.assessment

import com.intellij.diff.util.LineRange
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.util.ui.codereview.diff.AddCommentGutterIconRenderer
import com.intellij.util.ui.codereview.diff.DiffEditorGutterIconRendererFactory
import com.intellij.util.ui.codereview.diff.EditorComponentInlaysManager
import com.intellij.util.ui.codereview.diff.EditorRangesController
import de.tum.www1.orion.dto.Feedback
import de.tum.www1.orion.exercise.OrionAssessmentService
import de.tum.www1.orion.ui.util.notify

/**
 * Adds both the gutter icons to add more comments as well as all current feedback to the given editor
 *
 * @param editor to add the icons and feedback to
 * @param feedback for the file of the editor
 */
fun addGutterIconsAndFeedbackToEditor(editor: FileEditor, feedback: List<Feedback>) {
    // if editor cannot be casted, abort
    val editorImpl = (editor as? TextEditor)?.editor as? EditorImpl ?: return
    // inlays manager that manages the inline comments
    val inlaysManager = EditorComponentInlaysManager(editorImpl)
    // add feedback
    feedback.forEach {
        InlineAssessmentComment(it, inlaysManager, it.line!!)
    }
    // add gutter icons
    val factory = GutterIconRendererFactory(inlaysManager)
    GutterIconController(factory, inlaysManager.editor)
}

/**
 * Provides the [GutterIconRenderer] to the [GutterIconController], also cleans up any potential previous renderer
 *
 * @property inlaysManager passed through
 */
private class GutterIconRendererFactory(private val inlaysManager: EditorComponentInlaysManager) :
    DiffEditorGutterIconRendererFactory {
    override fun createCommentRenderer(line: Int): AddCommentGutterIconRenderer {
        val renderer = GutterIconRenderer(line, inlaysManager)
        // store renderer so it can be disposed upon reload
        inlaysManager.editor.project?.service<OrionAssessmentService>()?.gutterIconAndFeedbackRenderers?.add(renderer)
        return renderer
    }
}

/**
 * Provides all necessary data about the gutter icons, i.e. the icon, the tooltip text, the click action, ...
 *
 * @property line line the gutter icon is in
 * @property inlaysManager inlays manager of the editor, passed to the [InlineAssessmentComment]
 */
class GutterIconRenderer(override val line: Int, private val inlaysManager: EditorComponentInlaysManager) :
    AddCommentGutterIconRenderer() {

    // Must be overridden, use unclear
    override fun disposeInlay() {
        inlaysManager.dispose()
    }

    override fun getClickAction(): AnAction {
        return object : AnAction() {
            override fun actionPerformed(e: AnActionEvent) {
                InlineAssessmentComment(null, inlaysManager, line)
            }
        }
    }
}

/**
 * Registers the [GutterIconRendererFactory] to the Editor and marks all lines as commentable
 *
 * @param gutterIconRendererFactory factory providing the renderer of the gutter icons
 * @param editor to add the gutter icons to
 */
private class GutterIconController(gutterIconRendererFactory: DiffEditorGutterIconRendererFactory, editor: EditorEx) :
    EditorRangesController(gutterIconRendererFactory, editor) {
    init {
        markCommentableLines(LineRange(0, editor.markupModel.document.lineCount))
    }
}
