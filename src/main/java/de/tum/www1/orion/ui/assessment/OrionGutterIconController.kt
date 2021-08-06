package de.tum.www1.orion.ui.assessment

import com.intellij.diff.util.LineRange
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.util.Disposer
import com.intellij.util.ui.codereview.diff.AddCommentGutterIconRenderer
import com.intellij.util.ui.codereview.diff.DiffEditorGutterIconRendererFactory
import com.intellij.util.ui.codereview.diff.EditorComponentInlaysManager
import com.intellij.util.ui.codereview.diff.EditorRangesController

/**
 * Creates and registers a [GutterIconRendererFactory] to the editor and marks all lines as commentable
 *
 * @param inlaysManager belonging to the editor to add the icons to
 * @param path passed through
 */
class OrionGutterIconController(path: String, inlaysManager: EditorComponentInlaysManager) :
    EditorRangesController(GutterIconRendererFactory(path, inlaysManager), inlaysManager.editor) {
    init {
        markCommentableLines(LineRange(0, inlaysManager.editor.markupModel.document.lineCount))
    }
}

/**
 * Provides the [GutterIconRenderer] to the [OrionGutterIconController], also cleans up any potential previous renderer
 *
 * @property inlaysManager passed through
 * @property path passed through
 */
class GutterIconRendererFactory(private val path: String, private val inlaysManager: EditorComponentInlaysManager) :
    DiffEditorGutterIconRendererFactory {
    override fun createCommentRenderer(line: Int): AddCommentGutterIconRenderer {
        return GutterIconRenderer(line, path, inlaysManager)
    }
}

/**
 * Provides all necessary data about the gutter icons, i.e. the icon, the tooltip text, the click action, ...
 *
 * @property line line the gutter icon is in
 * @property inlaysManager passed through
 * @property path passed through
 */
class GutterIconRenderer(override val line: Int, private val path: String, private val inlaysManager: EditorComponentInlaysManager) :
    AddCommentGutterIconRenderer() {

    // Must be overridden, admittedly unsure about use; Did not observe any call yet
    override fun disposeInlay() {
        Disposer.dispose(inlaysManager)
    }

    override fun getClickAction(): AnAction {
        return object : AnAction() {
            override fun actionPerformed(e: AnActionEvent) {
                InlineAssessmentComment(null, path, line, inlaysManager)
            }
        }
    }
}
