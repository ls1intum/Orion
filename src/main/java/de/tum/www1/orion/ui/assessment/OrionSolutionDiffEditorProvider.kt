package de.tum.www1.orion.ui.assessment

import com.intellij.diff.DiffRequestFactory
import com.intellij.diff.editor.DiffEditorProvider
import com.intellij.diff.editor.SimpleDiffVirtualFile
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import de.tum.www1.orion.util.OrionAssessmentUtils.getRelativePathForAssignment
import de.tum.www1.orion.util.OrionAssessmentUtils.getSolutionOf
import de.tum.www1.orion.util.OrionAssessmentUtils.getStudentSubmissionOf

class OrionSolutionDiffEditorProvider : OrionEditorProvider() {

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        val relativePath = file.fileSystem.getNioPath(file)?.let {
            getRelativePathForAssignment(project, it)
        }!!
        val studentFile = relativePath.let {
            VirtualFileManager.getInstance().refreshAndFindFileByNioPath(
                getStudentSubmissionOf(project).resolve(it)
            )
        }
        val solutionFile = VirtualFileManager.getInstance().refreshAndFindFileByNioPath(
            getSolutionOf(project).resolve(relativePath)
        )

        val request = DiffRequestFactory.getInstance().createFromFiles(project, studentFile, solutionFile)
        val diffFile = SimpleDiffVirtualFile(request)

        return DiffEditorProvider().createEditor(project, diffFile)
    }

    override fun getEditorTypeId(): String = "ORION SOLUTION DIFF EDITOR"
}