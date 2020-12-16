package de.tum.www1.orion.build

import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.runners.GenericProgramRunner

class OrionProgramRunner : GenericProgramRunner<RunnerSettings>() {
    override fun canRun(executorId: String, profile: RunProfile): Boolean {
        return profile is OrionRunConfiguration
    }

    override fun getRunnerId(): String {
        return "de.tum.www1.orion.build.runner"
    }
}