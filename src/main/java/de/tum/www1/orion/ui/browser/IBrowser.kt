package de.tum.www1.orion.ui.browser

import org.cef.handler.CefLoadHandler
import org.cef.handler.CefMessageRouterHandler
import javax.swing.JComponent

interface IBrowser {
    val uiComponent: JComponent
    fun addJavaHandler(handler: CefMessageRouterHandler)

    /**
     * Used mainly to add Javascript handler to the code so that the page can properly communicate to the Java handlers.
     * @param handler A handler which is called when a page has finished loading and the DOM is available
     */
    fun addLoadHandler(handler: CefLoadHandler)
}