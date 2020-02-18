package de.tum.www1.orion.connector.ide.vcs.submit;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import de.tum.www1.orion.enumeration.ExerciseView;
import de.tum.www1.orion.util.registry.OrionStudentExerciseRegistry;

public class ChangeSubmissionContext {
    private ChangeSubmissionStrategy submissionStrategy;
    private final Project project;

    public ChangeSubmissionContext(Project project) {
        this.project = project;
    }

    public void determineSubmissionStrategy() {
        final var currentView = ServiceManager.getService(project, OrionStudentExerciseRegistry.class).getCurrentView();
        this.submissionStrategy = currentView == ExerciseView.STUDENT ? new StudentChangeSubmissionStrategy(project)
                : new InstructorChangeSubmissionStrategy(project);
    }

    public void submitChanges() {
        this.submissionStrategy.submitChanges();
    }
}
