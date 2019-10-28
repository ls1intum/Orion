package de.tum.www1.orion.util;

import de.tum.www1.orion.dto.ProgrammingExerciseDTO;

public interface OrionExerciseRegistry {
    /**
     * Adds a newly cloned/imported exercise for registration
     *
     * @param courseId The ID of the course, in which the exercise is registered
     * @param exerciseId The ID of the imported exercise
     * @param exerciseName The name of the imported exercise
     */
    void onNewExercise(long courseId, long exerciseId, String exerciseName);

    /**
     * Adds a newly cloned/imported exercise for registration
     *
     * @param exercise The newly imported exercise
     */
    void onNewExercise(ProgrammingExerciseDTO exercise);

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
    long getExerciseId();

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
    long getCourseId();

    /**
     * Has the specified programming exercise already imported into IntelliJ? This is the case if the exercise has
     * been cloned and not opened and if it has been opened.
     *
     * @param exerciseId The ID of the exercise
     * @return True, if the exercise has already been cloned, false otherwise
     */
    boolean alreadyImported(long exerciseId);
}
