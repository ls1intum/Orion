package de.tum.www1.orion.exercise

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import de.tum.www1.orion.dto.Feedback
import de.tum.www1.orion.ui.assessment.OrionAssessmentEditor

/**
 * Super class that provides shared functionality to create Editors including comments
 */
abstract class OrionInlineCommentService(private val project: Project) {
    var feedbackPerFile: MutableMap<String, MutableList<Feedback>> = mutableMapOf()

    // set storing a pair of path and line for all new, unsaved feedback comments
    // required to ensure only one feedback can be created per line
    val pendingFeedback: MutableSet<Pair<String, Int>> = mutableSetOf()
    var isInitialized: Boolean = false


    /**
     * Initializes the map with feedback sent from Artemis and notifies all [OrionAssessmentEditor]s
     *
     * @param submissionId to check validity against
     * @param feedback to load
     */
    abstract fun initializeFeedback(submissionId: Long, feedback: Array<Feedback>)


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
        closeEditors(false)
        feedbackPerFile = mutableMapOf()
        isInitialized = false
    }

    /**
     * Closes the assessmentEditor
     * @param reopen specifies if the editor should be opened again after closing
     */
    fun closeEditors(reopen: Boolean) {
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
