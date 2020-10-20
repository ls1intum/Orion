package de.tum.www1.orion.ui.browser

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefClient
import com.intellij.ui.jcef.JBCefJSQuery
import com.intellij.util.messages.Topic
import de.tum.www1.orion.connector.ide.build.OrionBuildConnector
import de.tum.www1.orion.connector.ide.exercise.OrionExerciseConnector
import de.tum.www1.orion.connector.ide.shared.OrionSharedUtilConnector
import de.tum.www1.orion.connector.ide.vcs.OrionVCSConnector
import de.tum.www1.orion.settings.OrionSettingsProvider
import de.tum.www1.orion.ui.OrionRouter
import org.cef.CefApp
import org.cef.CefSettings
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandlerAdapter
import java.util.*

class BrowserWebView(val project: Project) {
    val jbCefBrowser: JBCefBrowser
    val jsQuery: JBCefJSQuery
    val client: JBCefClient

    /**
     * Inits the actual browser panel. We use a JFXPanel in a [WebView] gets initialized. This web view only
     * displays the ArTEMiS Angular webapp containing a few adaptions, so that we only show the most important information
     * in the IDE.
     */
    init {
        //Dumb check but must be called to perform initialization
        if (!JBCefApp.isSupported()) {
            // Fallback to an alternative browser-less solution
        }
        val version = ResourceBundle.getBundle("de.tum.www1.orion.Orion").getString("version")
        val userAgent=ServiceManager.getService(OrionSettingsProvider::class.java).getSetting(OrionSettingsProvider.KEYS.USER_AGENT) + " Orion/" + version
        val route = project.service<OrionRouter>().routeForCurrentExercise()
        //Since JBCef wrapper doesn't support setting user-agent, we need to use reflection to access private properties.
        val jbCefAppInstance= JBCefApp.getInstance()
        val privateCefApp=jbCefAppInstance.getPrivateProperty<CefApp>("myCefApp")
        val privateCefSettings=privateCefApp.getPrivateProperty<CefSettings>("settings_")
        //The user agent needes to set before the call to createClient and can not be changed after
        privateCefSettings.user_agent=userAgent
        client=jbCefAppInstance.createClient()
        jbCefBrowser=JBCefBrowser(client, route)
        client.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                if(browser==null)
                    throw NullPointerException("Browser in onLoadEnd should not be null")
                else project.messageBus.syncPublisher(OrionBrowserNotifier.ORION_BROWSER_TOPIC).artemisLoadedWith(browser)
            }
        }, jbCefBrowser.cefBrowser)
        jsQuery = JBCefJSQuery.create(jbCefBrowser)
        injectJSBridge()
    }

    private fun injectJSBridge() {
        OrionSharedUtilConnector(this).initializeHandlers()
        OrionExerciseConnector(this).initializeHandlers()
        OrionBuildConnector(this).initializeHandlers()
        OrionVCSConnector(this).initializeHandlers()
    }
}

inline fun <reified E> Any.getPrivateProperty(propertyName: String): E {
    val privatePropertyField=this.javaClass.getDeclaredField(propertyName).apply {
        isAccessible=true
    }
    return privatePropertyField.get(this) as E
}

interface OrionBrowserNotifier {
    fun artemisLoadedWith(engine: CefBrowser)

    companion object {
        @JvmField
        val ORION_BROWSER_TOPIC = Topic.create("Orion Browser Init", OrionBrowserNotifier::class.java)
    }
}