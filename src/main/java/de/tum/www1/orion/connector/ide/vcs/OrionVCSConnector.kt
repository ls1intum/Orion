package de.tum.www1.orion.connector.ide.vcs

import com.intellij.openapi.components.ServiceManager
import de.tum.www1.orion.connector.ide.OrionConnector
import de.tum.www1.orion.connector.ide.build.IOrionBuildConnector
import de.tum.www1.orion.connector.ide.vcs.submit.ChangeSubmissionContext
import de.tum.www1.orion.dto.RepositoryType
import de.tum.www1.orion.exercise.registry.OrionInstructorExerciseRegistry
import de.tum.www1.orion.ui.browser.BrowserWebView
import de.tum.www1.orion.util.cefRouter
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefQueryCallback
import org.cef.handler.CefLoadHandlerAdapter
import org.cef.handler.CefMessageRouterHandlerAdapter
import java.util.*

class OrionVCSConnector(browserWebView: BrowserWebView) : OrionConnector(browserWebView), IOrionVCSConnector {
    override fun initializeHandlers() {
        jsQuery.cefRouter.addHandler(object : CefMessageRouterHandlerAdapter() {
            override fun onQuery(browser: CefBrowser?, frame: CefFrame?, queryId: Long, request: String?, persistent: Boolean, callback: CefQueryCallback?): Boolean {
                val scanner = Scanner(request)
                val methodName = scanner.nextLine()
                val methodNameEnum = try {
                    IOrionVCSConnector.FunctionName.valueOf(methodName)
                } catch (e: IllegalArgumentException) {
                    return false
                }
                when (methodNameEnum) {
                    IOrionVCSConnector.FunctionName.submit ->
                        submit()
                    IOrionVCSConnector.FunctionName.selectRepository ->
                        selectRepository(scanner.nextLine())
                }
                return true
            }
        }, false)
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