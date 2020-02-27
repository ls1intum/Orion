package de.tum.www1.orion.connector.client

import com.intellij.openapi.project.Project
import de.tum.www1.orion.connector.client.JavaScriptConnector.JavaScriptFunction.*
import de.tum.www1.orion.enumeration.ExerciseView
import de.tum.www1.orion.messaging.OrionIntellijStateNotifier
import de.tum.www1.orion.messaging.OrionIntellijStateNotifier.INTELLIJ_STATE_TOPIC
import de.tum.www1.orion.ui.browser.BrowserWebView
import de.tum.www1.orion.ui.browser.BrowserWebView.OrionBrowserNotifier.ORION_BROWSER_TOPIC
import javafx.application.Platform
import javafx.scene.web.WebEngine
import java.util.*

class ArtemisClientConnector(private val project: Project) : JavaScriptConnector {
    private var artemisLoaded = false
    private var webEngine: WebEngine? = null
    private val dispatchQueue: Queue<Runnable> = LinkedList<Runnable>()

    init {
        project.messageBus.connect().subscribe(ORION_BROWSER_TOPIC, BrowserWebView.OrionBrowserNotifier { artemisLoadedWith(it) })
    }

    /**
     * Notifies the JavaScript connector, that all web content has been loaded. This is used to trigger all remaining
     * calls to the web client, which were queued because Artemis has not fully been loaded, yet.
     *
     * @param engine The web engine used for loading the Artemis webapp.
     */
    private fun artemisLoadedWith(engine: WebEngine?) {
        artemisLoaded = true
        webEngine = engine
        dispatchQueue.forEach { Platform.runLater(it) }
    }

    override fun initIDEStateListeners() {
        project.messageBus.connect().subscribe(INTELLIJ_STATE_TOPIC, object : OrionIntellijStateNotifier {
            override fun openedExercise(opened: Long, currentView: ExerciseView) {
                executeJSFunction(ON_EXERCISE_OPENED, opened, currentView)
            }

            override fun startedBuild(courseId: Long, exerciseId: Long) {
                executeJSFunction(TRIGGER_BUILD_FROM_IDE, courseId, exerciseId)
            }

            override fun isCloning(cloning: Boolean) {
                executeJSFunction(IS_CLONING, cloning)
            }

            override fun isBuilding(building: Boolean) {
                executeJSFunction(IS_BUILDING, building)
            }

        })
    }

    private fun runAfterLoaded(task: Runnable) {
        if (!artemisLoaded) {
            dispatchQueue.add(task)
        } else {
            Platform.runLater(task)
        }
    }

    private fun executeJSFunction(function: JavaScriptConnector.JavaScriptFunction, vararg args: Any) {
        runAfterLoaded(Runnable { webEngine.also { function.execute(it, *args) } })
    }
}
