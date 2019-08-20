package de.tum.www1.artemis.plugin.intellij.ui

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import de.tum.www1.artemis.plugin.intellij.util.ArtemisExerciseRegistry

class ArtemisRouterService(private val project: Project): ArtemisRouter {

    override fun routeForCurrentExercise(): String? {
        val registry = ServiceManager.getService(project, ArtemisExerciseRegistry::class.java)
        return EXERCISE_DETAIL_URL.format(registry.courseId, registry.exerciseId)
    }

    companion object {
        private const val EXERCISE_DETAIL_URL = "https://artemistest.ase.in.tum.de/#/overview/%d/exercises/%d"

        @JvmStatic
        fun getInstance(project: Project): ArtemisRouterService {
            return ServiceManager.getService(project, ArtemisRouterService::class.java)
        }
    }
}
