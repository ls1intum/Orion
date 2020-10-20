package de.tum.www1.orion.connector.ide.build

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.ExecutionUtil
import com.intellij.openapi.components.ServiceManager
import com.jetbrains.rd.util.printlnError
import de.tum.www1.orion.build.OrionRunConfiguration
import de.tum.www1.orion.build.OrionSubmitRunConfigurationType
import de.tum.www1.orion.build.OrionTestParser
import de.tum.www1.orion.build.instructor.OrionInstructorBuildUtil
import de.tum.www1.orion.connector.ide.OrionConnector
import de.tum.www1.orion.dto.BuildError
import de.tum.www1.orion.dto.BuildLogFileErrorsDTO
import de.tum.www1.orion.ui.browser.BrowserWebView
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandlerAdapter
import java.util.*
import java.util.stream.Collectors

class OrionBuildConnector(browserWebView: BrowserWebView) : OrionConnector(browserWebView), IOrionBuildConnector {
    override fun buildAndTestLocally() {
        ServiceManager.getService(project, OrionInstructorBuildUtil::class.java).runTestsLocally()
    }

    override fun onBuildStarted(exerciseInstructions: String) {
        // Only listen to the first execution result
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

    override fun onBuildFinished() {
        ServiceManager.getService(project, OrionTestParser::class.java).onTestingFinished()
    }

    override fun onBuildFailed(buildLogsJsonString: String) {
        val mapType = object : TypeToken<Map<String?, List<BuildError?>?>?>() {}.type
        val allErrors = JsonParser().parse(buildLogsJsonString).asJsonObject["errors"]
        val errors = Gson().fromJson<Map<String, List<BuildError>>>(allErrors, mapType)
        val buildErrors = errors.entries.stream()
                .map { fileErrors: Map.Entry<String, List<BuildError>> -> BuildLogFileErrorsDTO(fileErrors.key, fileErrors.value) }
                .collect(Collectors.toList())
        val testParser = ServiceManager.getService(project, OrionTestParser::class.java)
        testParser.onCompileError(buildErrors)
    }

    override fun onTestResult(success: Boolean, testName: String, message: String) {
        ServiceManager.getService(project, OrionTestParser::class.java).onTestResult(success, testName, message)
    }

    override fun initializeHandlers() {
        jsQuery.addHandler { request ->
            val scanner = Scanner(request)
            val methodName = scanner.nextLine()
            printlnError("$methodName called")
            when (IOrionBuildConnector.FunctionName.valueOf(methodName)) {
                IOrionBuildConnector.FunctionName.buildAndTestLocally ->
                    buildAndTestLocally()
                IOrionBuildConnector.FunctionName.onBuildStarted ->{
                    onBuildStarted(scanner.nextLine())
                }
                IOrionBuildConnector.FunctionName.onBuildFinished->
                    onBuildFinished()
                IOrionBuildConnector.FunctionName.onTestResult->
                    onTestResult(scanner.nextLine()!!.toBoolean(), scanner.nextLine(), scanner.nextLine())
            }
            null
        }
        client.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                browser?.executeJavaScript("""
                    window.$connectorName={
                        ${IOrionBuildConnector.FunctionName.buildAndTestLocally.name}: function() {
                            ${jsQuery.inject("""
                                '${IOrionBuildConnector.FunctionName.buildAndTestLocally.name}'
                            """.trimIndent())}
                        },
                        ${IOrionBuildConnector.FunctionName.onBuildStarted}: function(exerciseInstructions){
                            ${jsQuery.inject("""
                                '${IOrionBuildConnector.FunctionName.onBuildStarted}' + '\n' + exerciseInstructions
                            """.trimIndent())}
                        },
                        ${IOrionBuildConnector.FunctionName.onBuildFinished.name}: function() {
                            ${jsQuery.inject("""
                                '${IOrionBuildConnector.FunctionName.onBuildFinished.name}'
                            """.trimIndent())}
                        },
                        ${IOrionBuildConnector.FunctionName.onBuildFailed.name}: function(buildLogsJsonString) {
                            ${jsQuery.inject("""
                                '${IOrionBuildConnector.FunctionName.onBuildFailed.name}' + '\n' + buildLogsJsonString
                            """.trimIndent())}
                        },
                        ${IOrionBuildConnector.FunctionName.onTestResult.name}: function(success, testName, message) {
                            ${jsQuery.inject("""
                                '${IOrionBuildConnector.FunctionName.onTestResult.name}' + '\n' + success + '\n' + testName + '\n' + message 
                            """.trimIndent())}
                        }
                    };
                """, browser?.url, 0)
            }
        }, browser.cefBrowser)
    }
}