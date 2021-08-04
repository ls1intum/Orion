package de.tum.www1.orion.exercise

import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import de.tum.www1.orion.dto.Feedback
import de.tum.www1.orion.exercise.registry.OrionTutorExerciseRegistry
import de.tum.www1.orion.messaging.OrionIntellijStateNotifier
import de.tum.www1.orion.ui.assessment.GutterIconRenderer
import de.tum.www1.orion.ui.assessment.addGutterIconsAndFeedbackToEditor
import de.tum.www1.orion.ui.util.notify
import de.tum.www1.orion.util.JsonUtils.gson
import java.nio.file.Path
import java.nio.file.Paths

class OrionAssessmentService(private val project: Project) {
    private var feedback: MutableMap<Path, MutableList<Feedback>> = mutableMapOf()
    var gutterIconAndFeedbackRenderers: MutableList<GutterIconRenderer> = mutableListOf()

    fun initializeFeedback(submissionId: Long, feedback: Array<Feedback>) {
        // validate submissionId, reject feedback for a different submission
        if (project.service<OrionTutorExerciseRegistry>().submissionId != submissionId) {
            project.notify("Submission id doesn't match")
            return
        }

        // reference has the format "file:FILE_line:LINE"
        feedback.forEach {
            val textParts = it.reference.split("_")
            if (textParts.size == 2 && textParts[0].startsWith("file:") && textParts[1].startsWith("line:")) {
                it.path = textParts[0].substring(5)
                it.line = textParts[1].substring(5).toInt()
            }
        }

        // filter invalid entries, group by file
        this.feedback = feedback.filter {
            it.path != null && it.line != null
        }.groupByTo(mutableMapOf()) {
            getAbsolutePath(it.path)
        }

        gutterIconAndFeedbackRenderers.forEach { it.dispose() }
        gutterIconAndFeedbackRenderers.clear()
        configureEditor()
    }

    private fun configureEditor() {
        runInEdt {
            FileEditorManager.getInstance(project).allEditors.forEach { editor ->
                editor.file?.let { file -> addGutterIconsAndFeedbackToEditorIfNeeded(project, editor, file) }
            }
        }
        project.messageBus.connect()
            .subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {
                override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
                    val editor = source.getSelectedEditor(file) ?: return
                    addGutterIconsAndFeedbackToEditorIfNeeded(project, editor, file)
                }
            })
    }

    private fun addGutterIconsAndFeedbackToEditorIfNeeded(project: Project, editor: FileEditor, file: VirtualFile) {
        // Only show gutter icons for files in the assignment folder
        if (file.toNioPath().startsWith(Paths.get(project.basePath!!, OrionJavaTutorProjectCreator.ASSIGNMENT))) {
            addGutterIconsAndFeedbackToEditor(editor, feedback[file.toNioPath()] ?: emptyList())
        }
    }

    fun deleteFeedback(feedback: Feedback) {
        if (this.feedback[getAbsolutePath(feedback.path)]?.remove(feedback) != true) {
            project.notify("Deletion failed")
        }
        synchronizeWithArtemis()
    }

    fun updateFeedback() {
        synchronizeWithArtemis()
    }

    fun addFeedback(feedback: Feedback) {
        // add to feedback list of file if the list is present, else put a new list
        this.feedback.putIfAbsent(getAbsolutePath(feedback.path), mutableListOf(feedback))?.add(feedback)
        synchronizeWithArtemis()
    }

    private fun synchronizeWithArtemis() {
        val submissionId = project.service<OrionTutorExerciseRegistry>().submissionId ?: return
        val feedbackAsJson = gson().toJson(feedback.values.flatten())
        project.messageBus.syncPublisher(OrionIntellijStateNotifier.INTELLIJ_STATE_TOPIC)
            .updateAssessment(submissionId, feedbackAsJson)
    }

    private fun getAbsolutePath(relativePath: String?): Path {
        return Paths.get(project.basePath!!, OrionJavaTutorProjectCreator.ASSIGNMENT, relativePath)
    }
}
