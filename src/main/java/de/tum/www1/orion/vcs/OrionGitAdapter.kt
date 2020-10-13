package de.tum.www1.orion.vcs

import com.intellij.dvcs.DvcsUtil
import com.intellij.dvcs.push.PushSpec
import com.intellij.dvcs.repo.VcsRepositoryManager
import com.intellij.dvcs.repo.VcsRepositoryMappingListener
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ThrowableComputable
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import de.tum.www1.orion.dto.RepositoryType
import de.tum.www1.orion.exercise.registry.OrionInstructorExerciseRegistry
import de.tum.www1.orion.messaging.OrionIntellijStateNotifier
import de.tum.www1.orion.util.OrionFileUtils
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
import org.jetbrains.annotations.SystemIndependent
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

private fun Module.repository(): GitRepository {
    val gitRepositoryManager = this.project.service(GitRepositoryManager::class.java)
    return gitRepositoryManager.repositories.first { it.root.name == this.name }
}

object OrionGitAdapter {
    fun cloneAndOpenExercise(project: Project, repository: String, path: @SystemIndependent String, andThen: (() -> Unit)?) {
        FileUtil.ensureExists(File(path))
        val parent = LocalFileSystem.getInstance().refreshAndFindFileByPath(path)!!.parent.path

        clone(project, repository, parent, path) {
            andThen?.invoke()
            ProjectUtil.openOrImport(path, project, false)
        }
    }

    fun clone(currentProject: Project, repository: String, baseDir: String, clonePath: String, andThen: (() -> Unit)?) {
        object : Task.Backgroundable(currentProject, "Importing from ArTEMiS...", true) {
            private val cloneResult = AtomicBoolean()
            private val listener = ProjectLevelVcsManager.getInstance(currentProject).compositeCheckoutListener

            private var parent: VirtualFile? = null

            override fun run(indicator: ProgressIndicator) {
                project.messageBus.syncPublisher(OrionIntellijStateNotifier.INTELLIJ_STATE_TOPIC).isCloning(true)
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
                try {
                    project.messageBus.syncPublisher(OrionIntellijStateNotifier.INTELLIJ_STATE_TOPIC).isCloning(false)
                    andThen?.invoke()
                } catch (e: AssertionError) {
                    if (e.message?.contains("Already disposed") != true) {
                        throw e
                    }
                }
            }

            override fun onError(error: Exception) {
                super.onError(error)
                project.messageBus.syncPublisher(OrionIntellijStateNotifier.INTELLIJ_STATE_TOPIC).isCloning(false)
            }
        }.queue()
    }

    fun submit(project: Project, withEmptyCommit: Boolean = true) {
        ProgressManager.getInstance().run(object : Task.Modal(project, "Submitting your changes...", false) {
            override fun run(indicator: ProgressIndicator) {
                invokeOnEDTAndWait { FileDocumentManager.getInstance().saveAllDocuments() }
                getAllUntracked(project)
                        .takeIf { it.isNotEmpty() }
                        ?.let { addAll(project, it) }
                val changes = ChangeListManager.getInstance(project).allChanges
                if (changes.isNotEmpty()) {
                    commitAll(project, changes)
                } else if (withEmptyCommit) {
                    emptyCommit(project)
                }
                push(project)
            }
        })
    }

    fun submit(module: Module, withEmptyCommit: Boolean = true) {
        ProgressManager.getInstance().run(object : Task.Modal(module.project, "Submitting your changes...", false) {
            override fun run(indicator: ProgressIndicator) {
                invokeOnEDTAndWait { FileDocumentManager.getInstance().saveAllDocuments() }
                getAllUntracked(module)
                        .takeIf { it.isNotEmpty() }
                        ?.let { addAll(module.project, it) }
                val moduleBaseDir = module.moduleFile!!.parent
                val changes = ChangeListManager.getInstance(module.project).getChangesIn(moduleBaseDir)
                if (changes.isNotEmpty()) {
                    commitAll(module.project, changes)
                } else if (withEmptyCommit) {
                    emptyCommit(module)
                }
                push(module)
            }
        })
    }

    private fun commitAll(project: Project, changes: Collection<Change>) {
        ServiceManager.getService(project, GitCheckinEnvironment::class.java)
                .commit(changes.toList(), "Automated commit by Orion")
    }

    private fun emptyCommit(module: Module) {
        val repo = module.repository()
        emptyCommit(repo, module.project)
    }

    private fun emptyCommit(project: Project) {
        val repo = getDefaultRootRepository(project)!!
        emptyCommit(repo, project)
    }

    private fun emptyCommit(repository: GitRepository, project: Project) {
        val remote = repository.remotes.first()
        val handler = GitLineHandler(project, repository.root, GitCommand.COMMIT)
        handler.urls = remote.urls
        handler.addParameters("-m Empty commit by Orion", "--allow-empty")

        GitImpl().runCommand(handler)
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

    fun pull(module: Module) {
        ProgressManager.getInstance().run(object : Task.Modal(module.project, "Updating your exercise files...", false) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                val repo = module.repository()
                doPull(repo, module.project, LocalFileSystem.getInstance().findFileByPath(ModuleUtil.getModuleDirPath(module))!!)
            }
        })
    }

    fun pull(project: Project) {
        ProgressManager.getInstance().run(object : Task.Modal(project, "Updating your exercise files...", false) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                val repo = getDefaultRootRepository(project)!!
                doPull(repo, project, OrionFileUtils.getRoot(project)!!)
            }
        })
    }

    private fun doPull(repo: GitRepository, project: Project, root: VirtualFile) {
        val remote = repo.remotes.first()
        val handler = GitLineHandler(project, root, GitCommand.PULL)
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
            VfsUtil.markDirtyAndRefresh(false, true, true, root)
        }
    }

    private fun masterOf(repository: GitRepository) = repository.branches.remoteBranches.first { it.name == "origin/master" }

    private fun getDefaultRootRepository(project: Project): GitRepository? {
        val gitRepositoryManager = ServiceManager.getService(project, GitRepositoryManager::class.java)
        val rootDir = OrionFileUtils.getRoot(project)
        //call to getRepositoryForRoot needs to be called in a background thread otherwise it throws a call in EDT exception.
        return ProgressManager.getInstance().runProcessWithProgressSynchronously(ThrowableComputable {
            gitRepositoryManager.getRepositoryForRoot(rootDir)
        }, "Getting default root repository", false, project)
    }

    fun updateExercise(project: Project) {
        project.service(DumbService::class.java).runWhenSmart {
            if (getDefaultRootRepository(project) == null) {
                project.messageBus.connect().subscribe(VcsRepositoryManager.VCS_REPOSITORY_MAPPING_UPDATED, VcsRepositoryMappingListener {
                    performUpdate(project)
                })
            } else {
                performUpdate(project)
            }
        }
    }

    private fun performUpdate(project: Project) {
        val registry = project.service(OrionInstructorExerciseRegistry::class.java)
        if (registry.isOpenedAsInstructor) {
            pull(project)
        } else {
            listOf(RepositoryType.TEST, RepositoryType.SOLUTION, RepositoryType.TEMPLATE)
                    .mapNotNull { it.moduleIn(project) }
                    .forEach { pull(it) }
        }
    }
}
