package de.tum.www1.orion.vcs

import com.intellij.dvcs.DvcsUtil
import com.intellij.dvcs.push.PushSpec
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.CheckoutProvider
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.VcsKey
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import de.tum.www1.orion.util.ArtemisSettingsProvider
import git4idea.GitVcs
import git4idea.checkin.GitCheckinEnvironment
import git4idea.checkout.GitCheckoutProvider
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitImpl
import git4idea.commands.GitLineHandler
import git4idea.config.GitVersionSpecialty
import git4idea.push.GitPushSource
import git4idea.push.GitPushSupport
import git4idea.push.GitPushTarget
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryManager
import java.io.File
import java.util.concurrent.locks.ReentrantLock

class ArtemisGitUtil {
    companion object {
        fun clone(project: Project, repository: String, courseId: Int, exerciseId: Int, exerciseName: String) {
            object : Task.Modal(project, "Importing from ArTEMiS...", true) {
                override fun run(indicator: ProgressIndicator) {
                    indicator.isIndeterminate = true
                    val settings = ServiceManager.getService(ArtemisSettingsProvider::class.java)
                    val artemisBaseDir = settings.getSetting(ArtemisSettingsProvider.KEYS.PROJECT_BASE_DIR)
                    val path = setupExerciseDirPath(artemisBaseDir, courseId, exerciseId, exerciseName)
                    val lfs = LocalFileSystem.getInstance()
                    val parent = lfs.findFileByIoFile(File(artemisBaseDir))
                    if (parent == null) {
                        lfs.refreshAndFindFileByIoFile(File(artemisBaseDir))
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
                            ProjectUtil.openOrImport(path, project, false)
                        }
                    }

                    GitCheckoutProvider.clone(project, Git.getInstance(), listenerProxy, parent, repository, path, artemisBaseDir)
                    lock.lock()
                    cond.await()
                    lock.unlock()
                }
            }.queue()
        }

        fun submit(project: Project) {
            ProgressManager.getInstance().run(object : Task.Modal(project, "Submitting your changes...", false) {
                override fun run(indicator: ProgressIndicator) {
                    val untracked = getAllUntracked(project)
                    val changes = ChangeListManager.getInstance(project).allChanges
                    if (!untracked.isEmpty() || !changes.isEmpty()) {
                        addAll(project, untracked)
                        commitAll(project, changes)
                    }
                    push(project)
                }
            })
        }

        private fun commitAll(project: Project, changes: Collection<Change>) {
            ServiceManager.getService(project, GitCheckinEnvironment::class.java)
                    .commit(changes.toList(), "Automated commit by OrION")
        }

        private fun addAll(project: Project, files: Collection<VirtualFile>) {
            ServiceManager.getService(project, GitCheckinEnvironment::class.java)
                    .scheduleUnversionedFilesForAddition(files.toList())
        }

        private fun getAllUntracked(project: Project): Collection<VirtualFile> {
            val gitRepositoryManager = ServiceManager.getService(project, GitRepositoryManager::class.java)
            return gitRepositoryManager.repositories[0].untrackedFilesHolder.retrieveUntrackedFiles()
        }

        private fun push(project: Project) {
            val gitRepositoryManager = ServiceManager.getService(project, GitRepositoryManager::class.java)
            val repository = gitRepositoryManager.repositories[0]
            val pushSupport = DvcsUtil.getPushSupport(GitVcs.getInstance(project))!! as GitPushSupport
            val source = pushSupport.getSource(repository)
            val branch = masterOf(repository)
            val target = GitPushTarget(branch, false)
            val pushSpecs = mapOf<GitRepository, PushSpec<GitPushSource, GitPushTarget>>(Pair(repository, PushSpec(source, target)))
            pushSupport.pusher.push(pushSpecs, null, false)
        }

        fun pull(project: Project) {
            ProgressManager.getInstance().run(object : Task.Modal(project, "Updating your exercise files...", false) {
                override fun run(indicator: ProgressIndicator) {
                    indicator.isIndeterminate = true
                    val repo = getDefaultRootRepository(project)!!
                    val remote = repo.remotes.first()
                    val handler = GitLineHandler(project, getRoot(project)!!, GitCommand.PULL)
                    handler.urls = remote.urls
                    handler.addParameters("--no-stat")
                    handler.addParameters("-v")
                    if (GitVersionSpecialty.ABLE_TO_USE_PROGRESS_IN_REMOTE_COMMANDS.existsIn(project)) {
                        handler.addParameters("--progress")
                    }
                    handler.addParameters(remote.name)
                    handler.addParameters("master")

                    GitImpl().runCommand(handler)
                    ApplicationManager.getApplication().invokeLater {
                        VfsUtil.markDirtyAndRefresh(false, true, false, getRoot(project))
                    }
                }
            })
        }

        private fun masterOf(repository: GitRepository) = repository.branches.remoteBranches.first { it.name == "origin/master" }

        private fun getDefaultRootRepository(project: Project): GitRepository? {
            val gitRepositoryManager = ServiceManager.getService(project, GitRepositoryManager::class.java)
            val rootDir = getRoot(project)

            return gitRepositoryManager.getRepositoryForRoot(rootDir)
        }

        private fun getRoot(project: Project): VirtualFile? {
            if (project.basePath != null) {
                val lfs = LocalFileSystem.getInstance()
                return lfs.findFileByPath(project.basePath!!)
            }

            return null
        }

        private fun setupExerciseDirPath(baseDir: String, courseId: Int, exerciseId: Int, exerciseName: String): String {
            val pathToExercise = File("$baseDir/$courseId-$exerciseId-${exerciseName.replace(' ', '_')}")
            if (!pathToExercise.exists()) {
                pathToExercise.mkdirs()
            }
            return pathToExercise.absolutePath
        }
    }
}