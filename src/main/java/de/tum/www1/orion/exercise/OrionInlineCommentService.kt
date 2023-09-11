package de.tum.www1.orion.exercise;

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import de.tum.www1.orion.dto.Feedback
import de.tum.www1.orion.ui.assessment.OrionAssessmentEditor
import de.tum.www1.orion.ui.util.YesNoChooser

/**
 * Super class that provides shared functionality to create Editors including comments
 */
abstract class OrionInlineCommentService(private val project: Project) {
    var feedbackPerFile: MutableMap<String, MutableList<Feedback>> = mutableMapOf()

    // set storing a pair of path and line for all new, unsaved feedback comments
    // required to ensure only one feedback can be created per line
    val pendingFeedback: MutableSet<Pair<String, Int>> = mutableSetOf()
    private var isInitialized: Boolean = false


    /**
     * Defines checks that get executed at the beginning of [initializeFeedback] and returns a boolean stating
     * if the checks were successful
     * @param submissionId the submission id that gets checked
     */

    abstract fun beforeFeedbackInitialization(submissionId: Long): Boolean

    /**
     * Initializes the map with feedback sent from Artemis and notifies all [OrionAssessmentEditor]s
     *
     * @param submissionId to check validity against
     * @param feedback to load
     */
    fun initializeFeedback(submissionId: Long, feedback: Array<Feedback>) {
        // run stuff before feedback initializsation
        beforeFeedbackInitialization(submissionId)

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
     * Close all open [OrionAssessmentEditor]s and reinitialize all variables.
     * Should be called upon downloading a new submission
     */
    fun reset() {
        closeAssessmentEditors(false)
        feedbackPerFile = mutableMapOf()
        isInitialized = false
    }

    /**
     * Closes the assementEditor
     * @param reopen specifies if the editor should be opened again after closing
     */
    private fun closeAssessmentEditors(reopen: Boolean) {
        WriteAction.runAndWait<Throwable> {
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
