package de.tum.www1.orion.exercise.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.application.ActionsKt;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.RoamingType;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Interface to persist data in IntelliJ's global storage using built-in features.
 * The mapping between know exercises and corresponding paths is saved
 */
@State(name = "registeredExercises", storages = @Storage(value = "orionRegistry.xml", roamingType = RoamingType.DISABLED))
public class OrionGlobalExerciseRegistryService implements PersistentStateComponent<OrionGlobalExerciseRegistryService.State> {
    private static final Logger log = Logger.getInstance(OrionGlobalExerciseRegistryService.class);

    private State myState;

    public static class State {
        public Map<Long, String> instructorImports;
        public Map<Long, String> tutorImports;
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
            myState.instructorImports = new ConcurrentHashMap<>();
        }
        if (myState.tutorImports == null) {
            myState.tutorImports = new ConcurrentHashMap<>();
        }
        if (myState.studentImports == null) {
            myState.studentImports = new ConcurrentHashMap<>();
        }
    }

    private void initState() {
        loadState(new State());
    }

    private Map<Long, String> selectMap(ExerciseView view) {
        return switch (view) {
            case STUDENT -> myState.studentImports;
            case TUTOR -> myState.tutorImports;
            case INSTRUCTOR -> myState.instructorImports;
        };
    }

    public void relinkExercise(long id, ExerciseView view, @SystemIndependent String path) {
        if (myState == null) initState();
        selectMap(view).put(id, path);
    }

    public void registerExercise(ProgrammingExercise exercise, ExerciseView view, @SystemIndependent String path) {
        if (myState == null) {
            initState();
        }
        selectMap(view).put(exercise.getId(), path);

        createImportFileForNewProject(exercise, view, path);
    }

    private void createImportFileForNewProject(ProgrammingExercise exercise, ExerciseView view, @SystemIndependent String path) {
        final Long templateParticipationId;
        final Long solutionParticipationId;
        if (view == ExerciseView.INSTRUCTOR) {
            templateParticipationId = exercise.getTemplateParticipation().getId();
            solutionParticipationId = exercise.getSolutionParticipation().getId();
        } else {
            templateParticipationId = null;
            solutionParticipationId = null;
        }
        Long exerciseGroupId = null;
        Long examId = null;
        if (exercise.getExerciseGroup() != null) {
            exerciseGroupId = exercise.getExerciseGroup().getId();
            examId = exercise.getExerciseGroup().getExam().getId();
        }
        final var imported = new ImportedExercise(
                exercise.getCourse().getId(),
                exercise.getId(),
                exercise.getCourse().getTitle(),
                exercise.getTitle(),
                view,
                exercise.getProgrammingLanguage(),
                templateParticipationId,
                solutionParticipationId,
                exerciseGroupId,
                examId
        );

        ActionsKt.runWriteAction(UtilsKt.ktLambda(() -> {
            try {
                final var importFile = Objects.requireNonNull(LocalFileSystem.getInstance().refreshAndFindFileByPath(path)).createChildData(this, ".artemisExercise.json");
                new ObjectMapper().writeValue(importFile.getOutputStream(this), imported);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }));
    }

    public void cleanup() {
        if (myState != null) {
            cleanMap(myState.instructorImports);
            cleanMap(myState.tutorImports);
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
        return myState != null && mapContainsOrRemove(id, selectMap(view));
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

    public String getPathForImportedExercise(Project project) {
        final var info = project.getService(OrionProjectRegistryStateService.class).getState();
        return getPathForImportedExercise(Objects.requireNonNull(info).getExerciseId(), info.getCurrentView());
    }

    public String getPathForImportedExercise(final long id, final ExerciseView view) {
        return selectMap(view).get(id);
    }
}
