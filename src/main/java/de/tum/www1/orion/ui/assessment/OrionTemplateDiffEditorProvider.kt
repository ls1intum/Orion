//@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
package de.tum.www1.orion.ui.assessment

import com.intellij.diff.DiffRequestFactory
import com.intellij.diff.editor.DiffRequestProcessorEditor
import com.intellij.diff.editor.DiffVirtualFile
import com.intellij.diff.editor.SimpleDiffVirtualFile
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import de.tum.www1.orion.util.OrionAssessmentUtils
import de.tum.www1.orion.util.OrionAssessmentUtils.getTemplateOf


/**
 * Registered in plugin.xml. Gets activated for all files in the assignment folder.
 * Upon opening such a file it will load the student submission file corresponding to the requested file and
 * generate a DiffEditor that will allow viewing the diff
 * Tutors can switch between the OrionAssessmentEditor, Diff-Editors and the normal editor
 */
class OrionTemplateDiffEditorProvider : OrionEditorProvider() {

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        val relativePath = file.fileSystem.getNioPath(file)?.let {
            OrionAssessmentUtils.getRelativePathForAssignment(project, it)
        }!!
        val studentFile = relativePath.let {
            VirtualFileManager.getInstance().refreshAndFindFileByNioPath(
                OrionAssessmentUtils.getStudentSubmissionOf(project).resolve(it)
            )
        }
        val templateFile = VirtualFileManager.getInstance().refreshAndFindFileByNioPath(
            getTemplateOf(project).resolve(relativePath)
        )

        val request = DiffRequestFactory.getInstance().createFromFiles(project, studentFile, templateFile)
        val diffFile = SimpleDiffVirtualFile(request)
        // Copy from DiffEditorProvider
        val processor = (diffFile as DiffVirtualFile).createProcessor(project)
        return DiffRequestProcessorEditor(diffFile, processor)
    }

    override fun getEditorTypeId(): String = "ORION TEMPLATE DIFF EDITOR"
    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.PLACE_BEFORE_DEFAULT_EDITOR
}