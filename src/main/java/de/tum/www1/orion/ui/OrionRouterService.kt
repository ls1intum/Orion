package de.tum.www1.orion.ui

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import de.tum.www1.orion.enumeration.ExerciseView
import de.tum.www1.orion.exercise.registry.OrionStudentExerciseRegistry
import de.tum.www1.orion.settings.OrionSettingsProvider

/**
 * Implementation of [OrionRouter]
 *
 * @property project project the router belongs to
 */
class OrionRouterService(private val project: Project) : OrionRouter {
    override fun routeForCurrentExerciseOrDefault(): String {
        val registry = project.service<OrionStudentExerciseRegistry>()
        val defaultRoute = service<OrionSettingsProvider>().getSetting(OrionSettingsProvider.KEYS.ARTEMIS_URL)
        val info = registry.exerciseInfo
        return if (registry.isArtemisExercise && info != null) {
            when (info.currentView) {
                ExerciseView.INSTRUCTOR ->
                    "${defaultRoute}$CODE_EDITOR_INSTRUCTOR_URL".format(
                        info.courseId,
                        info.exerciseId,
                        info.templateParticipationId,
                    )
                ExerciseView.TUTOR ->
                    if (info.submissionId != null && info.correctionRound != null) {
                        "${defaultRoute}$ASSESSMENT_CORRECTION_URL".format(
                            info.courseId,
                            info.exerciseId,
                            info.submissionId,
                            info.correctionRound,
                        )
                    } else {
                        "${defaultRoute}$ASSESSMENT_DASHBOARD_URL".format(info.courseId, info.exerciseId)
                    }
                ExerciseView.STUDENT ->
                    "${defaultRoute}$EXERCISE_DETAIL_URL".format(info.courseId, info.exerciseId)
            }
        } else {
            defaultRoute
        }
    }

    override fun routeForDocumentation(): String {
        return "https://artemis-platform.readthedocs.io/en/latest/user/orion"
    }

    companion object {
        private const val EXERCISE_DETAIL_URL = "/courses/%d/exercises/%d"
        private const val CODE_EDITOR_INSTRUCTOR_URL =
            "/course-management/%d/programming-exercises/%d/code-editor/ide/%d"
        private const val ASSESSMENT_DASHBOARD_URL = "/course-management/%d/assessment-dashboard/%d"
        private const val ASSESSMENT_CORRECTION_URL =
            "/course-management/%d/programming-exercises/%d/submissions/%d/assessment?correction-round=%d"
    }
}
