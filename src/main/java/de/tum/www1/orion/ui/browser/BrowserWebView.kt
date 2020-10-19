package de.tum.www1.orion.ui.browser

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.util.messages.Topic
import de.tum.www1.orion.settings.OrionSettingsProvider
import de.tum.www1.orion.ui.OrionRouter
import org.cef.CefApp
import org.cef.CefSettings
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandler
import org.cef.network.CefRequest
import java.util.*

class BrowserWebView(val project: Project) {
    val browser: JBCefBrowser

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
        val route = ServiceManager.getService(project, OrionRouter::class.java).routeForCurrentExercise()
        //Since JBCef wrapper doesn't support setting user-agent, we need to use reflection to access private properties.
        val jbCefAppInstance= JBCefApp.getInstance()
        val privateCefApp=jbCefAppInstance.getPrivateProperty<CefApp>("myCefApp")
        val privateCefSettings=privateCefApp.getPrivateProperty<CefSettings>("settings_")
        //The user agent needes to set before the call to createClient and can not be changed after
        privateCefSettings.user_agent=userAgent
        val jbCefClient=jbCefAppInstance.createClient()
        browser=JBCefBrowser(jbCefClient, route)
        //Inject js bridge
        jbCefClient.addLoadHandler(object : CefLoadHandler {
            override fun onLoadEnd(p0: CefBrowser?, p1: CefFrame?, p2: Int) {
                project.messageBus.syncPublisher(OrionBrowserNotifier.ORION_BROWSER_TOPIC).artemisLoadedWith(browser)
            }

            override fun onLoadingStateChange(p0: CefBrowser?, p1: Boolean, p2: Boolean, p3: Boolean) {
                //not needed
            }

            override fun onLoadStart(p0: CefBrowser?, p1: CefFrame?, p2: CefRequest.TransitionType?) {
                //not needed
            }

            override fun onLoadError(p0: CefBrowser?, p1: CefFrame?, p2: CefLoadHandler.ErrorCode?, p3: String?, p4: String?) {
                //not needed
            }
        }, browser.cefBrowser)
    }
}

inline fun <reified E> Any.getPrivateProperty(propertyName: String): E {
    val privatePropertyField=this.javaClass.getDeclaredField(propertyName).apply {
        isAccessible=true
    }
    return privatePropertyField.get(this) as E
}

interface OrionBrowserNotifier {
    fun artemisLoadedWith(engine: JBCefBrowser)

    companion object {
        @JvmField
        val ORION_BROWSER_TOPIC = Topic.create("Orion Browser Init", OrionBrowserNotifier::class.java)
    }
}