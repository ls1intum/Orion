package de.tum.www1.artemis.plugin.intellij.vcs

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vfs.LocalFileSystem
import git4idea.checkout.GitCheckoutProvider
import git4idea.commands.Git
import java.io.File

class ArtemisGitUtil {
    companion object {
        private val userHome: String = System.getProperty("user.home")
        private val artemisParentDirectory = "$userHome/ArtemisProjects"

        fun clone(project: Project, repository: String, exerciseName: String) {
            setupExerciseDirPath(exerciseName)
            val lfs = LocalFileSystem.getInstance()
            val parent = lfs.findFileByIoFile(File(artemisParentDirectory))
            val listener = ProjectLevelVcsManager.getInstance(project).compositeCheckoutListener

            GitCheckoutProvider.clone(project, Git.getInstance(), listener, parent, repository, exerciseName, artemisParentDirectory)
        }

        private fun setupExerciseDirPath(exerciseName: String) {
            val pathToExercise = File("$artemisParentDirectory/$exerciseName")
            if (!pathToExercise.exists()) {
                pathToExercise.mkdirs()
            }
        }
    }
}