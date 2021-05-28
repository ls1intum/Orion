package de.tum.www1.orion.ui

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import de.tum.www1.orion.enumeration.ExerciseView
import de.tum.www1.orion.exercise.registry.OrionStudentExerciseRegistry
import de.tum.www1.orion.settings.OrionSettingsProvider

class OrionRouterService(private val project: Project) : OrionRouter {
    override fun routeForCurrentExerciseOrDefault(): String {
        val registry = project.service<OrionStudentExerciseRegistry>()
        val defaultRoute = service<OrionSettingsProvider>().getSetting(OrionSettingsProvider.KEYS.ARTEMIS_URL)
        return if (registry.isArtemisExercise) {
            registry.exerciseInfo?.let {
                return when (it.currentView) {
                    ExerciseView.INSTRUCTOR ->
                        "${defaultRoute}$CODE_EDITOR_INSTRUCTOR_URL".format(
                            it.courseId,
                            it.exerciseId,
                            it.templateParticipationId
                        )
                    ExerciseView.TUTOR ->
                        "${defaultRoute}$ASSESSMENT_DASHBOARD_URL".format(it.courseId, it.exerciseId)
                    ExerciseView.STUDENT ->
                        "${defaultRoute}$EXERCISE_DETAIL_URL".format(it.courseId, it.exerciseId)
                }
            } ?: defaultRoute
        } else {
            defaultRoute
        }
    }

    companion object {
        private const val EXERCISE_DETAIL_URL = "/#/courses/%d/exercises/%d"
        private const val CODE_EDITOR_INSTRUCTOR_URL =
            "/#/course-management/%d/programming-exercises/%d/code-editor/ide/%d"
        private const val ASSESSMENT_DASHBOARD_URL = "/#/course-management/%d/assessment-dashboard/%d"
    }
}
