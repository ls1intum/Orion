package de.tum.www1.orion.build

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project

class OrionConfigurationFactory(type: OrionSubmitRunConfigurationType) : ConfigurationFactory(type) {
    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return OrionRunConfiguration(project, this, "Artemis Build & Test RunConfigurationFactory")
    }
}