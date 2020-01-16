package de.tum.www1.orion.build.instructor

import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.openapi.project.Project
import de.tum.www1.orion.enumeration.ProgrammingLanguage
import de.tum.www1.orion.util.selectedProgrammingLangauge

object OrionLocalRunConfigurationSettingsFactory {
    fun runConfigurationForInstructor(project: Project): RunnerAndConfigurationSettings {
        return when (project.selectedProgrammingLangauge()) {
            ProgrammingLanguage.JAVA -> JavaRunConfigurationProvider(project).proivdeBuildAndTestRunConfiguration()
            else -> throw IllegalArgumentException("Unsupported programming language for run configuration " + project.selectedProgrammingLangauge())
        }
    }
}