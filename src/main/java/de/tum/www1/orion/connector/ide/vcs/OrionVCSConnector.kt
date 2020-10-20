package de.tum.www1.orion.connector.ide.vcs

import com.intellij.openapi.components.ServiceManager
import com.jetbrains.rd.util.printlnError
import de.tum.www1.orion.connector.ide.OrionConnector
import de.tum.www1.orion.connector.ide.build.IOrionBuildConnector
import de.tum.www1.orion.connector.ide.vcs.submit.ChangeSubmissionContext
import de.tum.www1.orion.dto.RepositoryType
import de.tum.www1.orion.exercise.registry.OrionInstructorExerciseRegistry
import de.tum.www1.orion.ui.browser.BrowserWebView
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandlerAdapter
import java.util.*

class OrionVCSConnector(browserWebView: BrowserWebView) : OrionConnector(browserWebView), IOrionVCSConnector {
    override fun initializeHandlers() {
        jsQuery.addHandler { request ->
            val scanner = Scanner(request)
            val methodName = scanner.nextLine()
            printlnError("$methodName called")
            when (IOrionVCSConnector.FunctionName.valueOf(methodName)) {
                IOrionVCSConnector.FunctionName.submit ->
                    submit()
                IOrionVCSConnector.FunctionName.selectRepository ->
                    selectRepository(scanner.nextLine())
                }
            null
        }
        client.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                browser?.executeJavaScript("""
                    window.$connectorName={
                        ${IOrionVCSConnector.FunctionName.submit.name}: function() {
                            ${jsQuery.inject("""
                                '${IOrionVCSConnector.FunctionName.submit.name}'
                            """.trimIndent())}
                        },
                        ${IOrionVCSConnector.FunctionName.selectRepository}: function(repository){
                            ${jsQuery.inject("""
                                '${IOrionBuildConnector.FunctionName.onBuildStarted}' + '\n' + repository
                            """.trimIndent())}
                        }
                    };
                """, browser?.url, 0)
            }
        }, browser.cefBrowser)
    }

    override fun submit() {
        ServiceManager.getService(project, ChangeSubmissionContext::class.java).submitChanges()
    }

    override fun selectRepository(repository: String) {
        val parsedRepo = RepositoryType.valueOf(repository)
        ServiceManager.getService(project, OrionInstructorExerciseRegistry::class.java).selectedRepository = parsedRepo
    }
}