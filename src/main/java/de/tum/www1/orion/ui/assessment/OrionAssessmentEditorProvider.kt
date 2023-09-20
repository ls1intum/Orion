package de.tum.www1.orion.ui.assessment

import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import de.tum.www1.orion.util.OrionAssessmentUtils.getRelativePathForAssignment
import de.tum.www1.orion.util.OrionAssessmentUtils.getStudentSubmissionOf
import de.tum.www1.orion.util.translate

/**
 * Registered in plugin.xml. Gets activated for all files in the assignment folder.
 * Upon opening such a file it will load the student submission file corresponding to the requested file and
 * generate a [OrionAssessmentEditor] that will allow adding assessment to it
 * Tutors can switch between the [OrionAssessmentEditor] and the normal editor
 */
class OrionAssessmentEditorProvider : OrionEditorProvider() {

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        val relativePath = file.fileSystem.getNioPath(file)?.let {
            getRelativePathForAssignment(project, it)
        }!!
        val studentFile = relativePath.let {
            VirtualFileManager.getInstance().refreshAndFindFileByNioPath(
                getStudentSubmissionOf(project).resolve(it)
            )
        }

        val factory = EditorFactory.getInstance()
        val viewer = studentFile?.let { it ->
            FileDocumentManager.getInstance().getDocument(it)?.let {
                factory.createEditor(it, project, file.fileType, true)
            }
        } ?: factory.createViewer(factory.createDocument(translate("orion.error.file.loaded")), project)

        return OrionAssessmentEditor(viewer, relativePath.joinToString("/"), file)
    }

    override fun getEditorTypeId(): String = "ORION ASSESSMENT EDITOR"
    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.PLACE_BEFORE_DEFAULT_EDITOR
}
