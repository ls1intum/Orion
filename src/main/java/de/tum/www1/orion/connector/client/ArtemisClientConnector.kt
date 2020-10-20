package de.tum.www1.orion.connector.client

import com.intellij.openapi.project.Project
import de.tum.www1.orion.enumeration.ExerciseView
import de.tum.www1.orion.messaging.OrionIntellijStateNotifier
import de.tum.www1.orion.messaging.OrionIntellijStateNotifier.INTELLIJ_STATE_TOPIC
import de.tum.www1.orion.ui.browser.OrionBrowserNotifier
import de.tum.www1.orion.ui.browser.OrionBrowserNotifier.Companion.ORION_BROWSER_TOPIC
import de.tum.www1.orion.util.runOnEdt
import org.cef.browser.CefBrowser
import java.util.*

class ArtemisClientConnector(private val project: Project) : JavaScriptConnector {
    private var artemisLoaded = false
    private var browser: CefBrowser? = null
    private val dispatchQueue: Queue<Runnable> = LinkedList()

    /**
     * Notifies the JavaScript connector, that all web content has been loaded. This is used to trigger all remaining
     * calls to the web client, which were queued because Artemis has not fully been loaded, yet.
     *
     * @param engine The web engine used for loading the Artemis webapp.
     */
    init {
        project.messageBus.connect().subscribe(ORION_BROWSER_TOPIC, object : OrionBrowserNotifier {
            override fun artemisLoadedWith(engine: CefBrowser) {
                artemisLoaded = true
                this@ArtemisClientConnector.browser = engine
                dispatchQueue.forEach { runOnEdt { it } }
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

    private fun runAfterLoaded(task: Runnable) {
        if (!artemisLoaded)
            dispatchQueue.add(task)
        else
            runOnEdt { task }
    }

    private fun executeJSFunction(function: JavaScriptFunction, vararg args: Any) {
        runAfterLoaded { browser.also {
            if (it != null) {
                function.execute(it, *args)
            }
            TODO("Implement some handler for this corner case")
        } }
    }
}
