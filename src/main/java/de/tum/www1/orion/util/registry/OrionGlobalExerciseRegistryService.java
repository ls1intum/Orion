package de.tum.www1.orion.util.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.components.*;
import com.intellij.openapi.diagnostic.Logger;
import de.tum.www1.orion.dto.ProgrammingExercise;
import de.tum.www1.orion.enumeration.ExerciseView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@State(name = "registeredExercises", storages = @Storage(value = "orionRegistry.xml", roamingType = RoamingType.DISABLED))
public class OrionGlobalExerciseRegistryService implements PersistentStateComponent<OrionGlobalExerciseRegistryService.State> {
    private static final Logger log = Logger.getInstance(OrionGlobalExerciseRegistryService.class);

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
        if (myState == null) {
            myState = new State();
            myState.instructorImports = new HashMap<>();
            myState.studentImports = new HashMap<>();
        }
        final var importMap = view == ExerciseView.INSTRUCTOR ? myState.instructorImports : myState.studentImports;
        importMap.put(exercise.getId(), path.toString());

        createImportFileForNewProject(exercise, view, path);
    }

    private void createImportFileForNewProject(ProgrammingExercise exercise, ExerciseView view, Path path) {
        final var info = new HashMap<String, Object>();
        info.put("courseId", exercise.getCourse().getId());
        info.put("exerciseId", exercise.getId());
        info.put("courseTitle", exercise.getCourse().getTitle());
        info.put("exerciseTitle", exercise.getTitle());
        info.put("view", view);

        try {
            new ObjectMapper().writeValue(path.resolve(".artemisExercise.json").toFile(), info);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void cleanup() {
        if (myState != null) {
            cleanMap(myState.instructorImports);
            cleanMap(myState.studentImports);
        }
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
