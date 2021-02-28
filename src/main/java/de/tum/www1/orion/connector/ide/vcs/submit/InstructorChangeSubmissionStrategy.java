package de.tum.www1.orion.connector.ide.vcs.submit;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.LocalFileSystem;
import de.tum.www1.orion.exercise.registry.OrionInstructorExerciseRegistry;
import de.tum.www1.orion.vcs.OrionGitAdapter;

import java.io.File;
import java.util.Objects;

public class InstructorChangeSubmissionStrategy implements ChangeSubmissionStrategy {
    private final Project project;

    public InstructorChangeSubmissionStrategy(Project project) {
        this.project = project;
    }

    @Override
    public boolean submitChanges() {
        final var repository = ServiceManager.getService(project, OrionInstructorExerciseRegistry.class).getSelectedRepository();
        final var projectDir = new File(Objects.requireNonNull(project.getBasePath()));
        // Always works, since we always have our three base modules for instructors
        final var moduleDir = Objects.requireNonNull(projectDir.listFiles((file, name) -> name.equals(Objects.requireNonNull(repository).getDirectoryName())))[0];
        final var moduleFile = LocalFileSystem.getInstance().findFileByIoFile(moduleDir);
        final var module = ServiceManager.getService(project, ProjectFileIndex.class).getModuleForFile(Objects.requireNonNull(moduleFile));

        return OrionGitAdapter.INSTANCE.submit(Objects.requireNonNull(module), true);
    }
}
