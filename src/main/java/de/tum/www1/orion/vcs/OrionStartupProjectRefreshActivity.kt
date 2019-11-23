package de.tum.www1.orion.vcs

import com.intellij.dvcs.repo.VcsRepositoryManager
import com.intellij.dvcs.repo.VcsRepositoryMappingListener
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import de.tum.www1.orion.bridge.ArtemisBridge
import de.tum.www1.orion.util.OrionInstructorExerciseRegistry
import de.tum.www1.orion.util.OrionStudentExerciseRegistry
import de.tum.www1.orion.util.appService
import de.tum.www1.orion.util.registry.OrionGlobalExerciseRegistryService
import de.tum.www1.orion.util.registry.OrionProjectRegistryStateService
import de.tum.www1.orion.util.service

class OrionStartupProjectRefreshActivity : StartupActivity {

    /**
     * Runs all pending jobs on opening a programming exercise project. For now, this includes:
     * - Registering the opened exercise in the registry
     * - Pull all changes from the remote
     * - Tell the ArTEMiS webapp that a new exercise was opened
     */
    override fun runActivity(project: Project) {
        project.service(OrionProjectRegistryStateService::class.java).importIfPending()
        appService(OrionGlobalExerciseRegistryService::class.java).cleanup()
        val registry = ServiceManager.getService(project, OrionStudentExerciseRegistry::class.java)
        if (registry.isArtemisExercise) {
            val instructorRegistry = ServiceManager.getService(project, OrionInstructorExerciseRegistry::class.java)
            if (instructorRegistry.isOpenedAsInstructor) {
                instructorRegistry.registerPendingExercises()
                ServiceManager.getService(project, ArtemisBridge::class.java).onOpenedExerciseAsInstructor(instructorRegistry.exerciseId)
            } else {
                prepareStudentExercise(registry, project)
            }
        }
    }

    private fun prepareStudentExercise(registry: OrionStudentExerciseRegistry, project: Project) {
        registry.registerPendingExercises()
        ServiceManager.getService(project, DumbService::class.java).runWhenSmart {
            project.messageBus.connect().subscribe(VcsRepositoryManager.VCS_REPOSITORY_MAPPING_UPDATED, VcsRepositoryMappingListener {
                OrionGitUtil.pull(project)
            })
        }
        ServiceManager.getService(project, ArtemisBridge::class.java).onOpenedExercise(registry.exerciseId)
    }
}