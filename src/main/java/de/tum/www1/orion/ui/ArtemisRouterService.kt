package de.tum.www1.orion.ui

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import de.tum.www1.orion.ui.settings.ArtemisSettingsProvider
import de.tum.www1.orion.util.ArtemisExerciseRegistry

class ArtemisRouterService(private val project: Project): ArtemisRouter {

    override fun routeForCurrentExercise(): String? {
        val registry = ServiceManager.getService(project, ArtemisExerciseRegistry::class.java)
        return if (registry.isArtemisExercise) {
            "${defaultRoute()}$EXERCISE_DETAIL_URL".format(registry.courseId, registry.exerciseId)
        } else {
            null
        }
    }

    override fun defaultRoute(): String =
            ServiceManager.getService(ArtemisSettingsProvider::class.java).getSetting(ArtemisSettingsProvider.KEYS.ARTEMIS_URL)

    companion object {
        private const val EXERCISE_DETAIL_URL = "/#/overview/%d/exercises/%d"

        @JvmStatic
        fun getInstance(project: Project): ArtemisRouterService {
            return ServiceManager.getService(project, ArtemisRouterService::class.java)
        }
    }
}
