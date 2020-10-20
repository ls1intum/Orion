package de.tum.www1.orion.build.instructor

import com.intellij.execution.ExecutionListener
import com.intellij.execution.ExecutionManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ExecutionUtil
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.runInEdtAndWait
import de.tum.www1.orion.dto.RepositoryType
import de.tum.www1.orion.enumeration.ProgrammingLanguage
import de.tum.www1.orion.exercise.registry.OrionInstructorExerciseRegistry
import de.tum.www1.orion.messaging.OrionIntellijStateNotifier
import de.tum.www1.orion.util.appService
import de.tum.www1.orion.util.selectedProgrammingLangauge
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
                ProgrammingLanguage.JAVA, ProgrammingLanguage.PYTHON -> ""
                ProgrammingLanguage.C -> return "tests"
            }
        }
    }
}

class OrionInstructorBuildUtil(val project: Project) {
    fun runTestsLocally() {
        val repository = project.service<OrionInstructorExerciseRegistry>().selectedRepository
        val repositoryDirectory = File(project.basePath!! + File.separatorChar + repository.directoryName)
        val testsDirectory = File(project.basePath!! + File.separatorChar + RepositoryType.TEST.directoryName)
        val virtualRepoDir = LocalFileSystem.getInstance().findFileByIoFile(repositoryDirectory)
        val virtualTestsDir = LocalFileSystem.getInstance().findFileByIoFile(testsDirectory)

        val testBaseDirectory = File(project.basePath!! + File.separatorChar + LOCAL_TEST_DIRECTORY)
        FileUtil.delete(testBaseDirectory)
        FileUtil.ensureExists(testBaseDirectory)
        val virtualTestBase = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(testBaseDirectory)!!

        runInEdtAndWait {
            runWriteAction {
                appService(FileDocumentManager::class.java).saveAllDocuments()
                VfsUtil.markDirtyAndRefresh(false, true, true, virtualTestBase)
                val language = project.selectedProgrammingLangauge()
                virtualRepoDir?.let { copyRepoToTestDir(virtualTestBase, it, RepositoryCheckoutPath.ASSIGNMENT.forProgrammingLanguage(language)) }
                virtualTestsDir?.let { copyRepoToTestDir(virtualTestBase, it, RepositoryCheckoutPath.TEST.forProgrammingLanguage(language)) }
            }
        }

        val runConfigurationSettings = OrionLocalRunConfigurationSettingsFactory.runConfigurationForInstructor(project)
        ExecutionUtil.runConfiguration(runConfigurationSettings, DefaultRunExecutor.getRunExecutorInstance())
        project.messageBus.connect().subscribe(ExecutionManager.EXECUTION_TOPIC, object : ExecutionListener {
            override fun processTerminated(executorId: String, env: ExecutionEnvironment, handler: ProcessHandler, exitCode: Int) {
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
            return ServiceManager.getService(project, OrionInstructorBuildUtil::class.java)
        }
    }
}
