package de.tum.www1.orion.ui.browser

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.components.JBLabel
import com.intellij.ui.jcef.*
import com.intellij.util.messages.Topic
import de.tum.www1.orion.connector.ide.build.OrionBuildConnector
import de.tum.www1.orion.connector.ide.exercise.OrionExerciseConnector
import de.tum.www1.orion.connector.ide.shared.OrionSharedUtilConnector
import de.tum.www1.orion.connector.ide.vcs.OrionVCSConnector
import de.tum.www1.orion.settings.OrionSettingsProvider
import de.tum.www1.orion.ui.OrionRouter
import de.tum.www1.orion.util.cefRouter
import de.tum.www1.orion.util.getPrivateProperty
import org.cef.CefApp
import org.cef.CefSettings
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandler
import org.cef.handler.CefLoadHandlerAdapter
import org.cef.handler.CefMessageRouterHandler
import org.cef.handler.CefRequestHandlerAdapter
import org.cef.network.CefRequest
import java.util.*
import javax.swing.JComponent

/**
 * Initialize the browser and the associated settings, and provide the UI Component to add into the ToolWindow.
 */
class BrowserService(val project: Project) : IBrowser, Disposable {
    private val jbCefBrowser: JBCefBrowser
    private val jsQuery: JBCefJSQuery
    private val client: JBCefClient

    init {
        val version = ResourceBundle.getBundle("de.tum.www1.orion.Orion").getString("version")
        val userAgent = ServiceManager.getService(OrionSettingsProvider::class.java)
            .getSetting(OrionSettingsProvider.KEYS.USER_AGENT) + " Orion/" + version
        val route =
            project.service<OrionRouter>().routeForCurrentExercise() ?: project.service<OrionRouter>().defaultRoute()
        //Since JBCef wrapper doesn't support setting user-agent, we need to use reflection to access private properties.
        val jbCefAppInstance = JBCefApp.getInstance()
        val privateCefApp = jbCefAppInstance.getPrivateProperty<CefApp>("myCefApp")
        val privateCefSettings = privateCefApp.getPrivateProperty<CefSettings>("settings_")
        //Reading the source code of JBCefSettings we see that resource_dir_path should be the base path for JCEF, from
        //there we can buiild a path of cef_cache. This may not be true for Mac
        val jcefPath = privateCefSettings.resources_dir_path
        //Setting cache_path is necessary for saving logins.
        privateCefSettings.cache_path = "$jcefPath/cache"
        privateCefSettings.persist_session_cookies = true
        client = jbCefAppInstance.createClient()
        jbCefBrowser = JBCefBrowser(client, null)
        setUserAgentHandlerFor(userAgent)
        client.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                if (browser == null)
                    throw NullPointerException("Browser in onLoadEnd should not be null")
                else project.messageBus.syncPublisher(OrionBrowserNotifier.ORION_BROWSER_TOPIC)
                    .artemisLoadedWith(browser)
            }
        }, jbCefBrowser.cefBrowser)
        jsQuery = JBCefJSQuery.create(jbCefBrowser)
        //It is important that the just created jsQuery handlers is registered in the function below, before any browser
        //loading happen, if it's too late, then the window.cefQuery object won't be injected by JCEF
        injectJSBridge()
        jbCefBrowser.loadURL(route) //We only load until now to make sure that all handlers are registered
    }

    private fun setUserAgentHandlerFor(userAgent: String) {
        client.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadStart(
                browser: CefBrowser,
                frame: CefFrame?,
                transitionType: CefRequest.TransitionType?
            ) {
                browser.executeJavaScript("""
                    Object.defineProperty(navigator, 'userAgent', {
                        get: function () { return '${userAgent}'; }
                    });
                """.trimIndent(), browser.url, 0
                )
            }
        }, jbCefBrowser.cefBrowser)
    }

    private fun injectJSBridge() {
        project.service<OrionSharedUtilConnector>().initializeHandlers(this, jsQuery)
        project.service<OrionExerciseConnector>().initializeHandlers(this, jsQuery)
        project.service<OrionBuildConnector>().initializeHandlers(this, jsQuery)
        project.service<OrionVCSConnector>().initializeHandlers(this, jsQuery)
    }

    override fun addJavaHandler(handler: CefMessageRouterHandler) {
        jsQuery.cefRouter.addHandler(handler, false)
    }

    override fun addLoadHandler(handler: CefLoadHandler) {
        client.addLoadHandler(handler, jbCefBrowser.cefBrowser)
    }

    override val uiComponent: JComponent
        get() {
            if (!JBCefApp.isSupported()) {
                return JBLabel("JCEF support is not found in this IDE version. Please update your IDE.")
            }
            return jbCefBrowser.component
        }

    override fun dispose() {
        Disposer.dispose(jsQuery)
        Disposer.dispose(client)
    }
}

interface OrionBrowserNotifier {

    /**
     * Inform the JavascriptConnector that a browser has been loaded
     */
    fun artemisLoadedWith(engine: CefBrowser)

    companion object {
        @JvmField
        val ORION_BROWSER_TOPIC = Topic.create("Orion Browser Init", OrionBrowserNotifier::class.java)
    }
}