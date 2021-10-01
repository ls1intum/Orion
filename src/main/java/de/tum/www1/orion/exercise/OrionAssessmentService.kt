package de.tum.www1.orion.exercise

import com.intellij.collaboration.ui.codereview.diff.EditorComponentInlaysManager
import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import de.tum.www1.orion.dto.Feedback
import de.tum.www1.orion.exercise.registry.OrionTutorExerciseRegistry
import de.tum.www1.orion.messaging.OrionIntellijStateNotifier
import de.tum.www1.orion.ui.assessment.InlineAssessmentComment
import de.tum.www1.orion.ui.assessment.OrionAssessmentEditor
import de.tum.www1.orion.ui.util.YesNoChooser
import de.tum.www1.orion.ui.util.notify
import de.tum.www1.orion.util.JsonUtils.gson
import de.tum.www1.orion.util.translate

/**
 * Service managing the assessment for a tutor project.
 * Stores all feedback from Artemis in a map mapping the feedback to the files they belong to.
 * Also manages synchronization with Artemis through the connectors
 *
 * @property project the service belongs to
 */
class OrionAssessmentService(private val project: Project) {
    private var feedbackPerFile: MutableMap<String, MutableList<Feedback>> = mutableMapOf()
    // set storing a pair of path and line for all new, unsaved feedback comments
    // required to ensure only one feedback can be created per line
    private val pendingFeedback: MutableSet<Pair<String, Int>> = mutableSetOf()
    private var isInitialized: Boolean = false

    /**
     * Initializes the map with feedback sent from Artemis and notifies all [OrionAssessmentEditor]s
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
                        (editor as? OrionAssessmentEditor)?.initializeFeedback()
                    }
                }
            } else {
                // if not first load, close and reopen all files opened by assessment editors to reload the feedback
                closeAssessmentEditors(true)
            }
        }
    }

    /**
     * Retrieves the feedback list for the given file or null if no feedback has been loaded yet
     *
     * @param relativePath of the file to get the feedback for
     * @return feedback of the file
     */
    fun getFeedbackFor(relativePath: String): List<Feedback>? {
        if (!isInitialized) return null

        return feedbackPerFile[relativePath] ?: emptyList()
    }

    /**
     * Deletes the given feedback from the map and informs Artemis
     *
     * @param feedback to delete
     */
    fun deleteFeedback(feedback: Feedback) {
        if (this.feedbackPerFile[feedback.path!!]?.remove(feedback) != true) {
            project.notify("Deletion failed")
        }
        synchronizeWithArtemis()
    }

    /**
     * Updates feedback; no action required since the values have already been changed; only informs Artemis
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
        this.feedbackPerFile.putIfAbsent(feedback.path!!, mutableListOf(feedback))?.add(feedback)
        deletePendingFeedback(feedback.path!!, feedback.line!!)
        synchronizeWithArtemis()
    }

    /**
     * Close all open [OrionAssessmentEditor]s and reinitialize all variables.
     * Should be called upon downloading a new submission
     */
    fun reset() {
        closeAssessmentEditors(false)
        feedbackPerFile = mutableMapOf()
        isInitialized = false
    }

    /**
     * Adds a new feedback comment to the given file and line, if no feedback comment is present at that position yet
     *
     * @param path of the file to add a feedback to
     * @param line line in that file to add feedback to
     * @param inlaysManager passed through to the new comment
     */
    fun addFeedbackCommentIfPossible(path: String, line: Int, inlaysManager: EditorComponentInlaysManager) {
        val pair = Pair(path, line)
        // if there is already a feedback comment in that file and line, abort
        if (pendingFeedback.contains(pair) || getFeedbackFor(path)
                ?.any { it.line == line } == true) {
            return
        }

        // add feedback
        InlineAssessmentComment(null, path, line, inlaysManager)
        pendingFeedback.add(pair)
    }

    /**
     * Removes a pending feedback. This allows a different feedback to be added to the given file and line
     *
     * @param path
     * @param line
     */
    fun deletePendingFeedback(path: String, line: Int) {
        pendingFeedback.remove(Pair(path, line))
    }

    private fun synchronizeWithArtemis() {
        val submissionId = project.service<OrionTutorExerciseRegistry>().submissionId ?: return
        val feedbackAsJson = gson().toJson(feedbackPerFile.values.flatten())
        project.messageBus.syncPublisher(OrionIntellijStateNotifier.INTELLIJ_STATE_TOPIC)
            .updateAssessment(submissionId, feedbackAsJson)
    }

    private fun closeAssessmentEditors(reopen: Boolean) {
        runInEdt {
            FileEditorManager.getInstance(project).let { manager ->
                val selectedFile = manager.selectedEditor?.file
                manager.allEditors.filterIsInstance<OrionAssessmentEditor>().map { it.file }
                    .forEach {
                        manager.closeFile(it)
                        if (reopen && it != selectedFile) {
                            manager.openFile(it, false)
                        }
                    }
                // open selected file last to ensure focus; the focusEditor parameter does not seem to work
                if (reopen && selectedFile != null) {
                    manager.openFile(selectedFile, true)
                }
            }
        }
    }
}
