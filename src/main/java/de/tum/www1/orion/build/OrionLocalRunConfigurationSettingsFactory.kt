package de.tum.www1.orion.build

import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.project.Project
import de.tum.www1.orion.build.instructor.JavaRunConfigurationProvider
import de.tum.www1.orion.build.instructor.OrionInstructorBuildUtil
import de.tum.www1.orion.enumeration.ProgrammingLanguage
import de.tum.www1.orion.util.selectedProgrammingLanguage
import java.io.File.separatorChar

/**
 * Provides run configurations by delegating to the correct Provider depending on the language
 */
object OrionLocalRunConfigurationSettingsFactory {
    /**
     * Creates a run configuration working in the merged_test directory
     *
     * @param project project to generate the configuration for
     * @return the configuration
     */
    fun runConfigurationForInstructor(project: Project): RunnerAndConfigurationSettings? {

        return when (project.selectedProgrammingLanguage()) {
            ProgrammingLanguage.JAVA -> {
                //check if its Intellij
                val applicationInfo = ApplicationInfo.getInstance().fullApplicationName
                if (!applicationInfo.contains("IntelliJ")) {
                    return null
                }

                JavaRunConfigurationProvider(project).provideBuildAndTestRunConfiguration("${project.basePath!!}$separatorChar${OrionInstructorBuildUtil.LOCAL_TEST_DIRECTORY}")
            }

            else -> return null
        }
    }

    /**
     * Creates a run configuration working in the base directory
     *
     * @param project project to generate the configuration for
     * @return the configuration
     */
    fun runConfigurationForTutor(project: Project): RunnerAndConfigurationSettings? {
        return when (project.selectedProgrammingLanguage()) {
            else -> JavaRunConfigurationProvider(project).provideBuildAndTestRunConfiguration(
                project.basePath!!
            )
        }
    }
}