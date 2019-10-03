package de.tum.www1.orion.build

import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.runners.DefaultProgramRunner

class ArtemisProgramRunner : DefaultProgramRunner() {
    override fun canRun(executorId: String, profile: RunProfile): Boolean {
        return profile is ArtemisRunConfiguration
    }

    override fun getRunnerId(): String {
        return "de.tum.www1.orion.build.runner"
    }
}