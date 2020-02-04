package de.tum.www1.orion.bridge.submit;

import com.intellij.openapi.project.Project;
import de.tum.www1.orion.vcs.OrionGitUtil;

public class StudentChangeSubmissionStrategy implements ChangeSubmissionStrategy {
    private final Project project;

    public StudentChangeSubmissionStrategy(Project project) {
        this.project = project;
    }

    @Override
    public void submitChanges() {
        OrionGitUtil.INSTANCE.submit(project, true);
    }
}
