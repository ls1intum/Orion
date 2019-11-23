package de.tum.www1.orion.util.registry;

import com.intellij.openapi.components.*;
import de.tum.www1.orion.dto.ProgrammingExercise;
import de.tum.www1.orion.enumeration.ExerciseView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@State(name = "registeredExercises", storages = @Storage(value = "orionRegistry.xml", roamingType = RoamingType.DISABLED))
public class OrionGlobalExerciseRegistryService implements PersistentStateComponent<OrionGlobalExerciseRegistryService.State> {
    private State myState;

    public static OrionGlobalExerciseRegistryService getInstance() {
        return ServiceManager.getService(OrionGlobalExerciseRegistryService.class);
    }

    public static class State {
        public Map<Long, String> instructorImports;
        public Map<Long, String> studentImports;
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

    public void registerExercise(ProgrammingExercise exercise, ExerciseView view, Path path) {
        myState = new State();
        Map<Long, String> importMap = view == ExerciseView.INSTRUCTOR ? myState.instructorImports : myState.studentImports;
        if (importMap == null) importMap = new HashMap<>();
        importMap.put(exercise.getId(), path.toString());
    }

    public void cleanup() {
        cleanMap(myState.instructorImports);
        cleanMap(myState.studentImports);
    }

    private void cleanMap(@Nullable final Map<Long, String> toBeCleaned) {
        if (toBeCleaned != null) {
            final var cleaned = toBeCleaned.entrySet().stream()
                    .filter(entry -> Files.exists(Path.of(entry.getValue())))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            toBeCleaned.clear();
            toBeCleaned.putAll(cleaned);
        }
    }

    public boolean isImported(final long id, ExerciseView view) {
        return mapContainsOrRemove(id, view == ExerciseView.INSTRUCTOR ? myState.instructorImports : myState.studentImports);
    }

    private boolean mapContainsOrRemove(final long id, @Nullable final Map<Long, String> map) {
        if (map != null && map.containsKey(id)) {
            if (!Files.exists(Path.of(map.get(id)))) {
                map.remove(id);
                return false;
            }

            return true;
        }

        return false;
    }
}
