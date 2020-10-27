package de.tum.www1.orion.exercise.registry

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
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
import de.tum.www1.orion.ui.util.notify
import de.tum.www1.orion.util.OrionFileUtils.getRoot
import java.io.IOException

/**
 * Store the information imported from the .artemisExercise.json received from the server. The file is deleted after.
 * Storage location is .idea/workspace.iml
 * For some reason this class needs to be implemented in Kotlin, otherwise the IDE crushes when openned in instructor
 * mode and run "build and test locally".
 */
@State(name = "orionRegistry", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class OrionProjectRegistryStateService(private val myProject: Project) :
    PersistentStateComponent<OrionProjectRegistryStateService.State?> {
    private var myState: State? = null

    /**
     * Any rename of the field name in state would be a breaking change and require the users to re-clone the repo.
     */
    data class State(
        var courseId: Long = 0,
        var courseTitle: String? = null,
        var exerciseId: Long = 0,
        var exerciseTitle: String? = null,
        var selectedRepository: RepositoryType = RepositoryType.ASSIGNMENT,
        var currentView: ExerciseView = ExerciseView.STUDENT,
        var language: ProgrammingLanguage = ProgrammingLanguage.JAVA,
        var templateParticipationId: Long? = null,
        var solutionParticipationId: Long? = null,
    )

    override fun getState(): State? {
        return myState
    }

    override fun loadState(state: State) {
        myState = state
    }

    fun importIfPending() {
        val pendingImportFile = VfsUtil.findRelativeFile(getRoot(myProject), ".artemisExercise.json")
        if (pendingImportFile != null) {
            try {
                // Buggy right now. Uncomment, if the @JvmOverloads annotations works again on the KotlinModule constructor
                // final var imported = JsonUtils.INSTANCE.mapper().readValue(pendingImportFile.getInputStream(), ImportedExercise.class);
                val (courseId, exerciseId, courseTitle, exerciseTitle, view, language, templateParticipationId, solutionParticipationId) = ObjectMapper().registerModule(
                    KotlinModule()
                ).readValue(pendingImportFile.inputStream, ImportedExercise::class.java)
                val myState = State()
                myState.courseId = courseId
                myState.exerciseId = exerciseId
                myState.courseTitle = courseTitle
                myState.exerciseTitle = exerciseTitle
                myState.language = language
                myState.currentView = view
                myState.templateParticipationId = templateParticipationId
                myState.solutionParticipationId = solutionParticipationId
                if (myState.currentView === ExerciseView.INSTRUCTOR) {
                    myState.guessProjectSdk()
                    myState.selectedRepository = RepositoryType.TEST // init
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

    private fun State.guessProjectSdk() {
        val availableSdks: List<Sdk> = when (this.language) {
            ProgrammingLanguage.JAVA -> listOf(*ProjectJdkTable.getInstance().allJdks)
            ProgrammingLanguage.PYTHON -> PythonSdkUtil.getAllSdks()
            else -> return Unit.also { myProject.notify("Programming language " + this.language + " is not supported yet!") }
        }
        if (availableSdks.isEmpty()) {
            myProject.notify(
                "Can not find a project SDK for the current project. Please set it in File->Project Structure before" +
                        "running tests or submit."
            )
            return
        }
        availableSdks.maxWith(compareBy { sdk -> sdk.versionString })?.let {
            ApplicationManager.getApplication().invokeLater {
                WriteAction.run<RuntimeException> {
                    ProjectRootManager.getInstance(myProject).projectSdk = it
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