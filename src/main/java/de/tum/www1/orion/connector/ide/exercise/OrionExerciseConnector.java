package de.tum.www1.orion.connector.ide.exercise;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import de.tum.www1.orion.connector.ide.OrionConnector;
import de.tum.www1.orion.dto.ProgrammingExercise;
import de.tum.www1.orion.exercise.OrionExerciseService;
import de.tum.www1.orion.util.JsonUtils;

public class OrionExerciseConnector extends OrionConnector implements IOrionExerciseConnector {

    public OrionExerciseConnector(Project project) {
        super(project);
    }

    @Override
    public void editExercise(String exerciseJson) {
        final var exercise = JsonUtils.INSTANCE.gson().fromJson(exerciseJson, ProgrammingExercise.class);
        ServiceManager.getService(project, OrionExerciseService.class).editExercise(exercise);
    }

    @Override
    public void importParticipation(String repositoryUrl, String exerciseJson) {
        final var exercise = JsonUtils.INSTANCE.gson().fromJson(exerciseJson, ProgrammingExercise.class);
        ServiceManager.getService(project, OrionExerciseService.class).importParticipation(repositoryUrl, exercise);
    }
}
