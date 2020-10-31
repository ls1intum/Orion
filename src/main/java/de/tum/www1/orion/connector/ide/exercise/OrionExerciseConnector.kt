package de.tum.www1.orion.connector.ide.exercise

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.jcef.JBCefJSQuery
import de.tum.www1.orion.connector.ide.OrionConnector
import de.tum.www1.orion.dto.ProgrammingExercise
import de.tum.www1.orion.exercise.OrionExerciseService
import de.tum.www1.orion.ui.browser.IBrowser
import de.tum.www1.orion.util.JsonUtils.gson
import de.tum.www1.orion.util.nextAll
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefQueryCallback
import org.cef.handler.CefLoadHandlerAdapter
import org.cef.handler.CefMessageRouterHandlerAdapter
import java.util.*

/**
 * Java handler for when an exercise is first openned
 */
@Service
class OrionExerciseConnector(val project: Project) : OrionConnector(), IOrionExerciseConnector{

    override fun initializeHandlers(browser: IBrowser, queryInjector: JBCefJSQuery) {
        val editExerciseMethodName = IOrionExerciseConnector.FunctionName.editExercise.name
        val importParticipationMethodName = IOrionExerciseConnector.FunctionName.importParticipation.name
        browser.addJavaHandler(object : CefMessageRouterHandlerAdapter() {
            override fun onQuery(
                browser: CefBrowser?,
                frame: CefFrame?,
                queryId: Long,
                request: String?,
                persistent: Boolean,
                callback: CefQueryCallback?
            ): Boolean {
                request ?: return false
                val scanner = Scanner(request)
                when (scanner.nextLine()) {
                    editExerciseMethodName -> editExercise(scanner.nextAll())
                    importParticipationMethodName -> {
                        importParticipation(scanner.nextLine(), scanner.nextAll())
                    }
                    else -> return false
                }
                return true
            }
        })
        browser.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                browser?.executeJavaScript("""
                    window.$connectorName={
                        $editExerciseMethodName: function(exerciseJson) {
                            ${queryInjector.inject("""
                                '$editExerciseMethodName' + '\n' + exerciseJson
                            """.trimIndent())}
                        },
                        $importParticipationMethodName: function(repositoryUrl, exerciseJson){
                            ${queryInjector.inject("""
                                '$importParticipationMethodName' + '\n' + repositoryUrl + '\n' + exerciseJson
                            """.trimIndent())}
                        }
                    };
                """, browser.url, 0)
            }
        })
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