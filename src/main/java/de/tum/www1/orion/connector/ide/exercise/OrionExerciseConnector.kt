package de.tum.www1.orion.connector.ide.exercise

import com.intellij.openapi.components.service
import de.tum.www1.orion.connector.ide.OrionConnector
import de.tum.www1.orion.dto.ProgrammingExercise
import de.tum.www1.orion.exercise.OrionExerciseService
import de.tum.www1.orion.ui.browser.BrowserWebView
import de.tum.www1.orion.util.JsonUtils.gson
import de.tum.www1.orion.util.cefRouter
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefQueryCallback
import org.cef.handler.CefLoadHandlerAdapter
import org.cef.handler.CefMessageRouterHandlerAdapter
import java.util.*

class OrionExerciseConnector(browserWebView: BrowserWebView) : OrionConnector(browserWebView), IOrionExerciseConnector{

    override fun initializeHandlers() {
        val editExerciseMethodName = IOrionExerciseConnector.FunctionName.editExercise.name
        val importParticipationMethodName = IOrionExerciseConnector.FunctionName.importParticipation.name
        jsQuery.cefRouter.addHandler(object : CefMessageRouterHandlerAdapter() {
            override fun onQuery(browser: CefBrowser?, frame: CefFrame?, queryId: Long, request: String?, persistent: Boolean, callback: CefQueryCallback?): Boolean {
                request ?: return false
                val scanner = Scanner(request)
                val methodName = scanner.nextLine()
                when (methodName) {
                    editExerciseMethodName -> editExercise(scanner.nextLine())
                    importParticipationMethodName -> {
                        importParticipation(scanner.nextLine(), scanner.nextLine())
                    }
                    else ->return false
                }
                return true
            }
        }, false)
        client.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                browser?.executeJavaScript("""
                    window.$connectorName={
                        $editExerciseMethodName: function(exerciseJson) {
                            ${jsQuery.inject("""
                                '$editExerciseMethodName' + '\n' + exerciseJson
                            """.trimIndent())}
                        },
                        $importParticipationMethodName: function(repositoryUrl, exerciseJson){
                            ${jsQuery.inject("""
                                '$importParticipationMethodName' + '\n' + repositoryUrl + '\n' + exerciseJson
                            """.trimIndent())}
                        }
                    };
                """, browser?.url, 0)
            }
        }, browser.cefBrowser)
    }

    override fun editExercise(exerciseJson: String) {
        val exercise = gson().fromJson(exerciseJson, ProgrammingExercise::class.java)
        project.service<OrionExerciseService>().editExercise(exercise)
    }

    override fun importParticipation(repositoryUrl: String, exerciseJson: String) {
        val exercise = gson().fromJson(exerciseJson, ProgrammingExercise::class.java)
        project.service<OrionExerciseService>().importParticipation(repositoryUrl, exercise)
    }
}