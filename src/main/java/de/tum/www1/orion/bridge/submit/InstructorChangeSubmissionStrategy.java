package de.tum.www1.orion.bridge.submit;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.LocalFileSystem;
import de.tum.www1.orion.util.registry.OrionInstructorExerciseRegistry;
import de.tum.www1.orion.vcs.OrionGitUtil;

import java.io.File;
import java.util.Objects;

public class InstructorChangeSubmissionStrategy implements ChangeSubmissionStrategy {
    private final Project project;

    public InstructorChangeSubmissionStrategy(Project project) {
        this.project = project;
    }

    @Override
    public void submitChanges() {
        final var repository = ServiceManager.getService(project, OrionInstructorExerciseRegistry.class).getSelectedRepository();
        final var projectDir = new File(Objects.requireNonNull(project.getBasePath()));
        // Always works, since we always have our three base modules for instructors
        final var moduleDir = projectDir.listFiles((file, name) -> name.equals(repository.getDirectoryName()))[0];
        final var moduleFile = LocalFileSystem.getInstance().findFileByIoFile(moduleDir);
        final var module = ServiceManager.getService(project, ProjectFileIndex.class).getModuleForFile(moduleFile);

        OrionGitUtil.INSTANCE.submit(module, true);
    }
}
