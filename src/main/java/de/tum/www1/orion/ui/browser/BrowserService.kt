package de.tum.www1.orion.ui.browser

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.jcef.*
import com.intellij.util.messages.Topic
import de.tum.www1.orion.connector.ide.build.OrionBuildConnector
import de.tum.www1.orion.connector.ide.exercise.OrionExerciseConnector
import de.tum.www1.orion.connector.ide.shared.OrionSharedUtilConnector
import de.tum.www1.orion.connector.ide.vcs.OrionVCSConnector
import de.tum.www1.orion.exercise.registry.OrionStudentExerciseRegistry
import de.tum.www1.orion.messaging.OrionIntellijStateNotifier
import de.tum.www1.orion.settings.OrionSettingsProvider
import de.tum.www1.orion.util.cefRouter
import de.tum.www1.orion.util.getPrivateProperty
import de.tum.www1.orion.util.returnToExercise
import org.cef.CefApp
import org.cef.CefSettings
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandler
import org.cef.handler.CefLoadHandlerAdapter
import org.cef.handler.CefMessageRouterHandler
import org.cef.network.CefRequest
import org.jetbrains.annotations.NotNull
import javax.swing.JComponent

/**
 * Initialize the browser and the associated settings, and provide the UI Component to add into the ToolWindow.
 */
class BrowserService(val project: Project) : IBrowser, Disposable {
    private lateinit var jbCefBrowser: JBCefBrowser
    private lateinit var jsQuery: JBCefJSQuery
    private lateinit var client: JBCefClient

    override fun init() {
        if (!JBCefApp.isSupported()) {
            //Return early to prevent exceptions when initializing JBCef
            return
        }
        val version = PluginManagerCore.getPlugin(PluginId.getId("de.tum.www1.orion"))?.version ?: "0.0.0"
        val userAgent = service<OrionSettingsProvider>()
            .getSetting(OrionSettingsProvider.KEYS.USER_AGENT) + " Orion/" + version
        // Since JBCef wrapper doesn't support setting user-agent, we need to use reflection to access private properties.
        // TODO This approach does not work reliably! If any other plugin uses JCEF and happens to be loaded before Orion, this will do nothing
        val jbCefAppInstance = JBCefApp.getInstance()
        val privateCefSettings =
            jbCefAppInstance.getPrivateProperty<CefApp>("myCefApp").getPrivateProperty<CefSettings>("settings_")
        privateCefSettings.persist_session_cookies = true


        // Throws exception if a JCEF client has already been created


        client = jbCefAppInstance.createClient()
        jbCefBrowser = JBCefBrowserBuilder().setClient(client).setUrl(null).build()
        addArtemisWebappLoadedNotifier()
        setUserAgentHandlerFor(userAgent)
        client.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                if (browser == null)
                    throw NullPointerException("Browser in onLoadEnd should not be null")
                else project.messageBus.syncPublisher(OrionBrowserNotifier.ORION_BROWSER_TOPIC)
                    .artemisLoadedWith(browser)
            }
        }, jbCefBrowser.cefBrowser)
        jsQuery = JBCefJSQuery.create(jbCefBrowser as @NotNull JBCefBrowserBase)
        // It is important that the just created jsQuery handlers are registered in the function below, before any browser
        // loading happens, if it's too late, then the window.cefQuery object won't be injected by JCEF
        injectJSBridge()
        // Only load any URL at the end to make sure that all handlers are registered
        // Since JCEF does not really support url query parameters, the navigation is done via javascript
        // However, to execute javascript, some url has to have been loaded, so we first load the artemis default url
        jbCefBrowser.loadURL(service<OrionSettingsProvider>().getSetting(OrionSettingsProvider.KEYS.ARTEMIS_URL))
        // And, after it has been loaded, start the actual navigation
        // Not entirely sure why we have to wait for two page-loadings, but otherwise it won't call the client connector correctly
        onNextLoadEnd { onNextLoadEnd { returnToExercise(project) } }
    }

    private fun setUserAgentHandlerFor(userAgent: String) {
        addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadStart(
                browser: CefBrowser,
                frame: CefFrame?,
                transitionType: CefRequest.TransitionType?
            ) {
                browser.executeJavaScript(
                    """
                    Object.defineProperty(navigator, 'userAgent', {
                        get: function () { return '${userAgent}'; },
                        configurable: true
                    });
                    """.trimIndent(), browser.url, 0
                )
            }
        })
    }

    private fun addArtemisWebappLoadedNotifier() {
        addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                val artemisUrl = service<OrionSettingsProvider>().getSetting(OrionSettingsProvider.KEYS.ARTEMIS_URL)
                if (frame?.url != null && frame.url.startsWith(artemisUrl)) {
                    project.messageBus.syncPublisher(ArtemisWebappStatusNotifier.ORION_SITE_LOADED_TOPIC)
                        .webappLoaded()
                }
            }
        })
    }

    private fun onNextLoadEnd(task: () -> Unit) {
        addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                task()
                client.removeLoadHandler(this, jbCefBrowser.cefBrowser)
            }
        })
    }

    /*
    * Loads a specific url in the browser.
    */
    override fun loadUrl(url: String) {
        if (isInitialized) {
            jbCefBrowser.cefBrowser.executeJavaScript("window.location.href = '$url';", null, 0)
            // Reloading clears the webclient's data, reinitialize exercise information
            val registry = project.service<OrionStudentExerciseRegistry>()
            if (registry.isArtemisExercise) {
                registry.exerciseInfo?.let {
                    onNextLoadEnd {
                        project.messageBus.syncPublisher(OrionIntellijStateNotifier.INTELLIJ_STATE_TOPIC)
                            .openedExercise(it.exerciseId, it.currentView)
                    }
                }
            }
        }
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

/**
 * Wrapper for [OrionBrowserNotifier.artemisLoadedWith]
 */
interface OrionBrowserNotifier {
    /**
     * Informs the JavascriptConnector that a browser has been loaded
     */
    fun artemisLoadedWith(engine: CefBrowser)

    companion object {
        @JvmField
        val ORION_BROWSER_TOPIC = Topic.create("Orion Browser Init", OrionBrowserNotifier::class.java)
    }
}

/**
 * Wrapper for [ArtemisWebappStatusNotifier.webappLoaded]
 */
interface ArtemisWebappStatusNotifier {
    /**
     * Informs the JavascriptConnector that artemis has been loaded to trigger running all queued javascript
     */
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
