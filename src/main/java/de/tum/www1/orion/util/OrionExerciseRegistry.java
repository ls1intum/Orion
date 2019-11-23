package de.tum.www1.orion.util;

import de.tum.www1.orion.dto.ProgrammingExercise;

public interface OrionExerciseRegistry {

    /**
     * Adds a newly cloned/imported exercise for registration
     *
     * @param exercise The newly imported exercise
     */
    void onNewExercise(ProgrammingExercise exercise);

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
     * Has the specified programming exercise already imported into IntelliJ? This is the case if the exercise has
     * been cloned and not opened and if it has been opened.
     *
     * @param exerciseId The ID of the exercise
     * @return True, if the exercise has already been cloned, false otherwise
     */
    boolean alreadyImported(long exerciseId);
}
