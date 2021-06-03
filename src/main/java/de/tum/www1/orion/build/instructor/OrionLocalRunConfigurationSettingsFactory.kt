package de.tum.www1.orion.build.instructor

import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.openapi.project.Project
import de.tum.www1.orion.enumeration.ProgrammingLanguage
import de.tum.www1.orion.util.selectedProgrammingLanguage

/**
 * Provides run configurations by delegating to the correct Provider depending on the language
 */
object OrionLocalRunConfigurationSettingsFactory {
    /**
     * Creates a run configuration for maven clean test working in the merged_test directory
     *
     * @param project project to generate the configuration for
     * @return the configuration
     */
    fun runConfigurationForInstructor(project: Project): RunnerAndConfigurationSettings {
        return when (project.selectedProgrammingLanguage()) {
            ProgrammingLanguage.JAVA -> JavaRunConfigurationProvider(project).provideBuildAndTestRunConfiguration("${project.basePath!!}${System.lineSeparator()}${OrionInstructorBuildUtil.LOCAL_TEST_DIRECTORY}")
            else -> throw IllegalArgumentException("Unsupported programming language for run configuration " + project.selectedProgrammingLanguage())
        }
    }

    /**
     * Creates a run configuration for maven clean test working in the base directory
     *
     * @param project project to generate the configuration for
     * @return the configuration
     */
    fun runConfigurationForTutor(project: Project): RunnerAndConfigurationSettings {
        return when (project.selectedProgrammingLanguage()) {
            // maven clean test should work for any language
            else -> JavaRunConfigurationProvider(project).provideBuildAndTestRunConfiguration(
                project.basePath!!
            )
        }
    }
}