package de.tum.www1.orion.exercise

import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import de.tum.www1.orion.dto.Feedback
import de.tum.www1.orion.exercise.registry.OrionTutorExerciseRegistry
import de.tum.www1.orion.messaging.OrionIntellijStateNotifier
import de.tum.www1.orion.ui.assessment.OrionGradingEditor
import de.tum.www1.orion.ui.util.YesNoChooser
import de.tum.www1.orion.ui.util.notify
import de.tum.www1.orion.util.JsonUtils.gson
import de.tum.www1.orion.util.translate

class OrionAssessmentService(private val project: Project) {
    private var feedback: MutableMap<String, MutableList<Feedback>> = mutableMapOf()
    private var isInitialized = false

    /**
     * Initializes the map with feedback sent from Artemis
     *
     * @param submissionId to check validity against
     * @param feedback to load
     */
    fun initializeFeedback(submissionId: Long, feedback: Array<Feedback>) {
        // validate submissionId, reject feedback for a different submission
        if (project.service<OrionTutorExerciseRegistry>().submissionId != submissionId) {
            project.notify(translate("orion.warning.assessment.submissionId"))
            return
        }


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
                it.path!!
            }
            isInitialized = true
            if (!isInitialized) {
                // if first load, insert feedback into all currently open editors since their own initialization will have occurred before the feedback was loaded and therefore failed
                FileEditorManager.getInstance(project).let {
                    it.allEditors.forEach { editor ->
                        (editor as? OrionGradingEditor)?.initializeFeedback()
                    }
                }
            } else {
                // if not first load, close and reopen all files opened by grading editors to reload the feedback
                FileEditorManager.getInstance(project).let { manager ->
                    manager.allEditors.filterIsInstance<OrionGradingEditor>().map { it.file }
                        .forEach {
                            manager.closeFile(it)
                            manager.openFile(it, false)
                        }
                }
            }
        }
    }

    fun getFeedbackFor(relativePath: String): List<Feedback>? {
        if (!isInitialized) return null

        return feedback[relativePath] ?: emptyList()
    }

    /**
     * Deletes the given feedback from the map and informs Artemis
     *
     * @param feedback to delete
     */
    fun deleteFeedback(feedback: Feedback) {
        if (this.feedback[feedback.path!!]?.remove(feedback) != true) {
            project.notify("Deletion failed")
        }
        synchronizeWithArtemis()
    }

    /**
     * Updates feedback; no action required since the values have already been changed; only informs Artemis
     *
     */
    fun updateFeedback() {
        synchronizeWithArtemis()
    }

    /**
     * Adds the given feedback to the map and informs Artemis
     *
     * @param feedback to add
     */
    fun addFeedback(feedback: Feedback) {
        // add to feedback list of file if the list is present, else put a new list
        this.feedback.putIfAbsent(feedback.path!!, mutableListOf(feedback))?.add(feedback)
        synchronizeWithArtemis()
    }

    private fun synchronizeWithArtemis() {
        val submissionId = project.service<OrionTutorExerciseRegistry>().submissionId ?: return
        val feedbackAsJson = gson().toJson(feedback.values.flatten())
        project.messageBus.syncPublisher(OrionIntellijStateNotifier.INTELLIJ_STATE_TOPIC)
            .updateAssessment(submissionId, feedbackAsJson)
    }
}
