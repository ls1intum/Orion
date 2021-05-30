package de.tum.www1.orion.connector.ide.build

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.ExecutionUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.jcef.JBCefJSQuery
import de.tum.www1.orion.build.OrionRunConfiguration
import de.tum.www1.orion.build.OrionSubmitRunConfigurationType
import de.tum.www1.orion.build.OrionTestParser
import de.tum.www1.orion.build.instructor.OrionInstructorBuildUtil
import de.tum.www1.orion.connector.ide.OrionConnector
import de.tum.www1.orion.connector.ide.vcs.submit.ChangeSubmissionContext
import de.tum.www1.orion.dto.BuildError
import de.tum.www1.orion.dto.BuildLogFileErrorsDTO
import de.tum.www1.orion.dto.RepositoryType
import de.tum.www1.orion.exercise.registry.OrionProjectRegistryStateService
import de.tum.www1.orion.ui.browser.IBrowser
import de.tum.www1.orion.util.nextAll
import java.util.*
import java.util.stream.Collectors

@Service
class OrionBuildConnector(val project: Project) : OrionConnector(), IOrionBuildConnector {
    override fun buildAndTestLocally() {
        //Very important to execute the following task in a pooled thread. For some reason the IDE will crush violently
        //if executed on the CEF message handler thread
        ApplicationManager.getApplication().executeOnPooledThread {
            project.service<OrionInstructorBuildUtil>().runTestsLocally()
        }
    }

    override fun onBuildStarted(exerciseInstructions: String) {
        // Only listen to the first execution result
        if (exerciseInstructions == "TEMPLATE") {
            //The Artemis server for some reason spontaneously send onBuildStarted event with parameter TEMPLATE when
            //opening the instructor page for the first time, this is a work around for that issue.
            project.service<OrionProjectRegistryStateService>().state?.selectedRepository = RepositoryType.TEMPLATE
            return
        }
        //We do not want to block the running thread, otherwise there will be some weird hang up
        ApplicationManager.getApplication().executeOnPooledThread {
            if (!project.service<ChangeSubmissionContext>().submitChanges()) {
                //Something fails with git, we don't start the build process
                return@executeOnPooledThread
            }
            val testParser = ServiceManager.getService(project, OrionTestParser::class.java)
            if (!testParser.isAttachedToProcess) {
                val runManager = RunManager.getInstance(project)
                val settings = runManager
                    .createConfiguration("Build & Test on Artemis Server", OrionSubmitRunConfigurationType::class.java)
                (settings.configuration as OrionRunConfiguration).triggeredInIDE = false
                ExecutionUtil.runConfiguration(settings, DefaultRunExecutor.getRunExecutorInstance())
                testParser.parseTestTreeFrom(exerciseInstructions)
            }
        }
    }

    override fun onBuildFinished() {
        ServiceManager.getService(project, OrionTestParser::class.java).onTestingFinished()
    }

    override fun onBuildFailed(buildLogsJsonString: String) {
        val mapType = object : TypeToken<Map<String?, List<BuildError?>?>?>() {}.type
        val allErrors = Gson().fromJson(buildLogsJsonString, JsonObject::class.java)["error"]
        val errors = Gson().fromJson<Map<String, List<BuildError>>>(allErrors, mapType)
        val buildErrors = errors.entries.stream()
            .map { fileErrors: Map.Entry<String, List<BuildError>> ->
                BuildLogFileErrorsDTO(
                    fileErrors.key,
                    fileErrors.value
                )
            }
            .collect(Collectors.toList())
        val testParser = ServiceManager.getService(project, OrionTestParser::class.java)
        testParser.onCompileError(buildErrors)
    }

    override fun onTestResult(success: Boolean, testName: String, message: String) {
        ServiceManager.getService(project, OrionTestParser::class.java).onTestResult(success, testName, message)
    }

    override fun initializeHandlers(browser: IBrowser, queryInjector: JBCefJSQuery) {
        val reactions = mapOf("buildAndTestLocally" to { buildAndTestLocally() },
            "onBuildStarted" to { scanner: Scanner -> onBuildStarted(scanner.nextAll()) },
            "onBuildFinished" to { onBuildFinished() },
            "onBuildFailed" to { scanner: Scanner -> onBuildFailed(scanner.nextAll()) },
            "onTestResult" to { scanner: Scanner ->
                onTestResult(
                    scanner.nextLine().toBoolean(),
                    scanner.nextLine(),
                    scanner.nextAll()
                )
            })
        addJavaHandler(browser, reactions)

        val parameterNames = mapOf(
            "buildAndTestLocally" to listOf(),
            "onBuildStarted" to listOf("exerciseInstructions"),
            "onBuildFinished" to listOf(),
            "onBuildFailed" to listOf("buildLogsJsonString"),
            "onTestResult" to listOf("success", "testName", "message"),
        )
        addLoadHandler(browser, queryInjector, parameterNames)
    }
}