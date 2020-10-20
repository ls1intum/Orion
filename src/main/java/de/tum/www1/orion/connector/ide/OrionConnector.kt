package de.tum.www1.orion.connector.ide

import de.tum.www1.orion.ui.browser.BrowserWebView

abstract class OrionConnector(browserWebView: BrowserWebView){
    protected val connectorName: String
    protected val browser=browserWebView.jbCefBrowser
    protected val client=browserWebView.client
    protected val jsQuery=browserWebView.jsQuery
    protected val project=browserWebView.project

    init {
        // Most performant way to set the first letter to lowercase according to
        // https://stackoverflow.com/questions/4052840/most-efficient-way-to-make-the-first-character-of-a-string-lower-case
        val classNameChars = this.javaClass.simpleName.toCharArray()
        classNameChars[0] = Character.toLowerCase(classNameChars[0])
        connectorName = String(classNameChars)
    }

    abstract fun initializeHandlers()
}