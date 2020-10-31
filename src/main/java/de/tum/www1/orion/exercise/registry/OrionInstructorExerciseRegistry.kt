package de.tum.www1.orion.exercise.registry

import de.tum.www1.orion.dto.RepositoryType

interface OrionInstructorExerciseRegistry : OrionExerciseRegistry {
    val isOpenedAsInstructor: Boolean

    var selectedRepository: RepositoryType?
}
