package de.tum.www1.orion.util

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.nio.file.Path
import java.nio.file.Paths

object OrionAssessmentUtils {
    private const val ASSIGNMENT = "assignment"
    private const val STUDENT_SUBMISSION = "studentSubmission"
    const val TEMPLATE = "template"

    /**
     * Gives the nio path of the assignment folder of the given project
     *
     * @param project to find the folder for
     * @return absolute path to the assignment folder
     */
    fun getAssignmentOf(project: Project): Path = Paths.get(project.basePath!!, ASSIGNMENT)

    /**
     * Gives the nio path of the student submission folder of the given project.
     * This folder contains a copy of the submission needed for the assessment.
     * Its contents should not be edited
     *
     * @param project to find the folder for
     * @return absolute path to the student submission folder
     */
    fun getStudentSubmissionOf(project: Project): Path = Paths.get(project.basePath!!, STUDENT_SUBMISSION)

    /**
     * Gives the nio path of the template folder of the given project.
     * This folder contains a copy of the template needed for the assessment diff.
     * Its contents should not be edited
     *
     * @param project to find the folder for
     * @return absolute path to the student submission folder
     */
    fun getTemplateOf(project: Project): Path = Paths.get(project.basePath!!, TEMPLATE)

    /**
     * Determines a representation of the given path relative to the project's assignment folder
     *
     * @param project of the assignment folder
     * @param absolutePath to determine the relative path of
     * @return a path that, if appended to the project's assignment folder, will give the absolute path
     */
    fun getRelativePathForAssignment(project: Project, absolutePath: Path): Path =
        getAssignmentOf(project).relativize(absolutePath)

    /**
     * When called installs a listener that prevents any file from the student submission folder from getting opened
     *
     * @param project to prohibit the opening for
     */
    fun makeStudentSubmissionAndTemplateReadonly(project: Project) {
        project.messageBus.connect()
            .subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {
                override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
                    val filePath = file.toNioPath()
                    if (filePath.startsWith(getStudentSubmissionOf(project)) ||
                        filePath.startsWith(getTemplateOf(project))
                    ) {
                        (source.selectedEditor as? TextEditor)?.editor?.document?.setReadOnly(true)
                    }
                }
            })
    }
}
