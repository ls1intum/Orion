package de.tum.www1.orion.build

import com.intellij.execution.ExecutionResult
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.GenericProgramRunner
import com.intellij.execution.runners.ProgramRunner
import com.intellij.execution.runners.RunContentBuilder
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.openapi.fileEditor.FileDocumentManager
import de.tum.www1.orion.build.util.OrionRunConfiguration

class OrionProgramRunner : GenericProgramRunner<RunnerSettings>() {
    override fun canRun(executorId: String, profile: RunProfile): Boolean {
        return profile is OrionRunConfiguration
    }

    override fun getRunnerId(): String {
        return "de.tum.www1.orion.build.runner"
    }

    override fun doExecute(state: RunProfileState, environment: ExecutionEnvironment): RunContentDescriptor? {
        return executeState(state, environment, this)
    }

    private fun executeState(state: RunProfileState, environment: ExecutionEnvironment, runner: ProgramRunner<*>): RunContentDescriptor? {
        FileDocumentManager.getInstance().saveAllDocuments()
        return showRunContent(state.execute(environment.executor, runner), environment)
    }

    private fun showRunContent(executionResult: ExecutionResult?, environment: ExecutionEnvironment): RunContentDescriptor? {
        return executionResult?.let {
            RunContentBuilder(it, environment).showRunContent(environment.contentToReuse)
        }
    }
}