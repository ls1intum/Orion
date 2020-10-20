package de.tum.www1.orion.connector.ide.exercise

import com.intellij.openapi.components.service
import com.jetbrains.rd.util.printlnError
import de.tum.www1.orion.connector.ide.OrionConnector
import de.tum.www1.orion.dto.ProgrammingExercise
import de.tum.www1.orion.exercise.OrionExerciseService
import de.tum.www1.orion.ui.browser.BrowserWebView
import de.tum.www1.orion.util.JsonUtils.gson
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandlerAdapter
import java.util.*

class OrionExerciseConnector(browserWebView: BrowserWebView) : OrionConnector(browserWebView), IOrionExerciseConnector{

    override fun initializeHandlers() {
        val editExerciseMethodName = IOrionExerciseConnector.FunctionName.editExercise.name
        val importParticipationMethodName = IOrionExerciseConnector.FunctionName.importParticipation.name
        jsQuery.addHandler { request ->
            val scanner = Scanner(request)
            val methodName = scanner.nextLine()
            printlnError("$methodName called")
            when (methodName) {
                editExerciseMethodName -> editExercise(scanner.nextLine())
                importParticipationMethodName ->{
                    importParticipation(scanner.nextLine(), scanner.nextLine())
                }
            }
            null
        }
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