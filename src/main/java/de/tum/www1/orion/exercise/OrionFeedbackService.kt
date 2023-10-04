package de.tum.www1.orion.exercise

import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.components.Service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import de.tum.www1.orion.dto.Feedback
import de.tum.www1.orion.ui.feedback.OrionFeedbackCommentEditor
import de.tum.www1.orion.ui.util.YesNoChooser

/**
 * A Feedback service that provides a feedback from tutors for students
 */
@Service(Service.Level.PROJECT)
class OrionFeedbackService(private val project: Project) : OrionInlineCommentService(project = project) {
    override fun initializeFeedback(submissionId: Long, feedback: Array<Feedback>) {
        runInEdt {
            if (isInitialized) {
                // if feedback already has been loaded, ask if it should be overridden
                if (!invokeAndWaitIfNeeded { YesNoChooser(project, "feedbackOverwrite").showAndGet() }) {
                    // if no, do nothing
                    return@runInEdt
                }
            }

            // reference has the format "file:FILE_line:LINE"
            feedback.forEach {
                if (it.reference != null) {
                    val textParts = it.reference.split("_")
                    if (textParts.size == 2 && textParts[0].startsWith("file:") && textParts[1].startsWith("line:")) {
                        it.path = textParts[0].substring(5)
                        it.line = textParts[1].substring(5).toInt()
                    }
                }

            }

            // filter invalid entries, group by file
            this.feedbackPerFile = feedback.filter {
                it.path != null && it.line != null
            }.groupByTo(mutableMapOf()) {
                it.path!!
            }
            if (!isInitialized) {
                isInitialized = true
                // if first load, insert feedback into all currently open editors since their own initialization will have occurred before the feedback was loaded and therefore failed
                FileEditorManager.getInstance(project).let {
                    it.allEditors.forEach { editor ->
                        (editor as? OrionFeedbackCommentEditor)?.initializeFeedback()
                    }
                }
            } else {
                // if not first load, close and reopen all files opened by assessment editors to reload the feedback
                closeEditors(true)
            }
        }
    }
}
