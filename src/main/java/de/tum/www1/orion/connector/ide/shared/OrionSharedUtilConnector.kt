package de.tum.www1.orion.connector.ide.shared

import com.intellij.openapi.components.ServiceManager
import com.jetbrains.rd.util.printlnError
import de.tum.www1.orion.connector.ide.OrionConnector
import de.tum.www1.orion.ui.browser.BrowserWebView
import de.tum.www1.orion.vcs.OrionGitCredentialsService
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandlerAdapter
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
        jsQuery.addHandler { request ->
            val scanner = Scanner(request)
            val methodName = scanner.nextLine()
            printlnError("$methodName called")
            when (methodName) {
                loginMethodName -> login(scanner.nextLine(), scanner.nextLine())
                logMethodName ->{
                    log(scanner.nextLine())
                }
            }
            null
        }
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