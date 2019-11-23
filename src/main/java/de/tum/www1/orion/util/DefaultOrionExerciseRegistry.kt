package de.tum.www1.orion.util

import com.intellij.openapi.project.Project
import de.tum.www1.orion.dto.ProgrammingExercise
import de.tum.www1.orion.dto.RepositoryType
import de.tum.www1.orion.enumeration.ExerciseView
import de.tum.www1.orion.util.registry.OrionGlobalExerciseRegistryService
import de.tum.www1.orion.util.registry.OrionProjectRegistryStateService
import java.nio.file.Path

abstract class DefaultOrionExerciseRegistry(protected val project: Project) : OrionExerciseRegistry {
    override val isArtemisExercise: Boolean
            get() = project.service(OrionProjectRegistryStateService::class.java).isArtemisExercise

    override val exerciseInfo: OrionProjectRegistryStateService.State?
            get() = project.service(OrionProjectRegistryStateService::class.java).state

    override val currentView: ExerciseView
        get() = project.service(OrionProjectRegistryStateService::class.java).currentView

    override fun alreadyImported(exerciseId: Long, view: ExerciseView): Boolean =
            appService(OrionGlobalExerciseRegistryService::class.java).isImported(exerciseId, view)

    override fun onNewExercise(exercise: ProgrammingExercise, view: ExerciseView, path: Path) {
        appService(OrionGlobalExerciseRegistryService::class.java).registerExercise(exercise, view, path)
    }
}

class DefaultOrionStudentExerciseRegistry(project: Project) : DefaultOrionExerciseRegistry(project), OrionStudentExerciseRegistry

class DefaultOrionInstructorExerciseRegistry(project: Project) : DefaultOrionExerciseRegistry(project), OrionInstructorExerciseRegistry {

    override val isOpenedAsInstructor: Boolean
        get() = currentView == ExerciseView.INSTRUCTOR

    override var selectedRepository: RepositoryType
        get() = project.service(OrionProjectRegistryStateService::class.java).state!!.selectedRepository
        set(value) {
            project.service(OrionProjectRegistryStateService::class.java).state!!.selectedRepository = value
        }
}

