package de.tum.www1.orion.build.instructor

import com.intellij.execution.ExecutionListener
import com.intellij.execution.ExecutionManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ExecutionUtil
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import de.tum.www1.orion.build.OrionLocalRunConfigurationSettingsFactory
import de.tum.www1.orion.dto.RepositoryType
import de.tum.www1.orion.enumeration.ProgrammingLanguage
import de.tum.www1.orion.exercise.registry.OrionStudentExerciseRegistry
import de.tum.www1.orion.messaging.OrionIntellijStateNotifier
import de.tum.www1.orion.ui.util.notify
import de.tum.www1.orion.util.appService
import de.tum.www1.orion.util.selectedProgrammingLanguage
import de.tum.www1.orion.util.translate
import java.io.File

interface CustomizableCheckoutPath {
    fun forProgrammingLanguage(language: ProgrammingLanguage): String
}

/**
 * Path a repository should get checked out in a build plan. E.g. the assignment repository should get checked out
 * to a subdirectory called "assignment" for the Python programming language.
 */
enum class RepositoryCheckoutPath : CustomizableCheckoutPath {
    ASSIGNMENT {
        override fun forProgrammingLanguage(language: ProgrammingLanguage): String {
            return "assignment"
        }
    },
    TEST {
        override fun forProgrammingLanguage(language: ProgrammingLanguage): String {
            return when (language) {
                ProgrammingLanguage.JAVA, ProgrammingLanguage.PYTHON, ProgrammingLanguage.KOTLIN -> ""
                ProgrammingLanguage.C -> "tests"
                // runTestsLocally should prevent any other language from reaching this line
                else -> throw UnsupportedOperationException(
                    "Attempted to query test directory for language %s but it is not supported".format(
                        language
                    )
                )
            }
        }
    }
}

class OrionInstructorBuildUtil(val project: Project) {
    /**
     * A function to run tests locally
     */
    fun runTestsLocally() {
        val language = project.selectedProgrammingLanguage() ?: return Unit.also {
            when (val exerciseLanguage = project.service<OrionStudentExerciseRegistry>().exerciseInfo?.language) {
                ProgrammingLanguage.JAVA, ProgrammingLanguage.PYTHON -> project.notify(translate("orion.error.language.buildLocally.noSDK"))
                else -> project.notify(
                    translate("orion.error.language.buildLocally.notSupported").format(
                        exerciseLanguage
                    )
                )
            }
            project.messageBus.syncPublisher(OrionIntellijStateNotifier.INTELLIJ_STATE_TOPIC).isBuilding(false)
        }
        val repositoryDirectory = File(project.basePath!! + File.separatorChar + RepositoryType.SOLUTION.directoryName)
        val testsDirectory = File(project.basePath!! + File.separatorChar + RepositoryType.TEST.directoryName)
        val virtualRepoDir = LocalFileSystem.getInstance().findFileByIoFile(repositoryDirectory)
        val virtualTestsDir = LocalFileSystem.getInstance().findFileByIoFile(testsDirectory)

        val testBaseDirectory = File(project.basePath!! + File.separatorChar + LOCAL_TEST_DIRECTORY)
        FileUtil.delete(testBaseDirectory)
        FileUtil.ensureExists(testBaseDirectory)
        val virtualTestBase = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(testBaseDirectory)!!

        WriteAction.runAndWait<Throwable> {
            appService(FileDocumentManager::class.java).saveAllDocuments()
            VfsUtil.markDirtyAndRefresh(false, true, true, virtualTestBase)
            virtualRepoDir?.let {
                copyRepoToTestDir(
                    virtualTestBase,
                    it,
                    RepositoryCheckoutPath.ASSIGNMENT.forProgrammingLanguage(language)
                )
            }
            virtualTestsDir?.let {
                copyRepoToTestDir(
                    virtualTestBase,
                    it,
                    RepositoryCheckoutPath.TEST.forProgrammingLanguage(language)
                )
            }
        }
        // creates a run configuration
        val runConfigurationSettings = OrionLocalRunConfigurationSettingsFactory.runConfigurationForInstructor(project)
        if (runConfigurationSettings != null) {
            ExecutionUtil.runConfiguration(runConfigurationSettings, DefaultRunExecutor.getRunExecutorInstance())
        }
        project.messageBus.connect().subscribe(ExecutionManager.EXECUTION_TOPIC, object : ExecutionListener {
            override fun processTerminated(
                executorId: String,
                env: ExecutionEnvironment,
                handler: ProcessHandler,
                exitCode: Int
            ) {
                project.messageBus.syncPublisher(OrionIntellijStateNotifier.INTELLIJ_STATE_TOPIC).isBuilding(false)
            }
        })
    }

    private fun copyRepoToTestDir(virtualTestBase: VirtualFile, repository: VirtualFile, path: String) {
        val repoBase: VirtualFile = if (path == "") {
            virtualTestBase
        } else {
            VfsUtil.createDirectoryIfMissing(virtualTestBase, path)
        }

        repository.children
            .filter { !it.name.matches(Regex("\\.git.*")) }
            .forEach { it.copy(this, repoBase, it.name) }
    }

    companion object {
        const val LOCAL_TEST_DIRECTORY = "merged_tests"

        @JvmStatic
        fun getInstance(project: Project): OrionInstructorBuildUtil {
            return project.service()
        }
    }
}
