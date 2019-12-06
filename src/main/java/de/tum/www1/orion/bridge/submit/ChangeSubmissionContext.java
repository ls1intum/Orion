package de.tum.www1.orion.bridge.submit;

import com.intellij.openapi.project.Project;

public class ChangeSubmissionContext {
    private ChangeSubmissionStrategy submissionStrategy;
    private final Project project;

    public ChangeSubmissionContext(Project project) {
        this.project = project;
    }

    public void determineSubmissionStrategy() {

    }

    public void submitChanges() {
        this.submissionStrategy.submitChanges();
    }
}
