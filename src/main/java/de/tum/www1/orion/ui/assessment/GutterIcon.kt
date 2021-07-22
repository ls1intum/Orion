package de.tum.www1.orion.ui.assessment

import com.intellij.diff.util.LineRange
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.util.ui.codereview.diff.AddCommentGutterIconRenderer
import com.intellij.util.ui.codereview.diff.DiffEditorGutterIconRendererFactory
import com.intellij.util.ui.codereview.diff.EditorComponentInlaysManager
import com.intellij.util.ui.codereview.diff.EditorRangesController
import de.tum.www1.orion.ui.util.notify

fun addGutterIconsToEditor(editor: FileEditor) {
    // if editor cannot be casted, abort
    val editorImpl = (editor as? TextEditor)?.editor as? EditorImpl ?: return
    // inlays manager that manages the inline comments
    val inlaysManager = EditorComponentInlaysManager(editorImpl)
    val factory = GutterIconRendererFactory(inlaysManager)
    GutterIconController(factory, inlaysManager.editor)
}

/**
 * Provides the [GutterIconRenderer] to the [GutterIconController]
 *
 * @property inlaysManager passed through
 */
private class GutterIconRendererFactory(private val inlaysManager: EditorComponentInlaysManager) : DiffEditorGutterIconRendererFactory {
    override fun createCommentRenderer(line: Int): AddCommentGutterIconRenderer {
        return GutterIconRenderer(line, inlaysManager)
    }
}

/**
 * Provides all necessary data about the gutter icons, i.e. the icon, the tooltip text, the click action, ...
 *
 * @property line line the gutter icon is in
 * @property inlaysManager inlays manager of the editor, passed to the AddCommentAction
 */
private class GutterIconRenderer(override val line: Int, private val inlaysManager: EditorComponentInlaysManager) :
    AddCommentGutterIconRenderer() {

    // Must be overridden, use unclear
    override fun disposeInlay() {
        inlaysManager.editor.project?.notify("dispose called")
    }

    override fun getClickAction(): AnAction {
        return AddCommentAction(inlaysManager, line)
    }
}

/**
 * Registers the [GutterIconRendererFactory] to the Editor and marks all lines as commentable
 *
 * @param gutterIconRendererFactory factory providing the renderer of the gutter icons
 * @param editor editor to add the gutter icons to
 */
private class GutterIconController(gutterIconRendererFactory: DiffEditorGutterIconRendererFactory, editor: EditorEx) :
    EditorRangesController(gutterIconRendererFactory, editor) {
    init {
        markCommentableLines(LineRange(0, editor.markupModel.document.lineCount))
    }
}
