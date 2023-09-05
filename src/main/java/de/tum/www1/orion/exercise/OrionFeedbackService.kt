package de.tum.www1.orion.exercise;

import com.intellij.openapi.project.Project

class OrionFeedbackService(private val project: Project) : OrionInlinecommentService(project = project) {
    override fun beforeFeedbackInitialization(submissionId: Long): Boolean {
        return true
    }
}
