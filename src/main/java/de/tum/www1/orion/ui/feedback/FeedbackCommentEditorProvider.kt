package de.tum.www1.orion.ui.feedback

import com.intellij.openapi.components.service
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import de.tum.www1.orion.exercise.OrionFeedbackService
import de.tum.www1.orion.util.translate


/**
 * Provides an Editor that shows Feedback in IntelliJ
 */
class FeedbackCommentEditorProvider : FileEditorProvider, DumbAware {
    override fun accept(project: Project, file: VirtualFile): Boolean {
        //Check if there is feedback available
        val relativePath = file.path.removePrefix(project.basePath.toString()).removePrefix("/")
        return project.service<OrionFeedbackService>().getFeedbackFor(relativePath) != null
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        val factory = EditorFactory.getInstance()
        val viewer = file.let { it ->
            FileDocumentManager.getInstance().getDocument(it)?.let {
                factory.createEditor(it, project, file.fileType, true)
            }
        } ?: factory.createViewer(factory.createDocument(translate("orion.error.file.loaded")), project)
        // remove base bath from the path
        val relativePath = file.path.removePrefix(project.basePath.toString()).removePrefix("/")
        val editor = OrionFeedbackCommentEditor(viewer, relativePath, file)
        // dispose editor with the project
        Disposer.register(project, editor)
        return editor
    }

    override fun getEditorTypeId(): String = "OrionFeedbackEditor"

    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.HIDE_DEFAULT_EDITOR

}
