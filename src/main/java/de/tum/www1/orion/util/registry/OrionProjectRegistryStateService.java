package de.tum.www1.orion.util.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.application.ActionsKt;
import com.intellij.openapi.components.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import de.tum.www1.orion.dto.RepositoryType;
import de.tum.www1.orion.enumeration.ExerciseView;
import de.tum.www1.orion.util.OrionFileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

@State(name = "orionRegistry", storages = @Storage(StoragePathMacros.WORKSPACE_FILE))
public class OrionProjectRegistryStateService implements PersistentStateComponent<OrionProjectRegistryStateService.State> {
    private static final Logger log = Logger.getInstance(OrionProjectRegistryStateService.class);
    private State myState;
    private Project myProject;

    public OrionProjectRegistryStateService(Project project) {
        this.myProject = project;
    }

    public static OrionProjectRegistryStateService getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, OrionProjectRegistryStateService.class);
    }

    public static class State {
        public long courseId;
        public String courseTitle;
        public long exerciseId;
        public String exerciseTitle;
        @Nullable
        public RepositoryType selectedRepository;
        public ExerciseView view;
    }

    @Nullable
    @Override
    public State getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        myState = state;
    }

    public void importIfPending() {
        final var pendingImportFile = VfsUtil.findRelativeFile(OrionFileUtils.INSTANCE.getRoot(myProject), ".artemisExercise.json");
        if (pendingImportFile != null) {
            try {
                final var imported = new ObjectMapper().readValue(pendingImportFile.getInputStream(), ImportedExercise.class);
                myState = new State();
                myState.courseId = imported.getCourseId();
                myState.exerciseId = imported.getExerciseId();
                myState.courseTitle = imported.getCourseTitle();
                myState.exerciseTitle = imported.getExerciseTitle();
                myState.view = imported.getView();
                if (myState.view == ExerciseView.INSTRUCTOR) myState.selectedRepository = RepositoryType.TEST;  // init

                ActionsKt.runWriteAction(() -> {
                    try {
                        pendingImportFile.delete(this);
                    } catch (IOException e) {
                        log.error(e.getMessage(),  e);
                    }

                    return null;
                });
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public boolean isArtemisExercise() {
        return myState != null;
    }

    public ExerciseView getCurrentView() {
        return myState.view;
    }
}
