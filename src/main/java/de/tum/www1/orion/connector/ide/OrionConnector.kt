package de.tum.www1.orion.connector.ide

import com.intellij.ui.jcef.JBCefJSQuery
import de.tum.www1.orion.ui.browser.IBrowser
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefQueryCallback
import org.cef.handler.CefLoadHandlerAdapter
import org.cef.handler.CefMessageRouterHandlerAdapter
import java.util.*

/**
 * Represent a class which is responsible for initializing JS to Java communication
 *
 * When a Java handler is added to a CefBrowser instance (via JBCEFJSQuery wrapper in this case), JCEF will inject into
 * the window object of the loaded page's Javascript a cefQuery function. Message to Java can be conducted by invoking
 * in JS this function and passing the message. If there are multiple handlers attached, a random handler will receive
 * the message. Propagation of message to other handlers is done by setting the return value of onQuery to false. To
 * make sure that the message is dispatched to the right handler, we use message of the form:
 * "handlerName \n param1 \n param2 \n param3 \n ..."
 * See https://appdoc.app/artifact/org.bitbucket.johness/java-cef/49.87.win32.2/index.html?org/cef/handler/CefRequestHandler.html
 * for more information.
 *
 * @property connectorName the name of the javascript object which will be added into the window object so that user
 * interaction in the webpage is linked to Javascript handler, which then calls the corresponding Java handler via cefQuery
 */
abstract class OrionConnector {
    private val connectorName: String

    init {
        // Most performant way to set the first letter to lowercase according to
        // https://stackoverflow.com/questions/4052840/most-efficient-way-to-make-the-first-character-of-a-string-lower-case
        val nameChars = this.javaClass.simpleName.toCharArray()
        nameChars[0] = Character.toLowerCase(nameChars[0])
        connectorName = String(nameChars)
    }

    /**
     * Register the Java handler and connect it to the associated Javascript object.
     *
     * @param queryInjector an injector which produces a cefQuery string of the right form to add into JS string
     */
    abstract fun initializeHandlers(browser: IBrowser, queryInjector: JBCefJSQuery)

    /**
     * Adds a handler connecting the javascript with kotlin. If a function is called with the name present
     * in the map, the associated kotlin action is called with a scanner to allow the function to read
     * further parameters
     *
     * @param browser the browser to add the handler to
     * @param reactions mapping all functions to the kotlin function called if it is invoked
     */
    protected fun addJavaHandler(browser: IBrowser, reactions: Map<String, (Scanner) -> Unit>) {
        browser.addJavaHandler(object : CefMessageRouterHandlerAdapter() {
            override fun onQuery(
                browser: CefBrowser?,
                frame: CefFrame?,
                queryId: Long,
                request: String?,
                persistent: Boolean,
                callback: CefQueryCallback?
            ): Boolean {
                request ?: return false
                val scanner = Scanner(request)
                val methodName = scanner.nextLine()
                val reaction = reactions[methodName] ?: return false
                reaction.invoke(scanner)
                return true
            }
        })
    }

    /**
     * Generates the javascript half of the connectors and adds it to the browser
     *
     * @param browser the browser to inject the js into
     * @param queryInjector the injector from initializeHandlers
     * @param parameterNames mapping the function names to their parameter names
     */
    protected fun addLoadHandler(
        browser: IBrowser,
        queryInjector: JBCefJSQuery,
        parameterNames: Map<String, List<String>>
    ) {
        // @formatter:off
        /*
         * Javascript looks as follows:
         *   window.connector {
         *     function1: function(parameter1, parameter2) {
         *       $queryInjector.inject('function1' + '\n' + 'parameter1' + '\n' + 'parameter2')
         *     },
         *     function2 ...
         *   }
         *
         * Make sure formatter control is activated before auto-formatting this file
         */
        val javaScript = """
            window.$connectorName={
                ${parameterNames.map {
                    entry ->
                        """${entry.key}: function(${entry.value.joinToString(", ")}) {
                            ${queryInjector.inject("""
                                '${entry.key}' ${entry.value.joinToString("") {
                                    parameter -> " + '\\n' + $parameter"
                                }}""".trimIndent())}
                        }"""
                }.joinToString(",\n")}
            };
        """.trimIndent()
        // @formatter:on
        browser.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                browser?.executeJavaScript(javaScript, browser.url, 0)
            }
        })
    }
}
