package de.tum.www1.orion.build

import com.intellij.execution.Executor
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.RunConfigurationWithSuppressedDefaultRunAction
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project

class ArtemisRunConfiguration(project: Project, factory: ArtemisConfigurationFactory, name: String)
    : RunConfigurationBase<ArtemisCommandLineState>(project, factory, name), RunConfigurationWithSuppressedDefaultRunAction {

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return ArtemisSettingsEditor()
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState? {
        return ArtemisCommandLineState(project, environment)
    }

}