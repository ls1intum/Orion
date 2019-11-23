package de.tum.www1.orion.util.registry;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import de.tum.www1.orion.dto.ProgrammingExercise;
import de.tum.www1.orion.dto.RepositoryType;
import de.tum.www1.orion.enumeration.ExerciseView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "orionRegistry", storages = @Storage(StoragePathMacros.WORKSPACE_FILE))
public class OrionProjectRegistryStateService implements PersistentStateComponent<OrionProjectRegistryStateService.State> {
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

    public void registerExercise(ProgrammingExercise exercise, ExerciseView view) {
        myState = new State();
        myState.courseId = exercise.getCourse().getId();
        myState.exerciseId = exercise.getId();
        myState.courseTitle = exercise.getCourse().getTitle();
        myState.exerciseTitle = exercise.getTitle();
        myState.view = view;
    }

    public ExerciseView getCurrentView() {
        return myState.view;
    }
}
