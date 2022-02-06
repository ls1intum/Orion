package de.tum.www1.orion

import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import de.tum.www1.orion.connector.client.JavaScriptConnector
import de.tum.www1.orion.connector.ide.vcs.submit.ChangeSubmissionContext
import de.tum.www1.orion.enumeration.ExerciseView
import de.tum.www1.orion.exercise.OrionExerciseService
import de.tum.www1.orion.exercise.registry.BrokenRegistryLinkException
import de.tum.www1.orion.exercise.registry.OrionGlobalExerciseRegistryService
import de.tum.www1.orion.exercise.registry.OrionProjectRegistryStateService
import de.tum.www1.orion.exercise.registry.OrionStudentExerciseRegistry
import de.tum.www1.orion.ui.util.BrokenLinkWarning
import de.tum.www1.orion.ui.util.notify
import de.tum.www1.orion.util.OrionAssessmentUtils
import de.tum.www1.orion.util.appService
import de.tum.www1.orion.util.translate

class OrionStartupProjectRefreshActivity : StartupActivity, DumbAware {

    /**
     * Runs all pending jobs on opening a programming exercise project. For now, this includes:
     * - Registering the opened exercise in the registry
     * - Pull all changes from the remote
     * - Tell the Artemis webapp that a new exercise was opened
     */
    override fun runActivity(project: Project) {
        // We need to subscribe to all internal state listeners before any message could potentially be sent
        project.service<JavaScriptConnector>().initIDEStateListeners()
        // If the exercise was opened for the first time
        project.service<OrionProjectRegistryStateService>().importIfPending()
        // Remove all deleted exercises from the registry
        appService(OrionGlobalExerciseRegistryService::class.java).cleanup()
        val registry = project.service<OrionStudentExerciseRegistry>()
        try {
            if (registry.isArtemisExercise) {
                prepareExercise(registry, project)
                project.service<ChangeSubmissionContext>().determineSubmissionStrategy()
            }
        } catch (e: BrokenRegistryLinkException) {
            // Ask the user if he wants to relink the exercise in the global registry
            if (invokeAndWaitIfNeeded { BrokenLinkWarning(project).showAndGet() }) {
                registry.relinkExercise()
                prepareExercise(registry, project)
                project.service<ChangeSubmissionContext>().determineSubmissionStrategy()
            }
        }
    }

    private fun prepareExercise(registry: OrionStudentExerciseRegistry, project: Project) {
        registry.exerciseInfo?.let { exerciseInfo ->
            // ensure that the state information is consistent
            if (exerciseInfo.courseId == 0L || exerciseInfo.exerciseId == 0L || exerciseInfo.courseTitle == null) {
                project.notify(translate("orion.error.outdatedartemisfolder"))
                return
            }
            when (exerciseInfo.currentView) {
                ExerciseView.TUTOR -> OrionAssessmentUtils.configureEditorsForAssessment(project)
                else -> Unit
            }
            project.service<OrionExerciseService>().updateExercise()
        }
    }
}
