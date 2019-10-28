package de.tum.www1.orion.ui

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import de.tum.www1.orion.util.OrionInstructorExerciseRegistry
import de.tum.www1.orion.util.OrionSettingsProvider
import de.tum.www1.orion.util.OrionStudentExerciseRegistry

class OrionRouterService(private val project: Project): OrionRouter {

    override fun routeForCurrentExercise(): String? {
        val registry = ServiceManager.getService(project, OrionStudentExerciseRegistry::class.java)
        return if (registry.isArtemisExercise) {
            val instructorRegistry = ServiceManager.getService(project, OrionInstructorExerciseRegistry::class.java)
            if (instructorRegistry.isOpenedAsInstructor) {
                "${defaultRoute()}$CODE_EDITOR_INSTRUCTOR_URL".format(instructorRegistry.exerciseId)
            } else {
                "${defaultRoute()}$EXERCISE_DETAIL_URL".format(registry.courseId, registry.exerciseId)
            }
        } else {
            null
        }
    }

    override fun defaultRoute(): String =
            ServiceManager.getService(OrionSettingsProvider::class.java).getSetting(OrionSettingsProvider.KEYS.ARTEMIS_URL)

    companion object {
        private const val EXERCISE_DETAIL_URL = "/#/overview/%d/exercises/%d"
        private const val CODE_EDITOR_INSTRUCTOR_URL = "/#/code-editor/ide/%d/admin/test"

        @JvmStatic
        fun getInstance(project: Project): OrionRouterService {
            return ServiceManager.getService(project, OrionRouterService::class.java)
        }
    }
}
