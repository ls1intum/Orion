package de.tum.www1.orion.build

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project

class ArtemisConfigurationFactory(type: ArtemisSubmitRunConfigurationType) : ConfigurationFactory(type) {
    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return ArtemisRunConfiguration(project, this, "Artemis Build & Test RunConfigurationFactory")
    }
}