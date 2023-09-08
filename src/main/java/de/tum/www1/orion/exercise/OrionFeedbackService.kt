package de.tum.www1.orion.exercise;

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

/**
 * A Feedback service that provides a feedback from tutors for students
 */
@Service(Service.Level.PROJECT)
class OrionFeedbackService(private val project: Project) : OrionInlineCommentService(project = project) {
    override fun beforeFeedbackInitialization(submissionId: Long): Boolean {
        return true
    }
}
