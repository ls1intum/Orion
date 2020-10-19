package de.tum.www1.orion.connector.ide

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.ui.jcef.JBCefBrowser
import de.tum.www1.orion.ui.browser.OrionBrowserNotifier

abstract class OrionConnector : StartupActivity {
    protected lateinit var project: Project

    override fun runActivity(project: Project) {
        this.project=project
        // Most performant way to set the first letter to lowercase according to
        // https://stackoverflow.com/questions/4052840/most-efficient-way-to-make-the-first-character-of-a-string-lower-case
        val classNameChars = this.javaClass.simpleName.toCharArray()
        classNameChars[0] = Character.toLowerCase(classNameChars[0])
        val connectorName = String(classNameChars)
        project.messageBus.connect().subscribe(OrionBrowserNotifier.ORION_BROWSER_TOPIC, object : OrionBrowserNotifier {
            override fun artemisLoadedWith(engine: JBCefBrowser) {
                engine.cefBrowser.executeJavaScript("window.$connectorName", engine.cefBrowser.url, 0)
            }
        })
    }
}