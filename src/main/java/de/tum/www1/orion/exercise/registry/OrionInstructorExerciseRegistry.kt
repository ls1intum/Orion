package de.tum.www1.orion.exercise.registry

import de.tum.www1.orion.dto.RepositoryType

interface OrionInstructorExerciseRegistry : OrionExerciseRegistry {
    var selectedRepository: RepositoryType?
}
