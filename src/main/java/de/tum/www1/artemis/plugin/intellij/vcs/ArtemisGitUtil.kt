package de.tum.www1.artemis.plugin.intellij.vcs

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapperPeerFactory
import com.intellij.openapi.vcs.*
import com.intellij.openapi.vcs.actions.VcsContextFactory
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vcs.changes.CurrentContentRevision
import com.intellij.openapi.vcs.changes.ui.ChangesListView
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.peer.impl.VcsContextFactoryImpl
import com.intellij.util.ArrayUtil
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.containers.isEmpty
import git4idea.GitCommit
import git4idea.GitUtil
import git4idea.GitVcs
import git4idea.actions.GitAdd
import git4idea.checkin.GitCheckinEnvironment
import git4idea.checkout.GitCheckoutProvider
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.repo.GitRepositoryManager
import git4idea.util.GitUntrackedFilesHelper
import git4idea.vfs.GitVFSListener
import java.io.File
import java.util.concurrent.locks.ReentrantLock
import java.util.stream.Stream
import com.intellij.util.containers.notNullize
import git4idea.repo.GitUntrackedFilesHolder
import kotlin.streams.toList

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
            }.queue()
        }

        fun commitAll(project: Project) {
            val changes = ChangeListManager.getInstance(project).allChanges
            ServiceManager.getService(project, GitCheckinEnvironment::class.java)
                    .commit(changes.toList(), "Automated commit by OrION")
        }

        fun addAll(project: Project, files: Collection<VirtualFile>) {
            ServiceManager.getService(project, GitCheckinEnvironment::class.java)
                    .scheduleUnversionedFilesForAddition(files.toList())
        }

        fun getAllUntracked(project: Project): Collection<VirtualFile> {
            return GitRepositoryManager.getInstance(project).repositories[0].untrackedFilesHolder.retrieveUntrackedFiles()
        }

        private fun setupExerciseDirPath(exerciseName: String) {
            val pathToExercise = File("$artemisParentDirectory/$exerciseName")
            if (!pathToExercise.exists()) {
                pathToExercise.mkdirs()
            }
        }
    }
}