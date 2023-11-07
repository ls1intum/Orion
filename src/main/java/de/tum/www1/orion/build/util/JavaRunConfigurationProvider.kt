package de.tum.www1.orion.build.util

import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jetbrains.idea.maven.execution.MavenRunConfiguration
import org.jetbrains.idea.maven.execution.MavenRunConfigurationType
import org.jetbrains.plugins.gradle.service.execution.GradleExternalTaskConfigurationType
import org.jetbrains.plugins.gradle.service.execution.GradleRunConfiguration
import java.io.File

class JavaRunConfigurationProvider(val project: Project) : OrionRunConfigurationProvider {
    /**
     * Provides a java run configuration for maven/gradle projects
     */
    override fun provideBuildAndTestRunConfiguration(workingDir: String): RunnerAndConfigurationSettings? {


        val gradleBuildFile = File("$workingDir/build.gradle")
        val mavenBuildFile = File("$workingDir/pom.xml")

        val runManager = project.service<RunManager>()

        // decide between maven and gradle
        if (gradleBuildFile.exists() && !gradleBuildFile.isDirectory) {
            val gradleRunConfigurationSettings = runManager.createConfiguration(
                "Orion Build & Test locally",
                GradleExternalTaskConfigurationType::class.java
            )
            (gradleRunConfigurationSettings.configuration as GradleRunConfiguration).apply {
                rawCommandLine = "clean test"

            }
            return gradleRunConfigurationSettings
        } else if (mavenBuildFile.exists() && !mavenBuildFile.isDirectory) {
            val mavenRunConfigurationSettings =
                runManager.createConfiguration(
                    "Orion Maven Build & Test Locally",
                    MavenRunConfigurationType::class.java
                )
            (mavenRunConfigurationSettings.configuration as MavenRunConfiguration).apply {
                runnerParameters.apply {

                    goals = listOf("clean", "test")
                    workingDirPath = workingDir
                }
            }
            return mavenRunConfigurationSettings
        }
        return null
    }
}
