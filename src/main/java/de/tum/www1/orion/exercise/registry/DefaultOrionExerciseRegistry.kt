package de.tum.www1.orion.exercise.registry

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import de.tum.www1.orion.dto.ProgrammingExercise
import de.tum.www1.orion.dto.RepositoryType
import de.tum.www1.orion.enumeration.ExerciseView
import de.tum.www1.orion.util.appService
import org.jetbrains.annotations.SystemIndependent

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
}

class DefaultOrionStudentExerciseRegistry(project: Project) : DefaultOrionExerciseRegistry(project), OrionStudentExerciseRegistry

class DefaultOrionInstructorExerciseRegistry(project: Project) : DefaultOrionExerciseRegistry(project), OrionInstructorExerciseRegistry {
    override var selectedRepository: RepositoryType?
        get() = project.service<OrionProjectRegistryStateService>().state?.selectedRepository
        set(value) {
            if (value != null) {
                project.service<OrionProjectRegistryStateService>().state?.selectedRepository = value
            }
        }
}

