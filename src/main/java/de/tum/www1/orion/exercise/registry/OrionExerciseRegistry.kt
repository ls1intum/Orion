package de.tum.www1.orion.exercise.registry

import de.tum.www1.orion.dto.ProgrammingExercise
import de.tum.www1.orion.enumeration.ExerciseView
import org.jetbrains.annotations.SystemIndependent

class BrokenRegistryLinkException(message: String?) : Exception(message)

interface OrionExerciseRegistry {

    /**
     * Is the currently opened project an Artemis exercise?
     *
     * @return True, if the currently opened project refers to an Artemis programming exercise, otherwise false
     */
    val isArtemisExercise: Boolean

    val exerciseInfo: OrionProjectRegistryStateService.State?

    val currentView: ExerciseView?

    val pathForCurrentExercise: String

    fun relinkExercise()

    /**
     * Adds a newly cloned/imported exercise for registration
     *
     * @param exercise The newly imported exercise
     */
    fun onNewExercise(exercise: ProgrammingExercise, view: ExerciseView, path: @SystemIndependent String)

    /**
     * Has the specified programming exercise already imported into IntelliJ? This is the case if the exercise has
     * been cloned and not opened and if it has been opened.
     *
     * @param exerciseId The ID of the exercise
     * @return True, if the exercise has already been cloned, false otherwise
     */
    fun alreadyImported(exerciseId: Long, view: ExerciseView): Boolean
}
