package de.tum.www1.orion.util.registry

import de.tum.www1.orion.enumeration.ExerciseView

data class ImportedExercise(val courseId: Long, val exerciseId: Long, val courseTitle: String,
                            val exerciseTitle: String, val view: ExerciseView)