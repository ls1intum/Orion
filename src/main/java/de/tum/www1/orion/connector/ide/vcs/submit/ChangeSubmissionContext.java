package de.tum.www1.orion.connector.ide.vcs.submit;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import de.tum.www1.orion.exercise.registry.OrionStudentExerciseRegistry;

public class ChangeSubmissionContext {
    private ChangeSubmissionStrategy submissionStrategy;
    private final Project project;

    public static class InvalidSubmissionAttemptException extends RuntimeException {
        InvalidSubmissionAttemptException(String message) {
            super(message);
        }
    }

    public ChangeSubmissionContext(Project project) {
        this.project = project;
    }

    public void determineSubmissionStrategy() {
        final var currentView = ServiceManager.getService(project, OrionStudentExerciseRegistry.class).getCurrentView();
        if (currentView != null) {
            switch (currentView) {
                case STUDENT:
                    this.submissionStrategy = new StudentChangeSubmissionStrategy(project);
                    break;
                case INSTRUCTOR:
                    this.submissionStrategy = new InstructorChangeSubmissionStrategy(project);
                    break;
                default:
                    // Tutors may never submit
                    this.submissionStrategy = null;
            }
        }
    }

    public boolean submitChanges() {
        if (submissionStrategy != null) {
            return this.submissionStrategy.submitChanges();
        } else {
            throw new InvalidSubmissionAttemptException("Submission impossible");
        }
    }
}
