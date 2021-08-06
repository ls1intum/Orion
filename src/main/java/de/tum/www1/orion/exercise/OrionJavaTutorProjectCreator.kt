package de.tum.www1.orion.exercise

import com.intellij.execution.RunManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFileManager
import de.tum.www1.orion.build.instructor.OrionLocalRunConfigurationSettingsFactory
import de.tum.www1.orion.ui.util.notify
import de.tum.www1.orion.util.OrionAssessmentUtils.getAssignmentOf
import de.tum.www1.orion.util.translate

/**
 * Utilities to configure a tutor project
 */
object OrionJavaTutorProjectCreator {
    private const val SRC = "src"

    /**
     * Does the configuration steps that do not work automatically
     * Adds the maven run configuration
     *
     * @param project project to configure
     */
    fun prepareProjectAfterImport(project: Project) {
        runInEdt {
            WriteAction.run<Throwable> {
                configureRunConfiguration(project)
            }
        }
    }

    private fun configureRunConfiguration(project: Project) {
        val runConfiguration = OrionLocalRunConfigurationSettingsFactory.runConfigurationForTutor(project)
        runConfiguration.storeInDotIdeaFolder()
        RunManager.getInstance(project).addConfiguration(runConfiguration)
    }

    /**
     * Adds the assignment/src directory to project sources, necessary to use autocompletion
     * Done when loading a new submission to ensure the project is properly loaded
     *
     * Needs to be called from a [WriteAction]
     *
     * @param project project to configure
     */
    fun configureModules(project: Project) {
        val modules = ModuleManager.getInstance(project).modules
        if (modules.size != 1) {
            project.notify(translate("orion.error.exercise.moduleFailed"))
            return
        }

        val model = ModuleRootManager.getInstance(modules[0]).modifiableModel

        val roots = model.contentEntries
        if (roots.size != 1) {
            project.notify(translate("orion.error.exercise.moduleFailed"))
            return
        }

        val assessmentSrc =
            VirtualFileManager.getInstance().refreshAndFindFileByNioPath(getAssignmentOf(project).resolve(SRC))
                ?: return Unit.also {
                    project.notify(translate("orion.error.exercise.moduleFailed"))
                }
        roots[0].addSourceFolder(assessmentSrc, false)
        model.commit()
    }
}
