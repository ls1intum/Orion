package de.tum.www1.orion.connector.ide.shared

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.ui.jcef.JBCefJSQuery
import de.tum.www1.orion.connector.ide.OrionConnector
import de.tum.www1.orion.ui.browser.IBrowser
import de.tum.www1.orion.vcs.OrionGitCredentialsService
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefQueryCallback
import org.cef.handler.CefLoadHandlerAdapter
import org.cef.handler.CefMessageRouterHandlerAdapter
import org.slf4j.LoggerFactory
import java.util.*

/**
 * A Java Handler for when user logs into Artemis
 */
@Service
class OrionSharedUtilConnector(val project: Project) : OrionConnector(), IOrionSharedUtilConnector {

    override fun login(username: String, password: String) {
        ServiceManager.getService(OrionGitCredentialsService::class.java).storeGitCredentials(username, password)
    }

    override fun log(message: String) {
        LoggerFactory.getLogger(OrionSharedUtilConnector::class.java).info(message)
    }

    override fun initializeHandlers(browser: IBrowser, queryInjector: JBCefJSQuery) {
        val loginMethodName = IOrionSharedUtilConnector.FunctionName.login.name
        val logMethodName = IOrionSharedUtilConnector.FunctionName.log.name
        browser.addJavaHandler(object : CefMessageRouterHandlerAdapter() {
            override fun onQuery(browser: CefBrowser?, frame: CefFrame?, queryId: Long, request: String?, persistent: Boolean, callback: CefQueryCallback?): Boolean {
                request ?: return false
                val scanner = Scanner(request)
                when (scanner.nextLine()) {
                    loginMethodName -> login(scanner.nextLine(), scanner.nextLine())
                    logMethodName ->{
                        log(scanner.nextLine())
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
                        $loginMethodName: function(username, password) {
                            ${queryInjector.inject("""
                                '$loginMethodName' + '\n' + username + '\n' + password
                            """.trimIndent())}
                        },
                        $logMethodName: function(message){
                            ${queryInjector.inject("""
                                '$logMethodName' + '\n' + message
                            """.trimIndent())}
                        }
                    };
                """, browser.url, 0)
            }
        })
    }
}