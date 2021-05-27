package de.tum.www1.orion.exercise

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import de.tum.www1.orion.connector.ide.exercise.OrionExerciseConnector
import de.tum.www1.orion.dto.ProgrammingExercise
import de.tum.www1.orion.enumeration.ExerciseView
import de.tum.www1.orion.exercise.OrionJavaInstructorProjectCreator.prepareProjectForImport
import de.tum.www1.orion.exercise.registry.OrionGlobalExerciseRegistryService
import de.tum.www1.orion.exercise.registry.OrionInstructorExerciseRegistry
import de.tum.www1.orion.exercise.registry.OrionStudentExerciseRegistry
import de.tum.www1.orion.messaging.OrionIntellijStateNotifier
import de.tum.www1.orion.ui.util.ImportPathChooser
import de.tum.www1.orion.util.OrionProjectUtil.newEmptyProject
import de.tum.www1.orion.util.appService
import de.tum.www1.orion.vcs.OrionGitAdapter
import de.tum.www1.orion.vcs.OrionGitAdapter.clone
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException

/**
 * Provides methods for importing and updating exercises
 */
class OrionExerciseService(private val project: Project) {
    /**
     * Imports an exercise as instructor, clones template, test and solution repository, and does all necessary configuration
     *
     * @param exercise exercise data from Artemis
     */
    fun editExercise(exercise: ProgrammingExercise) {
        val registry = project.service<OrionInstructorExerciseRegistry>()
        if (!registry.alreadyImported(exercise.id, ExerciseView.INSTRUCTOR)) {
            runInEdt(ModalityState.NON_MODAL) {
                val chooser = ImportPathChooser(project, exercise, ExerciseView.INSTRUCTOR)
                if (chooser.showAndGet()) {
                    val path = chooser.chosenPath
                    try {
                        FileUtil.ensureExists(File(path))
                        // Create a new empty project
                        val newProject = newEmptyProject(exercise.title, path)
                        // Clone all base repositories
                        clone(project, exercise.templateParticipation.repositoryUrl.toString(),
                                newProject!!.basePath!!, newProject.basePath + "/exercise", null)
                        clone(project, exercise.testRepositoryUrl.toString(),
                                newProject.basePath!!, newProject.basePath + "/tests", null)
                        clone(project, exercise.solutionParticipation.repositoryUrl.toString(),
                                newProject.basePath!!, newProject.basePath + "/solution") {
                            // After cloning all repos, create the necessary project files and notify the webview about the opened project
                            prepareProjectForImport(File(newProject.basePath!!))
                            registry.onNewExercise(exercise, ExerciseView.INSTRUCTOR, path)
                            ProjectUtil.openOrImport(newProject.basePath!!, project, false)
                        }
                    } catch (e: IOException) {
                        LoggerFactory.getLogger(OrionExerciseConnector::class.java).error(e.message, e)
                    }
                } else {
                    project.messageBus.syncPublisher(OrionIntellijStateNotifier.INTELLIJ_STATE_TOPIC).isCloning(false)
                }
            }
        } else { // Exercise is already imported
            project.messageBus.syncPublisher(OrionIntellijStateNotifier.INTELLIJ_STATE_TOPIC).isCloning(false)
            val exercisePath = ServiceManager.getService(OrionGlobalExerciseRegistryService::class.java).getPathForImportedExercise(exercise.id, ExerciseView.INSTRUCTOR)
            invokeLater { ProjectUtil.openOrImport(exercisePath, project, false) }
        }
    }

    /**
     * Imports an exercise as student, clones the participation and does all required configuration
     *
     * @param repositoryUrl url to the participation
     * @param exercise exercise data from Artemis
     */
    fun importParticipation(repositoryUrl: String, exercise: ProgrammingExercise) {
        val registry = project.service<OrionStudentExerciseRegistry>()
        if (!registry.alreadyImported(exercise.id, ExerciseView.STUDENT)) {
            runInEdt(ModalityState.NON_MODAL) {
                val chooser = ImportPathChooser(project, exercise, ExerciseView.STUDENT)
                if (chooser.showAndGet()) {
                    OrionGitAdapter.cloneAndOpenExercise(project, repositoryUrl, chooser.chosenPath) {
                        registry.onNewExercise(exercise, ExerciseView.STUDENT, chooser.chosenPath)
                    }
                }
            }
        } else {
            appService(OrionGlobalExerciseRegistryService::class.java).getPathForImportedExercise(exercise.id, ExerciseView.STUDENT)
                    .also { invokeLater { ProjectUtil.openOrImport(it, project, false) } }
        }
    }

    /**
     * Updates all exercise files by syncing with the git server
     */
    fun updateExercise() = OrionGitAdapter.updateExercise(project)
}
