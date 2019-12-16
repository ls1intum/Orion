package de.tum.www1.orion

import com.intellij.dvcs.repo.VcsRepositoryManager
import com.intellij.dvcs.repo.VcsRepositoryMappingListener
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.testFramework.runInEdtAndGet
import de.tum.www1.orion.bridge.downcall.ArtemisJavascriptDowncallBridge
import de.tum.www1.orion.bridge.submit.ChangeSubmissionContext
import de.tum.www1.orion.dto.RepositoryType
import de.tum.www1.orion.enumeration.ExerciseView
import de.tum.www1.orion.messaging.OrionIntellijStateNotifier
import de.tum.www1.orion.tips.OrionTipsDialog
import de.tum.www1.orion.ui.util.BrokenLinkWarning
import de.tum.www1.orion.util.appService
import de.tum.www1.orion.util.registry.*
import de.tum.www1.orion.util.service
import de.tum.www1.orion.vcs.OrionGitUtil

class OrionStartupProjectRefreshActivity : StartupActivity {

    /**
     * Runs all pending jobs on opening a programming exercise project. For now, this includes:
     * - Registering the opened exercise in the registry
     * - Pull all changes from the remote
     * - Tell the ArTEMiS webapp that a new exercise was opened
     */
    override fun runActivity(project: Project) {
        // If the exercise was opened for the first time
        project.service(OrionProjectRegistryStateService::class.java).importIfPending()
        // Remove all deleted exercises from the registry
        appService(OrionGlobalExerciseRegistryService::class.java).cleanup()
        val registry = ServiceManager.getService(project, OrionStudentExerciseRegistry::class.java)
        try {
            if (registry.isArtemisExercise) {
                // We need to subscribe to all internal state listeners before any message could potentially be sent
                project.service(ArtemisJavascriptDowncallBridge::class.java).initStateListeners()
                prepareExercise(registry, project)
                project.service(ChangeSubmissionContext::class.java).determineSubmissionStrategy()
            }
        } catch (e: BrokenRegistryLinkException) {
            // Ask the user if he wants to relink the exercise in the global registry
            if (runInEdtAndGet { BrokenLinkWarning(project).showAndGet() }) {
                registry.relinkExercise()
                prepareExercise(registry, project)
                project.service(ChangeSubmissionContext::class.java).determineSubmissionStrategy()
            }
        }

        OrionTipsDialog.showForProject(project)
    }

    private fun prepareExercise(registry: OrionStudentExerciseRegistry, project: Project) {
        registry.exerciseInfo?.let { exerciseInfo ->
            project.messageBus.syncPublisher(OrionIntellijStateNotifier.INTELLIJ_STATE_TOPIC).openedExercise(exerciseInfo.exerciseId, exerciseInfo.view)
            updateExercise(registry, project)
        }
    }

    private fun updateExercise(registry: OrionExerciseRegistry, project: Project) {
        project.service(DumbService::class.java).runWhenSmart {
            project.messageBus.connect().subscribe(VcsRepositoryManager.VCS_REPOSITORY_MAPPING_UPDATED, VcsRepositoryMappingListener {
                if (registry.exerciseInfo?.view != ExerciseView.INSTRUCTOR) {
                    OrionGitUtil.pull(project)
                } else {
                    listOf(RepositoryType.TEST, RepositoryType.SOLUTION, RepositoryType.TEMPLATE)
                            .mapNotNull { it.moduleIn(project) }
                            .forEach { OrionGitUtil.pull(it) }
                }
            })

        }
    }
}
