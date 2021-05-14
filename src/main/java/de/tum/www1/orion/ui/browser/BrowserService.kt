package de.tum.www1.orion.ui.browser

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.jcef.*
import com.intellij.util.messages.Topic
import de.tum.www1.orion.connector.ide.build.OrionBuildConnector
import de.tum.www1.orion.connector.ide.exercise.OrionExerciseConnector
import de.tum.www1.orion.connector.ide.shared.OrionSharedUtilConnector
import de.tum.www1.orion.connector.ide.vcs.OrionVCSConnector
import de.tum.www1.orion.settings.OrionSettingsProvider
import de.tum.www1.orion.ui.OrionRouter
import de.tum.www1.orion.ui.util.UrlAccessForbiddenWarning
import de.tum.www1.orion.util.cefRouter
import de.tum.www1.orion.util.getPrivateProperty
import org.cef.CefApp
import org.cef.CefSettings
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandler
import org.cef.handler.CefLoadHandlerAdapter
import org.cef.handler.CefMessageRouterHandler
import org.cef.network.CefRequest
import org.jetbrains.annotations.NotNull
import java.util.*
import javax.swing.JComponent

/**
 * Initialize the browser and the associated settings, and provide the UI Component to add into the ToolWindow.
 */
class BrowserService(val project: Project) : IBrowser, Disposable {
    private lateinit var jbCefBrowser: JBCefBrowser
    private lateinit var jsQuery: JBCefJSQuery
    private lateinit var client: JBCefClient
    private lateinit var route: String

    override fun init() {
        if (!JBCefApp.isSupported()) {
            //Return early to prevent exceptions when initializing JBCef
            return
        }
        val version = ResourceBundle.getBundle("de.tum.www1.orion.Orion").getString("version")
        val userAgent = ServiceManager.getService(OrionSettingsProvider::class.java)
            .getSetting(OrionSettingsProvider.KEYS.USER_AGENT) + " Orion/" + version
        route =
            project.service<OrionRouter>().routeForCurrentExercise() ?: project.service<OrionRouter>().defaultRoute()
        //Since JBCef wrapper doesn't support setting user-agent, we need to use reflection to access private properties.
        val jbCefAppInstance = JBCefApp.getInstance()
        val privateCefApp = jbCefAppInstance.getPrivateProperty<CefApp>("myCefApp")
        val privateCefSettings = privateCefApp.getPrivateProperty<CefSettings>("settings_")
        //Reading the source code of JBCefSettings we see that resource_dir_path should be the base path for JCEF, from
        //there we can build a path of cef_cache. This may not be true for Mac
        val jcefPath = privateCefSettings.resources_dir_path
        //Setting cache_path is necessary for saving logins.
        privateCefSettings.cache_path = "$jcefPath/cache"
        privateCefSettings.persist_session_cookies = true
        privateCefSettings.user_agent = userAgent
        client = jbCefAppInstance.createClient()
        jbCefBrowser = JBCefBrowser(client, null)
        //alwaysCheckForValidArtemisUrl() Temporary removed for external logins.
        addArtemisWebappLoadedNotifier()
        client.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                if (browser == null)
                    throw NullPointerException("Browser in onLoadEnd should not be null")
                else project.messageBus.syncPublisher(OrionBrowserNotifier.ORION_BROWSER_TOPIC)
                    .artemisLoadedWith(browser)
            }
        }, jbCefBrowser.cefBrowser)
        jsQuery = JBCefJSQuery.create(jbCefBrowser as @NotNull JBCefBrowserBase)
        //It is important that the just created jsQuery handlers is registered in the function below, before any browser
        //loading happen, if it's too late, then the window.cefQuery object won't be injected by JCEF
        injectJSBridge()
        jbCefBrowser.loadURL(route) //We only load until now to make sure that all handlers are registered
    }

    private fun alwaysCheckForValidArtemisUrl() {
        client.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadStart(
                browser: CefBrowser?,
                frame: CefFrame?,
                transitionType: CefRequest.TransitionType?
            ) {
                val artemisUrl = service<OrionSettingsProvider>().getSetting(OrionSettingsProvider.KEYS.ARTEMIS_URL)
                if (frame?.url != null && frame.url != "about:blank" && !frame.url.startsWith(artemisUrl)) {
                    runInEdt { UrlAccessForbiddenWarning(project).show() }
                    jbCefBrowser.loadURL(artemisUrl)
                }
            }
        }, jbCefBrowser.cefBrowser)
    }

    private fun addArtemisWebappLoadedNotifier() {
        client.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                val artemisUrl = service<OrionSettingsProvider>().getSetting(OrionSettingsProvider.KEYS.ARTEMIS_URL)
                if (frame?.url != null && frame.url.startsWith(artemisUrl)) {
                    project.messageBus.syncPublisher(ArtemisWebappStatusNotifier.ORION_SITE_LOADED_TOPIC)
                        .webappLoaded()
                }
            }
        }, jbCefBrowser.cefBrowser)
    }

    override fun returnToHomepage() {
        if (isInitialized)
            jbCefBrowser.loadURL(route)
    }

    override val isInitialized: Boolean
        get() = ::jbCefBrowser.isInitialized

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
            if (::jbCefBrowser.isInitialized.not()) {
                return JBTextArea(JCEF_ERROR_MESSAGE, 10, 90).apply {
                    lineWrap = true
                    wrapStyleWord = true
                }
            }
            return jbCefBrowser.component
        }

    override fun dispose() {
        if (::jsQuery.isInitialized) Disposer.dispose(jsQuery)
        if (::client.isInitialized) Disposer.dispose(client)
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

interface ArtemisWebappStatusNotifier {
    fun webappLoaded()

    companion object {
        @JvmField
        val ORION_SITE_LOADED_TOPIC = Topic.create("Orion Webapp Loaded", ArtemisWebappStatusNotifier::class.java)
    }
}

private const val JCEF_ERROR_MESSAGE: String =
    """
JCEF support is not found in this IDE version (It is enabled by default in IntelliJ 2020.2).
Please update your IDE and make sure that the JCEF feature in IntelliJ is enabled.
To enable ide.browser.jcef.enabled in Registry dialog, invoke Help | Find Action and type “Registry” and restart the IDE for changes to take effect.
        
If the problem persists, please install the "Choose Runtime" plugin from Help -> Find Action -> Type "Plugins"-> Marketplace. Invoke the plugin from
Find Action -> Choose Runtime and install and use the latest jbsdk version from the list.
        
Consult this: https://youtrack.jetbrains.com/issue/IDEA-231833#focus=streamItem-27-3993099.0-0
    """
