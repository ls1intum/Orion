package de.tum.www1.orion.build.instructor

import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.openapi.project.Project
import de.tum.www1.orion.util.service
import org.jetbrains.idea.maven.execution.MavenRunConfigurationType

class JavaRunConfigurationProvider(val project: Project) : OrionRunConfigurationProvider {
    override fun proivdeBuildAndTestRunConfiguration(): RunnerAndConfigurationSettings {
        val runManager = project.service(RunManager::class.java)
        val runConfiguration = runManager.createConfiguration("Orion Maven Build & Test Locally", MavenRunConfigurationType::class.java)

        return runConfiguration
    }
}