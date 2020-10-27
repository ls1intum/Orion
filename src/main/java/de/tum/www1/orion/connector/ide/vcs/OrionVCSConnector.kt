package de.tum.www1.orion.connector.ide.vcs

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.ui.jcef.JBCefJSQuery
import de.tum.www1.orion.connector.ide.OrionConnector
import de.tum.www1.orion.connector.ide.build.IOrionBuildConnector
import de.tum.www1.orion.connector.ide.vcs.submit.ChangeSubmissionContext
import de.tum.www1.orion.dto.RepositoryType
import de.tum.www1.orion.exercise.registry.OrionInstructorExerciseRegistry
import de.tum.www1.orion.ui.browser.IBrowser
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefQueryCallback
import org.cef.handler.CefLoadHandlerAdapter
import org.cef.handler.CefMessageRouterHandlerAdapter
import java.util.*

@Service
class OrionVCSConnector(val project: Project) : OrionConnector(), IOrionVCSConnector {

    /**
     * This method now does nothing, the submitting is now delegated to onBuildStarted() so it has more information on
     * whether or not the commit is successful and acts accordingly. The server always call onBuildStarted() after submit()
     * any way.
     */
    override fun submit() {
    }

    override fun selectRepository(repository: String) {
        val parsedRepo = RepositoryType.valueOf(repository)
        ServiceManager.getService(project, OrionInstructorExerciseRegistry::class.java).selectedRepository = parsedRepo
    }

    override fun initializeHandlers(browser: IBrowser, queryInjector: JBCefJSQuery) {
        browser.addJavaHandler(object : CefMessageRouterHandlerAdapter() {
            override fun onQuery(browser: CefBrowser?, frame: CefFrame?, queryId: Long, request: String?, persistent: Boolean, callback: CefQueryCallback?): Boolean {
                val scanner = Scanner(request)
                val methodName = scanner.nextLine()
                val methodNameEnum = IOrionVCSConnector.FunctionName.values().find {
                    it.name==methodName
                } ?: return false
                when (methodNameEnum) {
                    IOrionVCSConnector.FunctionName.submit ->
                        submit()
                    IOrionVCSConnector.FunctionName.selectRepository ->
                        selectRepository(scanner.nextLine())
                }
                return true
            }
        })
        browser.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                browser?.executeJavaScript("""
                    window.$connectorName={
                        ${IOrionVCSConnector.FunctionName.submit.name}: function() {
                            ${queryInjector.inject("""
                                '${IOrionVCSConnector.FunctionName.submit.name}'
                            """.trimIndent())}
                        },
                        ${IOrionVCSConnector.FunctionName.selectRepository}: function(repository){
                            ${queryInjector.inject("""
                                '${IOrionBuildConnector.FunctionName.onBuildStarted}' + '\n' + repository
                            """.trimIndent())}
                        }
                    };
                """, browser.url, 0)
            }
        })
    }
}