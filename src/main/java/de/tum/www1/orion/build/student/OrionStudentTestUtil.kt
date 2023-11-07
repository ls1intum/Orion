package de.tum.www1.orion.build.student

import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import de.tum.www1.orion.ui.util.notify
import de.tum.www1.orion.util.OrionProjectUtil
import de.tum.www1.orion.vcs.OrionGitAdapter
import java.io.File


@Service(Service.Level.PROJECT)
class OrionStudentTestUtilService(val project: Project) {

    /**
     * Initializes the test repository for a programming exercise
     */
    fun initializeTestRepo(testRepositoryUrl: String) {
        // clone repo
        val currentDirectory = project.basePath
        if (currentDirectory == null) {
            project.notify("Error initializing the project!!! [REPLACE THIS]")
            return
        }
        // Clone Module this can be done for any project
        OrionGitAdapter.clone(
            project, testRepositoryUrl, currentDirectory, "./artemis-tests/", false
        ) {
            // followup setups for different programming languages/types of projects
            copyAssignmentFolder(project)
            initializeModule(project)
            project.notify("Successfully initialized test setup!", NotificationType.IDE_UPDATE)
        }

    }

    /**
     * Copies the assignment folder (src)
     * @param project the currently opened project
     */
    private fun copyAssignmentFolder(project: Project) {
        runInEdt {
            runWriteAction {
                val srcPath = File("${project.basePath}${File.separatorChar}src")
                val assignemntSrcPath =
                    File("${project.basePath}${File.separatorChar}artemis-tests${File.separatorChar}assignment${File.separatorChar}src")
                FileUtil.createDirectory(assignemntSrcPath)
                if (srcPath.isDirectory) {
                    FileUtil.copyFileOrDir(srcPath, assignemntSrcPath)
                }
            }
        }
    }


    /**
     * Initializes a new module with tests for the project
     * @param project the currently opened project
     *
     */
    private fun initializeModule(project: Project) {
        // check if its IntelliJ else stop the configuration
        val applicationInfo = ApplicationInfo.getInstance().fullApplicationName
        if (!applicationInfo.contains("IntelliJ")) {
            return
        }
        // is gradle project
        if (File("${project.basePath}${File.separatorChar}artemis-tests${File.separatorChar}build.gradle").isFile) {
            OrionProjectUtil.newModule(project, "artemis-tests", OrionProjectUtil.ModuleType.GRADLE_MODULE)
        }
        // is maven project
        if (File("${project.basePath}${File.separatorChar}artemis-tests${File.separatorChar}pom.xml").isFile) {
            OrionProjectUtil.newModule(project, "artemis-tests", OrionProjectUtil.ModuleType.MAVEN_MODULE)
        }
    }
}
