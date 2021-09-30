package de.tum.www1.orion.ui.browser

import org.cef.handler.CefLoadHandler
import org.cef.handler.CefMessageRouterHandler
import javax.swing.JComponent

/**
 * Interface for [BrowserService]
 */
interface IBrowser {
    val uiComponent: JComponent
    val isInitialized: Boolean

    /**
     * Used to add handlers that transfer Javascript requests to Kotlin functions
     *
     * @param handler handler which is called when one of the connector functions was called
     */
    fun addJavaHandler(handler: CefMessageRouterHandler)

    /**
     * Used mainly to add Javascript handler to the code so that the page can properly communicate to the Java handlers.
     * @param handler handler which is called when a page has finished loading and the DOM is available
     */
    fun addLoadHandler(handler: CefLoadHandler)

    /**
     * Initializes the user-agent, setting up the JCEF objects and CefSettings
     */
    fun init()

    /**
     * Makes the browser load the given url
     *
     * @param url to load
     */
    fun loadUrl(url: String)
}
