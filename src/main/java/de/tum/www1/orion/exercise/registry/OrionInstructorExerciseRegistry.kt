package de.tum.www1.orion.exercise.registry

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import de.tum.www1.orion.dto.RepositoryType

interface OrionInstructorExerciseRegistry : OrionExerciseRegistry {
    val isOpenedAsInstructor: Boolean

    var selectedRepository: RepositoryType

    companion object {
        @JvmStatic
        fun getInstance(project: Project): OrionInstructorExerciseRegistry {
            return ServiceManager.getService(project, OrionInstructorExerciseRegistry::class.java)
        }
    }
}
