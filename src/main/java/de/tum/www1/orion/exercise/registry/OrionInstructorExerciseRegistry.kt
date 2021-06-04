package de.tum.www1.orion.exercise.registry

import de.tum.www1.orion.dto.RepositoryType

/**
 * Registry for instructor exercises
 */
interface OrionInstructorExerciseRegistry : OrionExerciseRegistry {
    var selectedRepository: RepositoryType?
}
