package de.tum.www1.orion.build

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.icons.AllIcons
import javax.swing.Icon

class ArtemisSubmitRunConfigurationType : ConfigurationType {
    override fun getIcon(): Icon {
        return AllIcons.General.Information
    }

    override fun getConfigurationTypeDescription(): String {
        return "Demo Artemis run config"
    }

    override fun getId(): String {
        return "ARTEMIS_RUN_CONFIG"
    }

    override fun getDisplayName(): String {
        return "Artemis Build and Test"
    }

    override fun getConfigurationFactories(): Array<ConfigurationFactory> {
        return arrayOf(ArtemisConfigurationFactory(this))
    }

}