package de.tum.www1.artemis.plugin.intellij.vcs

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import de.tum.www1.artemis.plugin.intellij.util.ArtemisExerciseRegistry

class ArtemiStartupProjectRefreshActivity : StartupActivity {
    override fun runActivity(project: Project) {
        val registry = ServiceManager.getService(project, ArtemisExerciseRegistry::class.java)
        if (registry.isArtemisExercise) {
            registry.registerPendingExercises()
            ArtemisGitUtil.pull(project)
        }
    }
}