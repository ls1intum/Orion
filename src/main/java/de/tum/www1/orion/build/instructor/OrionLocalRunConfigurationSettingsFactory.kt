package de.tum.www1.orion.build.instructor

import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.openapi.project.Project
import de.tum.www1.orion.enumeration.ProgrammingLanguage
import de.tum.www1.orion.util.selectedProgrammingLanguage

object OrionLocalRunConfigurationSettingsFactory {
    fun runConfigurationForInstructor(project: Project): RunnerAndConfigurationSettings {
        return when (project.selectedProgrammingLanguage()) {
            ProgrammingLanguage.JAVA -> JavaRunConfigurationProvider(project).provideBuildAndTestRunConfiguration()
            else -> throw IllegalArgumentException("Unsupported programming language for run configuration " + project.selectedProgrammingLanguage())
        }
    }
}