package de.tum.www1.orion.util.registry

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project

interface OrionStudentExerciseRegistry : OrionExerciseRegistry {
    companion object {
        @JvmStatic
        fun getInstance(project: Project): OrionStudentExerciseRegistry {
            return ServiceManager.getService(project, OrionStudentExerciseRegistry::class.java)
        }
    }
}
