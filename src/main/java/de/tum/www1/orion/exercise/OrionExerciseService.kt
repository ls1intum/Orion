package de.tum.www1.orion.exercise

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import de.tum.www1.orion.connector.ide.exercise.OrionExerciseConnector
import de.tum.www1.orion.dto.ProgrammingExercise
import de.tum.www1.orion.enumeration.ExerciseView
import de.tum.www1.orion.exercise.OrionJavaInstructorProjectCreator.prepareProjectForImport
import de.tum.www1.orion.exercise.registry.OrionGlobalExerciseRegistryService
import de.tum.www1.orion.messaging.OrionIntellijStateNotifier
import de.tum.www1.orion.ui.util.ImportPathChooser
import de.tum.www1.orion.ui.util.notify
import de.tum.www1.orion.util.OrionProjectUtil.newEmptyProject
import de.tum.www1.orion.vcs.OrionGitAdapter
import de.tum.www1.orion.vcs.OrionGitAdapter.clone
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException

class OrionExerciseService(private val project: Project) {
    private fun createProject(
        exercise: ProgrammingExercise,
        exerciseView: ExerciseView,
        cloneFunction: (chosenPath: String, globalRegistry: OrionGlobalExerciseRegistryService) -> Unit
    ) {
        val registry = service<OrionGlobalExerciseRegistryService>()
        if (!registry.isImported(exercise.id, exerciseView)) {
            runInEdt(ModalityState.NON_MODAL) {
                val chooser = ImportPathChooser(project, exercise, exerciseView)
                if (chooser.showAndGet()) {
                    FileUtil.ensureExists(File(chooser.chosenPath))
                    cloneFunction.invoke(chooser.chosenPath, registry)
                } else {
                    project.messageBus.syncPublisher(OrionIntellijStateNotifier.INTELLIJ_STATE_TOPIC).isCloning(false)
                }
            }
        } else {
            project.messageBus.syncPublisher(OrionIntellijStateNotifier.INTELLIJ_STATE_TOPIC).isCloning(false)
            val exercisePath = registry.getPathForImportedExercise(exercise.id, exerciseView)
            invokeLater { ProjectUtil.openOrImport(exercisePath, project, false) }
        }
    }

    fun editExercise(exercise: ProgrammingExercise) {
        createProject(exercise, ExerciseView.INSTRUCTOR) { chosenPath, registry ->
            try {
                // Create a new empty project
                val projectPath = newEmptyProject(exercise.title, chosenPath)!!.basePath!!
                // Clone all base repositories
                clone(
                    project, exercise.templateParticipation.repositoryUrl.toString(),
                    projectPath, "$projectPath/exercise", null
                )
                clone(
                    project, exercise.testRepositoryUrl.toString(),
                    projectPath, "$projectPath/tests", null
                )
                clone(
                    project, exercise.solutionParticipation.repositoryUrl.toString(),
                    projectPath, "$projectPath/solution"
                ) {
                    // After cloning all repos, create the necessary project files and notify the webview about the opened project
                    prepareProjectForImport(File(projectPath))
                    registry.registerExercise(exercise, ExerciseView.INSTRUCTOR, chosenPath)
                    ProjectUtil.openOrImport(projectPath, project, false)
                }
            } catch (e: IOException) {
                LoggerFactory.getLogger(OrionExerciseConnector::class.java).error(e.message, e)
                project.notify(e.toString())
            }
        }
    }

    fun importParticipation(repositoryUrl: String, exercise: ProgrammingExercise) {
        createProject(exercise, ExerciseView.STUDENT) { chosenPath, registry ->
            val parent = LocalFileSystem.getInstance().refreshAndFindFileByPath(chosenPath)!!.parent.path
            clone(project, repositoryUrl, parent, chosenPath) {
                registry.registerExercise(exercise, ExerciseView.STUDENT, chosenPath)
                ProjectUtil.openOrImport(chosenPath, project, false)
            }
        }
    }

    fun assessExercise(exercise: ProgrammingExercise) {
        createProject(exercise, ExerciseView.TUTOR) { chosenPath, registry ->
            val parent = LocalFileSystem.getInstance().refreshAndFindFileByPath(chosenPath)!!.parent.path
            clone(project, exercise.testRepositoryUrl.toString(), parent, chosenPath) {
                registry.registerExercise(exercise, ExerciseView.TUTOR, chosenPath)
                ProjectUtil.openOrImport(chosenPath, project, false)
            }
        }
    }

    fun updateExercise() = OrionGitAdapter.updateExercise(project)
}
