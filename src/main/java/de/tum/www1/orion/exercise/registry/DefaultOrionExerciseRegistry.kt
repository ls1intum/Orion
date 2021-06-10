package de.tum.www1.orion.exercise.registry

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import de.tum.www1.orion.dto.ProgrammingExercise
import de.tum.www1.orion.dto.RepositoryType
import de.tum.www1.orion.enumeration.ExerciseView
import de.tum.www1.orion.util.appService
import org.jetbrains.annotations.SystemIndependent

/**
 * Service to access data from [OrionProjectRegistryStateService.myState]
 *
 * @property project project the state belongs to
 */
abstract class DefaultOrionExerciseRegistry(protected val project: Project) : OrionExerciseRegistry {
    override val isArtemisExercise: Boolean
        get() {
            val isArtemisExercise = project.service<OrionProjectRegistryStateService>().isArtemisExercise
            if (isArtemisExercise && !appService(OrionGlobalExerciseRegistryService::class.java)
                    .isImported(exerciseInfo!!.exerciseId, exerciseInfo!!.currentView)
            ) {
                // Broken link between global registry and local info
                throw BrokenRegistryLinkException(
                    "Exercise ${exerciseInfo!!.exerciseId} is imported, but not linked " +
                            "in global registry!"
                )
            }

            return isArtemisExercise
        }

    override val exerciseInfo: OrionProjectRegistryStateService.State?
        get() = project.service<OrionProjectRegistryStateService>().state

    override val currentView: ExerciseView?
        get() = project.service<OrionProjectRegistryStateService>().state?.currentView

    override val pathForCurrentExercise: String
        get() = appService(OrionGlobalExerciseRegistryService::class.java).pathForImportedExercise

    override fun alreadyImported(exerciseId: Long, view: ExerciseView): Boolean =
        appService(OrionGlobalExerciseRegistryService::class.java).isImported(exerciseId, view)

    override fun onNewExercise(exercise: ProgrammingExercise, view: ExerciseView, path: @SystemIndependent String) {
        appService(OrionGlobalExerciseRegistryService::class.java).registerExercise(exercise, view, path)
    }

    override fun relinkExercise() {
        appService(OrionGlobalExerciseRegistryService::class.java).relinkExercise(
            exerciseInfo!!.exerciseId,
            exerciseInfo!!.currentView, project.basePath
        )
    }

    protected fun getState(): OrionProjectRegistryStateService.State? {
        return project.service<OrionProjectRegistryStateService>().state
    }
}

/**
 * Registry for student exercises, currently no special properties
 */
class DefaultOrionStudentExerciseRegistry(project: Project) : DefaultOrionExerciseRegistry(project),
    OrionStudentExerciseRegistry

/**
 * Registry for instructor exercises
 */
class DefaultOrionInstructorExerciseRegistry(project: Project) : DefaultOrionExerciseRegistry(project),
    OrionInstructorExerciseRegistry {
    override var selectedRepository: RepositoryType?
        get() = getState()?.selectedRepository
        set(value) {
            if (value != null) {
                getState()?.selectedRepository = value
            }
        }
}

/**
 * Registry for tutor exercises
 */
class DefaultOrionTutorExerciseRegistry(project: Project) : DefaultOrionExerciseRegistry(project),
    OrionTutorExerciseRegistry {
    override val submissionId: Long?
        get() = getState()?.submissionId

    override val correctionRound: Long?
        get() = getState()?.correctionRound

    override fun setSubmission(submissionId: Long?, correctionRound: Long?) {
        getState()?.submissionId = submissionId
        getState()?.correctionRound = correctionRound
    }
}

