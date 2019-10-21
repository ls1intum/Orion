package de.tum.www1.orion.build

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.icons.AllIcons
import javax.swing.Icon

class OrionSubmitRunConfigurationType : ConfigurationType {
    override fun getIcon(): Icon {
        return AllIcons.RunConfigurations.Remote
    }

    override fun getConfigurationTypeDescription(): String {
        return "Submit, build and test you code on the Artemis server and receive feedback and test results based on your submission"
    }

    override fun getId(): String {
        return "ARTEMIS_RUN_CONFIG"
    }

    override fun getDisplayName(): String {
        return "Artemis Build & Test"
    }

    override fun getConfigurationFactories(): Array<ConfigurationFactory> {
        return arrayOf(OrionConfigurationFactory(this))
    }

}