package de.tum.www1.orion.build.util

import com.intellij.execution.Executor
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.RunConfigurationWithSuppressedDefaultRunAction
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import de.tum.www1.orion.build.OrionCommandLineState
import de.tum.www1.orion.build.OrionConfigurationFactory
import de.tum.www1.orion.build.OrionSettingsEditor

class OrionRunConfiguration(project: Project, factory: OrionConfigurationFactory, name: String)
    : RunConfigurationBase<OrionCommandLineState>(project, factory, name), RunConfigurationWithSuppressedDefaultRunAction {
    var triggeredInIDE = true

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return OrionSettingsEditor()
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return OrionCommandLineState(project, environment)
    }

}