package de.tum.www1.orion.exercise.registry

import de.tum.www1.orion.enumeration.ExerciseView
import de.tum.www1.orion.enumeration.ProgrammingLanguage

data class ImportedExercise(
    val courseId: Long,
    val exerciseId: Long,
    val courseTitle: String,
    val exerciseTitle: String,
    val view: ExerciseView,
    val language: ProgrammingLanguage,
    val templateParticipationId: Long?,
    val solutionParticipationId: Long?,
    val exerciseGroupId: Long?,
    val examId: Long?,
)
