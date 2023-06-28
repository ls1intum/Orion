package de.tum.www1.orion.exercise.registry

import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtil
import com.jetbrains.python.sdk.PythonSdkUtil
import de.tum.www1.orion.dto.RepositoryType
import de.tum.www1.orion.enumeration.ExerciseView
import de.tum.www1.orion.enumeration.ProgrammingLanguage
import de.tum.www1.orion.exercise.OrionJavaTutorProjectCreator
import de.tum.www1.orion.ui.util.notify
import de.tum.www1.orion.util.JsonUtils
import de.tum.www1.orion.util.OrionFileUtils.getRoot
import de.tum.www1.orion.util.translate
import java.io.IOException

/**
 * Interface to persist data in IntelliJ's workspace.xml file using built-in features.
 * Stores all artemis specific data like exercise id and course id.
 *
 * For some reason this class needs to be implemented in Kotlin, otherwise the IDE crushes when
 * opened in instructor mode when running "build and test locally".
 */
@State(name = "orionRegistry", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class OrionProjectRegistryStateService(private val myProject: Project) :
    PersistentStateComponent<OrionProjectRegistryStateService.State?> {
    private var myState: State? = null

    /**
     * Renaming any field in state would be a breaking change and require the users to re-clone the repo.
     */
    data class State(
        var courseId: Long = 0,
        var courseTitle: String? = null,
        var exerciseId: Long = 0,
        var exerciseTitle: String? = null,
        var selectedRepository: RepositoryType = RepositoryType.ASSIGNMENT,
        var currentView: ExerciseView = ExerciseView.STUDENT,
        var language: ProgrammingLanguage = ProgrammingLanguage.JAVA,
        // For exercises opened as instructor
        var templateParticipationId: Long? = null,
        var solutionParticipationId: Long? = null,
        // For exercises opened as tutor
        var submissionId: Long? = null,
        var correctionRound: Long? = null,
        // Currently unused, Orion does not support test runs
        // The flag is transferred and stored nonetheless to allow quick extension
        var testRun: Boolean? = null,
        // For exam exercises
        var exerciseGroupId: Long? = null,
        var examId: Long? = null,
    )

    override fun getState(): State? {
        return myState
    }

    override fun loadState(state: State) {
        myState = state
    }

    /**
     * Store the information imported from the .artemisExercise.json received from the server.
     * The file is deleted afterwards.
     */
    fun importIfPending() {
        val pendingImportFile = VfsUtil.findRelativeFile(getRoot(myProject), ".artemisExercise.json")
        if (pendingImportFile != null) {
            try {
                val imported = JsonUtils.mapper().readValue(pendingImportFile.inputStream, ImportedExercise::class.java)
                val myState = State()
                imported.apply {
                    myState.courseId = courseId
                    myState.exerciseId = exerciseId
                    myState.courseTitle = courseTitle
                    myState.exerciseTitle = exerciseTitle
                    myState.language = language
                    myState.currentView = view
                    myState.templateParticipationId = templateParticipationId
                    myState.solutionParticipationId = solutionParticipationId
                    myState.exerciseGroupId = exerciseGroupId
                    myState.examId = examId
                }
                // operations specific to a view
                when (myState.currentView) {
                    ExerciseView.INSTRUCTOR -> {
                        myState.guessProjectSdk()
                        // init
                        myState.selectedRepository = RepositoryType.TEST
                    }
                    ExerciseView.TUTOR -> OrionJavaTutorProjectCreator.prepareProjectAfterImport(myProject)
                    else -> Unit
                }
                // warn if language not supported
                when (myState.language) {
                    ProgrammingLanguage.JAVA -> Unit
                    else -> myProject.notify(translate("orion.error.language.import.notSupported").format(myState.language.name))
                }
                loadState(myState)
                ApplicationManager.getApplication().invokeLater {
                    try {
                        WriteAction.run<IOException> { pendingImportFile.delete(this) }
                    } catch (e: IOException) {
                        log.error(e.message, e)
                    }
                }
            } catch (e: IOException) {
                log.error(e.message, e)
            }
        }
    }

    /**
     * Suggests SDK to use for the freshly opened project.
     */
    private fun State.guessProjectSdk() {
        val availableSdks: List<Sdk> = when (this.language) {
            ProgrammingLanguage.JAVA -> ProjectJdkTable.getInstance().allJdks.toList()
            ProgrammingLanguage.KOTLIN -> ProjectJdkTable.getInstance().allJdks.toList()
            ProgrammingLanguage.PYTHON -> PythonSdkUtil.getAllSdks()
            else -> return
        }
        if (availableSdks.isEmpty()) {
            myProject.notify(translate("orion.error.sdk.notAvailable"))
            return
        }
        availableSdks.maxWithOrNull(compareBy { sdk -> sdk.versionString })?.let {
            ApplicationManager.getApplication().invokeLater {
                try {
                    WriteAction.run<RuntimeException> {
                        ProjectRootManager.getInstance(myProject).projectSdk = it
                    }
                } catch (e: Throwable) {
                    myProject.notify(
                        translate("orion.error.exercise.sdkFailed").format(e.message),
                        NotificationType.WARNING
                    )
                }
            }
        }

    }

    val isArtemisExercise: Boolean
        get() = myState != null

    companion object {
        private val log = Logger.getInstance(
            OrionProjectRegistryStateService::class.java
        )
    }
}
