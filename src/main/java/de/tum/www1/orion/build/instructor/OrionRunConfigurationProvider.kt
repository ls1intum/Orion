package de.tum.www1.orion.build.instructor

import com.intellij.execution.RunnerAndConfigurationSettings

interface OrionRunConfigurationProvider {
    fun provideBuildAndTestRunConfiguration(workingDir: String): RunnerAndConfigurationSettings?
}