package de.tum.www1.orion.build.instructor

import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.openapi.project.Project
import de.tum.www1.orion.util.service
import org.jetbrains.idea.maven.execution.MavenRunConfiguration
import org.jetbrains.idea.maven.execution.MavenRunConfigurationType
import java.io.File

class JavaRunConfigurationProvider(val project: Project) : OrionRunConfigurationProvider {
    override fun proivdeBuildAndTestRunConfiguration(): RunnerAndConfigurationSettings {
        val runManager = project.service(RunManager::class.java)
        val runConfigurationSettings = runManager.createConfiguration("Orion Maven Build & Test Locally", MavenRunConfigurationType::class.java)
        (runConfigurationSettings.configuration as MavenRunConfiguration).apply {
            runnerParameters.apply {
                goals = listOf("clean", "test")
                workingDirPath = project.basePath!! + File.separatorChar + OrionInstructorBuildUtil.LOCAL_TEST_DIRECTORY
            }
        }

        return runConfigurationSettings
    }
}