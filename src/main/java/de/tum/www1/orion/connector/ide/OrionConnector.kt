package de.tum.www1.orion.connector.ide

import com.intellij.ui.jcef.JBCefJSQuery
import de.tum.www1.orion.ui.browser.IBrowser

/**
 * Represent a class which is responsible for initializing JS to Java communication
 *
 * When a Java handler is added to a CefBrowser instance (via JBCEFJSQuery wrapper in this case, JCEF will inject into
 * the window object of the loaded page's Javascript a cefQuery function. Message to Java can be conducted by invoking
 * in JS this function and passing the message. If there are multiple handlers attached, a random handler will receive
 * the message. Propagation of message to other handlers is done by setting the return value of onQuery to false. To
 * make sure that the message is dispatched to the right handler, we use message of the form:
      "handlerName \n param1 \n param2 \n param3 \n ...
 * See https://appdoc.app/artifact/org.bitbucket.johness/java-cef/49.87.win32.2/index.html?org/cef/handler/CefRequestHandler.html
 * for more information.
 *
 * @property connectorName the name of the javascript object which will be added into the window object so that user
 * interaction in the webpage is linked to Javascript handler, which then calls the corresponding Java handler via cefQuery
 */
abstract class OrionConnector{
    protected val connectorName: String

    init {
        // Most performant way to set the first letter to lowercase according to
        // https://stackoverflow.com/questions/4052840/most-efficient-way-to-make-the-first-character-of-a-string-lower-case
        val classNameChars = this.javaClass.simpleName.toCharArray()
        classNameChars[0] = Character.toLowerCase(classNameChars[0])
        connectorName = String(classNameChars)
    }

    /**
     * Register the Java handler and connect it to the associated Javascript object.
     *
     * @param queryInjector an injector which produces a cefQuery string of the right form to add into JS string
     */
    abstract fun initializeHandlers(browser: IBrowser, queryInjector: JBCefJSQuery)
}