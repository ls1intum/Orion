package de.tum.www1.orion.ui.assessment

import com.intellij.diff.DiffRequestFactory
import com.intellij.diff.editor.DiffEditorProvider
import com.intellij.diff.editor.SimpleDiffVirtualFile
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import de.tum.www1.orion.util.OrionAssessmentUtils
import de.tum.www1.orion.util.OrionAssessmentUtils.getTemplateOf

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

        //TODO: change the name of the editor
        return DiffEditorProvider().createEditor(project, diffFile)
    }

    override fun getEditorTypeId(): String = "ORION TEMPLATE DIFF EDITOR"


}