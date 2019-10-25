package de.tum.www1.orion.vcs

import com.intellij.dvcs.repo.VcsRepositoryManager
import com.intellij.dvcs.repo.VcsRepositoryMappingListener
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import de.tum.www1.orion.bridge.ArtemisBridge
import de.tum.www1.orion.util.ArtemisExerciseRegistry

class ArtemiStartupProjectRefreshActivity : StartupActivity {

    /**
     * Runs all pending jobs on opening a programming exercise project. For now, this includes:
     * - Registering the opened exercise in the registry
     * - Pull all changes from the remote
     * - Tell the ArTEMiS webapp that a new exercise was opened
     */
    override fun runActivity(project: Project) {
        val registry = ServiceManager.getService(project, ArtemisExerciseRegistry::class.java)
        if (registry.isArtemisExercise) {
            registry.registerPendingExercises()
            ServiceManager.getService(project, DumbService::class.java).runWhenSmart {
                project.messageBus.connect().subscribe(VcsRepositoryManager.VCS_REPOSITORY_MAPPING_UPDATED, VcsRepositoryMappingListener {
                    ArtemisGitUtil.pull(project)
                })
            }
            ServiceManager.getService(project, ArtemisBridge::class.java).onOpenedExercise(registry.exerciseId)
        }
    }
}