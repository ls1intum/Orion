package de.tum.www1.orion.connector.ide.vcs.submit;

import com.intellij.openapi.project.Project;
import de.tum.www1.orion.vcs.OrionGitAdapter;

public class StudentChangeSubmissionStrategy implements ChangeSubmissionStrategy {
    private final Project project;

    public StudentChangeSubmissionStrategy(Project project) {
        this.project = project;
    }

    @Override
    public void submitChanges() {
        OrionGitAdapter.INSTANCE.submit(project, true);
    }
}
