package de.tum.www1.orion.util.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.application.ActionsKt;
import com.intellij.openapi.components.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.LocalFileSystem;
import de.tum.www1.orion.dto.ProgrammingExercise;
import de.tum.www1.orion.enumeration.ExerciseView;
import de.tum.www1.orion.util.UtilsKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.SystemIndependent;

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
        if (myState.instructorImports == null) {
            myState.instructorImports = new HashMap<>();
        }
        if (myState.studentImports == null) {
            myState.studentImports = new HashMap<>();
        }
    }

    public void registerExercise(ProgrammingExercise exercise, ExerciseView view, @SystemIndependent String path) {
        if (myState == null) {
            myState = new State();
        }
        final var importMap = view == ExerciseView.INSTRUCTOR ? myState.instructorImports : myState.studentImports;
        importMap.put(exercise.getId(), path);

        createImportFileForNewProject(exercise, view, path);
    }

    private void createImportFileForNewProject(ProgrammingExercise exercise, ExerciseView view, @SystemIndependent String path) {
        final var imported = new ImportedExercise(exercise.getCourse().getId(), exercise.getId(),
                exercise.getCourse().getTitle(), exercise.getTitle(), view);

            ActionsKt.runWriteAction(UtilsKt.ktLambda(() -> {
                try {
                    final var importFile = LocalFileSystem.getInstance().findFileByPath(path).createChildData(this, ".artemisExercise.json");
                    new ObjectMapper().writeValue(importFile.getOutputStream(this), imported);
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }));
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
        return myState != null && mapContainsOrRemove(id, view == ExerciseView.INSTRUCTOR ? myState.instructorImports : myState.studentImports);
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

    public String getPathForImportedExercise() {
        final var info = ServiceManager.getService(OrionProjectRegistryStateService.class).getState();
        return getPathForImportedExercise(info.exerciseId, info.view);
    }

    public String getPathForImportedExercise(final long id, final ExerciseView view) {
        return (view == ExerciseView.INSTRUCTOR ? myState.instructorImports : myState.studentImports).get(id);
    }
}
