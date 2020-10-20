package de.tum.www1.orion.connector.client

import com.intellij.openapi.project.Project
import de.tum.www1.orion.enumeration.ExerciseView
import de.tum.www1.orion.messaging.OrionIntellijStateNotifier
import de.tum.www1.orion.messaging.OrionIntellijStateNotifier.INTELLIJ_STATE_TOPIC
import de.tum.www1.orion.ui.browser.OrionBrowserNotifier
import de.tum.www1.orion.ui.browser.OrionBrowserNotifier.Companion.ORION_BROWSER_TOPIC
import org.cef.browser.CefBrowser
import java.util.*

class ArtemisClientConnector(private val project: Project) : JavaScriptConnector {
    private var browser: CefBrowser? = null
    private val dispatchQueue: Queue<String> = LinkedList()

    /**
     * Notifies the JavaScript connector, that all web content has been loaded. This is used to trigger all remaining
     * calls to the web client, which were queued because Artemis has not fully been loaded, yet.
     *
     * @param engine The web engine used for loading the Artemis webapp.
     */
    init {
        project.messageBus.connect().subscribe(ORION_BROWSER_TOPIC, object : OrionBrowserNotifier {
            override fun artemisLoadedWith(engine: CefBrowser) {
                this@ArtemisClientConnector.browser = engine
                for (task in dispatchQueue) {
                    browser?.executeJavaScript(task, browser?.url, 0) ?: return //null means artemis not been loaded
                }
            }
        })
    }

    override fun initIDEStateListeners() {
        project.messageBus.connect().subscribe(INTELLIJ_STATE_TOPIC, object : OrionIntellijStateNotifier {
            override fun openedExercise(opened: Long, currentView: ExerciseView) {
                executeJSFunction(JavaScriptFunction.ON_EXERCISE_OPENED, opened, currentView)
            }

            override fun startedBuild(courseId: Long, exerciseId: Long) {
                executeJSFunction(JavaScriptFunction.TRIGGER_BUILD_FROM_IDE, courseId, exerciseId)
            }

            override fun isCloning(cloning: Boolean) {
                executeJSFunction(JavaScriptFunction.IS_CLONING, cloning)
            }

            override fun isBuilding(building: Boolean) {
                executeJSFunction(JavaScriptFunction.IS_BUILDING, building)
            }

        })
    }

    private fun executeJSFunction(function: JavaScriptFunction, vararg args: Any) {
        val executeString=function.executeString(*args)
        dispatchQueue.add(executeString)
        for (task in dispatchQueue) {
            browser?.executeJavaScript(task, browser?.url, 0) ?: return //null means artemis not been loaded
        }
    }
}
