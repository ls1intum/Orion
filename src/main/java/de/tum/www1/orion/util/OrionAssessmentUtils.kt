package de.tum.www1.orion.util

import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.awt.Font
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.JLabel
import javax.swing.SwingConstants

/**
 * Class providing utilities for assessments, e.g. retrieving various directories relevant for assessment
 * and setting certain folders read-only
 */
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
     * Also adds a header for every editor opened for files in the assignment folder
     *
     * @param project to prohibit the opening for
     */
    fun makeStudentSubmissionAndTemplateReadonlyAndAddHeader(project: Project) {
        project.messageBus.connect()
            .subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {
                override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
                    makeStudentSubmissionAndTemplateReadonlyAndAddHeader(project, source, file)
                }
            })
        runInEdt {
            val manager = FileEditorManager.getInstance(project)
            manager.openFiles.forEach { makeStudentSubmissionAndTemplateReadonlyAndAddHeader(project, manager, it) }
        }
    }

    private fun makeStudentSubmissionAndTemplateReadonlyAndAddHeader(
        project: Project,
        manager: FileEditorManager,
        file: VirtualFile
    ) {
        val filePath = file.fileSystem.getNioPath(file) ?: return
        when {
            filePath.startsWith(getStudentSubmissionOf(project)) ||
                    filePath.startsWith(getTemplateOf(project)) -> manager.getEditors(file).forEach {
                (it as? TextEditor)?.editor?.document?.setReadOnly(true)
            }
            filePath.startsWith(getAssignmentOf(project)) -> manager.getEditors(file).forEach {
                (it as? TextEditor)?.editor?.headerComponent =
                    createHeader(translate("orion.exercise.editMode").uppercase())
            }
        }
    }

    /**
     * Wraps the given String into a JLabel using a bold font with 1.5 times the default size
     */
    fun createHeader(text: String): JLabel {
        val label = JLabel(text, SwingConstants.CENTER)
        label.font = Font(label.font.name, Font.BOLD, (label.font.size * 1.5).toInt())
        return label
    }
}
