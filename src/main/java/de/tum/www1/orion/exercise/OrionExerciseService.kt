package de.tum.www1.orion.exercise

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.*
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFileManager
import de.tum.www1.orion.connector.ide.exercise.OrionExerciseConnector
import de.tum.www1.orion.dto.ProgrammingExercise
import de.tum.www1.orion.enumeration.ExerciseView
import de.tum.www1.orion.exercise.OrionJavaInstructorProjectCreator.prepareProjectForImport
import de.tum.www1.orion.exercise.registry.OrionGlobalExerciseRegistryService
import de.tum.www1.orion.exercise.registry.OrionTutorExerciseRegistry
import de.tum.www1.orion.messaging.OrionIntellijStateNotifier
import de.tum.www1.orion.ui.util.ImportPathChooser
import de.tum.www1.orion.ui.util.YesNoChooser
import de.tum.www1.orion.ui.util.notify
import de.tum.www1.orion.util.OrionAssessmentUtils.TEMPLATE
import de.tum.www1.orion.util.OrionAssessmentUtils.getAssignmentOf
import de.tum.www1.orion.util.OrionAssessmentUtils.getStudentSubmissionOf
import de.tum.www1.orion.util.OrionFileUtils.deleteIfExists
import de.tum.www1.orion.util.OrionFileUtils.getUniqueFilename
import de.tum.www1.orion.util.OrionFileUtils.storeBase64asFile
import de.tum.www1.orion.util.OrionFileUtils.unzip
import de.tum.www1.orion.util.OrionFileUtils.unzipSingleEntry
import de.tum.www1.orion.util.OrionProjectUtil.newEmptyProject
import de.tum.www1.orion.util.returnToExercise
import de.tum.www1.orion.util.runWithIndeterminateProgressModal
import de.tum.www1.orion.util.translate
import de.tum.www1.orion.vcs.OrionGitAdapter
import de.tum.www1.orion.vcs.OrionGitAdapter.clone
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Provides methods for importing and updating exercises
 */
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

    /**
     * Imports an exercise as instructor, clones template, test and solution repository, and does all necessary configuration
     *
     * @param exercise exercise data from Artemis
     */
    fun editExercise(exercise: ProgrammingExercise) {
        createProject(exercise, ExerciseView.INSTRUCTOR) { chosenPath, registry ->
            try {
                // Create a new empty project
                val projectPath = newEmptyProject(exercise.title, chosenPath)!!.basePath!!

                exercise.auxiliaryRepositories?.also {
                    project.notify(translate("orion.warning.auxiliaryRepositories"))
                }?.forEach {
                    clone(
                        project,
                        it.repositoryUrl.toString(),
                        projectPath,
                        "$projectPath/${it.checkoutDirectory}",
                        null
                    )
                }
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

    /**
     * Imports an exercise as student, clones the participation and does all required configuration
     *
     * @param repositoryUrl url to the participation
     * @param exercise exercise data from Artemis
     */
    fun importParticipation(repositoryUrl: String, exercise: ProgrammingExercise) {
        createProject(exercise, ExerciseView.STUDENT) { chosenPath, registry ->
            val parent = LocalFileSystem.getInstance().refreshAndFindFileByPath(chosenPath)!!.parent.path
            clone(project, repositoryUrl, parent, chosenPath) {
                registry.registerExercise(exercise, ExerciseView.STUDENT, chosenPath)
                ProjectUtil.openOrImport(chosenPath, project, false)
            }
        }
    }

    /**
     * Imports an exercise as tutor, clones the test and does all required configuration
     *
     * @param exercise
     */
    fun assessExercise(exercise: ProgrammingExercise) {
        createProject(exercise, ExerciseView.TUTOR) { chosenPath, registry ->
            val parent = LocalFileSystem.getInstance().refreshAndFindFileByPath(chosenPath)!!.parent.path
            clone(project, exercise.testRepositoryUrl.toString(), parent, chosenPath) {
                clone(
                    project, exercise.templateParticipation.repositoryUrl.toString(),
                    parent, "$chosenPath/$TEMPLATE"
                ) {
                    registry.registerExercise(exercise, ExerciseView.TUTOR, chosenPath)
                    ProjectUtil.openOrImport(chosenPath, project, false)
                }
            }
        }
    }

    /**
     * Decodes and stores the given participation and updates the exercise registry
     *
     * @param submissionId id of the submission, required to load the correct url
     * @param correctionRound also required to load the url
     * @param base64data base64 encoded zip file from the submission export
     */
    fun downloadSubmission(submissionId: Long, correctionRound: Long, base64data: String) {
        val registry = project.service<OrionTutorExerciseRegistry>()
        if (registry.submissionId != submissionId || registry.correctionRound != correctionRound) {
            runInEdt(ModalityState.NON_MODAL) {
                if (downloadSubmissionInEdt(base64data)) {
                    // Update registry
                    registry.setSubmission(submissionId, correctionRound)
                    returnToExercise(project)
                } else {
                    // The clone state is overridden by the reload in the if case
                    project.messageBus.syncPublisher(OrionIntellijStateNotifier.INTELLIJ_STATE_TOPIC).isCloning(false)
                }
            }
        } else {
            // Return to assessment editor
            returnToExercise(project)
        }
    }

    private fun downloadSubmissionInEdt(base64data: String): Boolean {
        // Confirm action
        if (!invokeAndWaitIfNeeded { YesNoChooser(project, "submissionDeletion").showAndGet() }) {
            return false
        }

        val assignment = getAssignmentOf(project)
        val studentSubmission = getStudentSubmissionOf(project)

        // Delete previous assignment if needed
        if (!deleteIfExists(assignment) || !deleteIfExists(studentSubmission)) {
            project.notify(translate("orion.exercise.submissiondeletionfailed"))
            // Delete known submission to force re-downloading since nothing can be guaranteed about the files
            project.service<OrionTutorExerciseRegistry>().setSubmission(null, null)
            return false
        }

        runWithIndeterminateProgressModal(project, "orion.exercise.submissiondownloading") {
            WriteAction.runAndWait<Throwable> {
                extractSubmission(base64data)
                OrionJavaTutorProjectCreator.configureModules(project)
            }

            // Refresh view
            val virtualAssignment = VirtualFileManager.getInstance().refreshAndFindFileByNioPath(assignment)
            val virtualStudentSubmission =
                VirtualFileManager.getInstance().refreshAndFindFileByNioPath(studentSubmission)
            LocalFileSystem.getInstance()
                .refreshFiles(listOf(virtualAssignment, virtualStudentSubmission), true, true, null)
            project.service<OrionAssessmentService>().reset()
        }
        return true
    }

    private fun extractSubmission(base64data: String) {
        // Decode and save archive of submission data
        val downloadedSubmission =
            getUniqueFilename(Paths.get(project.basePath!!, "downloadedSubmission"), ".zip")
        storeBase64asFile(base64data, downloadedSubmission)

        // Extract submission zip from doubly zipped archive
        val extractedSubmission =
            getUniqueFilename(Paths.get(project.basePath!!, "extractedSubmission"), ".zip")
        unzipSingleEntry(downloadedSubmission, extractedSubmission)

        // Extract submission data
        unzip(extractedSubmission, getAssignmentOf(project))
        unzip(extractedSubmission, getStudentSubmissionOf(project))

        // Delete archives
        Files.delete(downloadedSubmission)
        Files.delete(extractedSubmission)
    }

    /**
     * Updates all exercise files by syncing with the git server
     */
    fun updateExercise() = OrionGitAdapter.updateExercise(project)
}
