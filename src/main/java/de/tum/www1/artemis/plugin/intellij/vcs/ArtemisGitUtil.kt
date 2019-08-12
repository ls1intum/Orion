package de.tum.www1.artemis.plugin.intellij.vcs

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.CheckoutProvider
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.VcsKey
import com.intellij.openapi.vfs.LocalFileSystem
import git4idea.checkout.GitCheckoutProvider
import git4idea.commands.Git
import java.io.File
import java.util.concurrent.locks.ReentrantLock

class ArtemisGitUtil {
    companion object {
        private val userHome: String = System.getProperty("user.home")
        private val artemisParentDirectory = "$userHome/ArtemisProjects"

        fun clone(project: Project, repository: String, exerciseName: String) {
            object : Task.Modal(project, "Importing from ArTEMiS...", true) {
                override fun run(indicator: ProgressIndicator) {
                    indicator.isIndeterminate = true
                    setupExerciseDirPath(exerciseName)
                    val lfs = LocalFileSystem.getInstance()
                    val parent = lfs.findFileByIoFile(File(artemisParentDirectory))
                    val listener = ProjectLevelVcsManager.getInstance(project).compositeCheckoutListener
                    val lock = ReentrantLock()
                    val cond = lock.newCondition()
                    val listenerProxy = object : CheckoutProvider.Listener {
                        override fun directoryCheckedOut(directory: File?, vcs: VcsKey?) {
                            listener.directoryCheckedOut(directory, vcs)
                        }

                        override fun checkoutCompleted() {
                            lock.lock()
                            cond.signalAll()
                            lock.unlock()
                            listener.checkoutCompleted()
                        }
                    }

                    GitCheckoutProvider.clone(project, Git.getInstance(), listenerProxy, parent, repository, exerciseName, artemisParentDirectory)
                    lock.lock()
                    cond.await()
                    lock.unlock()
                }
            }.queue();
        }

        private fun setupExerciseDirPath(exerciseName: String) {
            val pathToExercise = File("$artemisParentDirectory/$exerciseName")
            if (!pathToExercise.exists()) {
                pathToExercise.mkdirs()
            }
        }
    }
}