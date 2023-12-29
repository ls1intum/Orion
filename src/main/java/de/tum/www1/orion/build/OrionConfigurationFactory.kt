package de.tum.www1.orion.build

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project
import de.tum.www1.orion.build.util.OrionRunConfiguration
import de.tum.www1.orion.build.util.OrionSubmitRunConfigurationType

class OrionConfigurationFactory(type: OrionSubmitRunConfigurationType) : ConfigurationFactory(type) {

    /*We need to override this because it is required.
      See https://github.com/JetBrains/intellij-community/blob/master/platform/lang-api/src/com/intellij/execution/configurations/ConfigurationFactory.java
     */
    override fun getId(): String {
        return "Artemis Build & Test"
    }

    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return OrionRunConfiguration(project, this, "Artemis Build & Test RunConfigurationFactory")
    }
}