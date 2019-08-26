package de.tum.www1.orion.util;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public interface ArtemisExerciseRegistry {
    /**
     * Adds a newly cloned/imported exercise for registration
     *
     * @param courseId The ID of the course, in which the exercise is registered
     * @param exerciseId The ID of the imported exercise
     * @param exerciseName The name of the imported exercise
     */
    void onNewExercise(int courseId, int exerciseId, String exerciseName);

    /**
     * Registers the currently opened exercise in the project properties of the current project. This is the case
     * if an exercise gets opened in the IDE for the first time
     */
    void registerPendingExercises();

    /**
     * Is the currently opened project an ArTEMiS exercise?
     *
     * @return True, if the currently opened project refers to an ArTEMiS programming exercise, otherwise false
     */
    boolean isArtemisExercise();

    /**
     * Get the ID of the currently opened exercise
     *
     * @return The ID of the exercise, that is opened in the project of the IDE
     */
    int getExerciseId();

    /**
     * Get the name of the currently opened exercise
     *
     * @return The name of the exercise, that is opened in the project of the IDE
     */
    String getExerciseName();

    /**
     * Get the ID of the course of the exercise that is currently opened
     *
     * @return The ID of the course of the opened exercise
     */
    int getCourseId();

    static ArtemisExerciseRegistry getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, ArtemisExerciseRegistry.class);
    }
}
