package de.tum.www1.orion.connector.ide.shared

import com.intellij.openapi.components.ServiceManager
import de.tum.www1.orion.connector.ide.OrionConnector
import de.tum.www1.orion.ui.browser.BrowserWebView
import de.tum.www1.orion.util.cefRouter
import de.tum.www1.orion.vcs.OrionGitCredentialsService
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefQueryCallback
import org.cef.handler.CefLoadHandlerAdapter
import org.cef.handler.CefMessageRouterHandlerAdapter
import org.slf4j.LoggerFactory
import java.util.*

class OrionSharedUtilConnector(browserWebView: BrowserWebView) : OrionConnector(browserWebView), IOrionSharedUtilConnector {
    override fun login(username: String, password: String) {
        ServiceManager.getService(OrionGitCredentialsService::class.java).storeGitCredentials(username, password)
    }

    override fun log(message: String) {
        LoggerFactory.getLogger(OrionSharedUtilConnector::class.java).info(message)
    }

    override fun initializeHandlers() {
        val loginMethodName = IOrionSharedUtilConnector.FunctionName.login.name
        val logMethodName = IOrionSharedUtilConnector.FunctionName.log.name
        jsQuery.cefRouter.addHandler(object : CefMessageRouterHandlerAdapter() {
            override fun onQuery(browser: CefBrowser?, frame: CefFrame?, queryId: Long, request: String?, persistent: Boolean, callback: CefQueryCallback?): Boolean {
                request ?: return false
                val scanner = Scanner(request)
                val methodName = scanner.nextLine()
                when (methodName) {
                    loginMethodName -> login(scanner.nextLine(), scanner.nextLine())
                    logMethodName ->{
                        log(scanner.nextLine())
                    }
                    else -> return false
                }
                return true
            }
        }, false)
        client.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                browser?.executeJavaScript("""
                    window.$connectorName={
                        $loginMethodName: function(username, password) {
                            ${jsQuery.inject("""
                                '$loginMethodName' + '\n' + username + '\n' + password
                            """.trimIndent())}
                        },
                        $logMethodName: function(message){
                            ${jsQuery.inject("""
                                '$logMethodName' + '\n' + message
                            """.trimIndent())}
                        }
                    };
                """, browser?.url, 0)
            }
        }, browser.cefBrowser)
    }
}