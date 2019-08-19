package de.tum.www1.artemis.plugin.intellij.vcs

import com.intellij.dvcs.DvcsUtil
import com.intellij.dvcs.push.PushSpec
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.CheckoutProvider
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.VcsKey
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import git4idea.GitVcs
import git4idea.checkin.GitCheckinEnvironment
import git4idea.checkout.GitCheckoutProvider
import git4idea.commands.Git
import git4idea.push.GitPushSource
import git4idea.push.GitPushSupport
import git4idea.push.GitPushTarget
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryManager
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
                    if (parent == null) {
                        lfs.refreshAndFindFileByIoFile(File(artemisParentDirectory))
                    }
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
            val gitRepositoryManager = ServiceManager.getService(project, GitRepositoryManager::class.java)
            return gitRepositoryManager.repositories[0].untrackedFilesHolder.retrieveUntrackedFiles()
        }

        fun push(project: Project) {
            val gitRepositoryManager = ServiceManager.getService(project, GitRepositoryManager::class.java)
            val repository = gitRepositoryManager.repositories[0]
            val remote = repository.remotes.first()
            val pushSupport = DvcsUtil.getPushSupport(GitVcs.getInstance(project))!! as GitPushSupport
            val source = pushSupport.getSource(repository)
            val branch = repository.branches.remoteBranches.first { it.remote == remote && it.name == "origin/master" }
            val target = GitPushTarget(branch, false)
            val pushSpecs = mapOf<GitRepository, PushSpec<GitPushSource, GitPushTarget>>(Pair(repository, PushSpec(source, target)))
            pushSupport.pusher.push(pushSpecs, null, false)
        }

        private fun setupExerciseDirPath(exerciseName: String) {
            val pathToExercise = File("$artemisParentDirectory/$exerciseName")
            if (!pathToExercise.exists()) {
                pathToExercise.mkdirs()
            }
        }
    }
}