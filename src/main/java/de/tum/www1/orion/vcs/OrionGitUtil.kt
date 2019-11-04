package de.tum.www1.orion.vcs

import com.intellij.dvcs.DvcsUtil
import com.intellij.dvcs.push.PushSpec
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import de.tum.www1.orion.bridge.ArtemisBridge
import de.tum.www1.orion.enumeration.ExerciseView
import de.tum.www1.orion.util.OrionFileUtils
import de.tum.www1.orion.util.OrionSettingsProvider
import de.tum.www1.orion.util.invokeOnEDTAndWait
import de.tum.www1.orion.util.service
import git4idea.GitVcs
import git4idea.checkin.GitCheckinEnvironment
import git4idea.checkout.GitCheckoutProvider
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitImpl
import git4idea.commands.GitLineHandler
import git4idea.config.GitVersionSpecialty
import git4idea.push.GitPushSupport
import git4idea.push.GitPushTarget
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryManager
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

private fun Module.repository(): GitRepository {
    val gitRepositoryManager = this.project.service(GitRepositoryManager::class.java)
    return gitRepositoryManager.repositories.first { it.root.name == this.name }
}

object OrionGitUtil {
    fun cloneAndOpenExercise(project: Project, repository: String, courseId: Long, exerciseId: Long, exerciseName: String) {
        val path = OrionFileUtils.setupExerciseDirPath(courseId, exerciseId, exerciseName, ExerciseView.STUDENT)
        val settings = ServiceManager.getService(OrionSettingsProvider::class.java)
        val artemisBaseDir = settings.getSetting(OrionSettingsProvider.KEYS.PROJECT_BASE_DIR)

        clone(project, repository, artemisBaseDir, path) {
            ProjectUtil.openOrImport(path, project, false)
        }
    }

    fun <T> clone(currentProject: Project, repository: String, baseDir: String, clonePath: String, andThen: (() -> T)?) {
        object : Task.Backgroundable(currentProject, "Importing from ArTEMiS...", true) {
            private val cloneResult = AtomicBoolean()
            private val listener = ProjectLevelVcsManager.getInstance(currentProject).compositeCheckoutListener

            private var parent: VirtualFile? = null

            override fun run(indicator: ProgressIndicator) {
                ServiceManager.getService(project, ArtemisBridge::class.java).isCloning(true)
                indicator.isIndeterminate = true
                val lfs = LocalFileSystem.getInstance()
                parent = lfs.findFileByIoFile(File(baseDir))
                if (parent == null) {
                    lfs.refreshAndFindFileByIoFile(File(baseDir))
                }

                cloneResult.set(GitCheckoutProvider.doClone(currentProject, Git.getInstance(), clonePath, baseDir, repository))
            }

            override fun onSuccess() {
                if (!cloneResult.get()) return;
                DvcsUtil.addMappingIfSubRoot(currentProject, FileUtil.join(baseDir, clonePath), GitVcs.NAME)
                parent?.refresh(true, true) {
                    if (currentProject.isOpen && !currentProject.isDisposed && !currentProject.isDefault) {
                        val mgr = VcsDirtyScopeManager.getInstance(currentProject)
                        mgr.fileDirty(parent!!)
                    }
                }
                listener.apply {
                    directoryCheckedOut(File(baseDir, clonePath), GitVcs.getKey())
                    checkoutCompleted()
                }
                ServiceManager.getService(project, ArtemisBridge::class.java).isCloning(false)
                andThen?.invoke()
            }

            override fun onError(error: Exception) {
                super.onError(error)
                ServiceManager.getService(project, ArtemisBridge::class.java).isCloning(false)
            }
        }.queue()
    }

    fun submit(project: Project) {
        ProgressManager.getInstance().run(object : Task.Modal(project, "Submitting your changes...", false) {
            override fun run(indicator: ProgressIndicator) {
                invokeOnEDTAndWait { FileDocumentManager.getInstance().saveAllDocuments() }
                getAllUntracked(project)
                        .takeIf { it.isNotEmpty() }
                        ?.let { addAll(project, it) }
                ChangeListManager.getInstance(project).allChanges
                        .takeIf { it.isNotEmpty() }
                        ?.let { commitAll(project, it) }
                push(project)
            }
        })
    }

    fun submit(module: Module) {
        getAllUntracked(module)
                .takeIf { it.isNotEmpty() }
                ?.let { addAll(module.project, it) }
        val moduleBaseDir = module.moduleFile!!.parent
        ChangeListManager.getInstance(module.project).getChangesIn(moduleBaseDir)
                .takeIf { it.isNotEmpty() }
                ?.let { commitAll(module.project, it) }
        push(module)
    }

    private fun commitAll(project: Project, changes: Collection<Change>) {
        ServiceManager.getService(project, GitCheckinEnvironment::class.java)
                .commit(changes.toList(), "Automated commit by Orion")
    }

    private fun addAll(project: Project, files: Collection<VirtualFile>) {
        ServiceManager.getService(project, GitCheckinEnvironment::class.java)
                .scheduleUnversionedFilesForAddition(files.toList())
    }

    private fun getAllUntracked(project: Project): Collection<VirtualFile> {
        val gitRepositoryManager = ServiceManager.getService(project, GitRepositoryManager::class.java)
        return gitRepositoryManager.repositories[0].untrackedFilesHolder.retrieveUntrackedFiles()
    }

    private fun getAllUntracked(module: Module): Collection<VirtualFile> {
        return module.repository().untrackedFilesHolder.retrieveUntrackedFiles()
    }

    private fun push(project: Project) {
        val gitRepositoryManager = ServiceManager.getService(project, GitRepositoryManager::class.java)
        val repository = gitRepositoryManager.repositories[0]
        pushToMaster(project, repository)
    }

    private fun pushToMaster(project: Project, repository: GitRepository) {
        val pushSupport = DvcsUtil.getPushSupport(GitVcs.getInstance(project))!! as GitPushSupport
        val source = pushSupport.getSource(repository)
        val branch = masterOf(repository)
        val target = GitPushTarget(branch, false)
        val pushSpecs = mapOf(Pair(repository, PushSpec(source, target)))
        pushSupport.pusher.push(pushSpecs, null, false)
    }

    private fun push(module: Module) {
        pushToMaster(module.project, module.repository())
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
                    VfsUtil.markDirtyAndRefresh(false, true, true, getRoot(project))
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
}