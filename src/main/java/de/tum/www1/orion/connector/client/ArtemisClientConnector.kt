package de.tum.www1.orion.connector.client

import com.intellij.openapi.project.Project
import de.tum.www1.orion.connector.client.JavaScriptConnector.JavaScriptFunction
import de.tum.www1.orion.enumeration.ExerciseView
import de.tum.www1.orion.messaging.OrionIntellijStateNotifier
import de.tum.www1.orion.messaging.OrionIntellijStateNotifier.INTELLIJ_STATE_TOPIC
import de.tum.www1.orion.ui.browser.ArtemisWebappStatusNotifier
import de.tum.www1.orion.ui.browser.ArtemisWebappStatusNotifier.Companion.ORION_SITE_LOADED_TOPIC
import de.tum.www1.orion.ui.browser.OrionBrowserNotifier
import de.tum.www1.orion.ui.browser.OrionBrowserNotifier.Companion.ORION_BROWSER_TOPIC
import org.cef.browser.CefBrowser
import java.util.concurrent.CopyOnWriteArrayList

class ArtemisClientConnector(private val project: Project) : JavaScriptConnector {
    private lateinit var browser: CefBrowser

    // Since this list may be access by multiple thread, CopyOnWriteArrayList is needed to avoid ConcurrentModificationException.
    private val dispatchQueue: MutableList<String> = CopyOnWriteArrayList()

    init {
        project.messageBus.connect().subscribe(ORION_BROWSER_TOPIC, object : OrionBrowserNotifier {
            /**
             * Notifies the JavaScript connector, that all web content has been loaded. This is used to trigger all remaining
             * calls to the web client, which were queued because Artemis has not fully been loaded, yet.
             *
             * @param engine The web engine / browser used for loading the Artemis webapp.
             */
            override fun artemisLoadedWith(engine: CefBrowser) {
                this@ArtemisClientConnector.browser = engine
            }
        })
        project.messageBus.connect().subscribe(ORION_SITE_LOADED_TOPIC, object : ArtemisWebappStatusNotifier {
            override fun webappLoaded() {
                dispatchQueue.apply {
                    forEach { dispatchJS(it) }
                    clear()
                }
            }
        })
    }

    private fun dispatchJS(task: String) = browser.executeJavaScript(task, browser.url, 0)

    override fun initIDEStateListeners() {
        project.messageBus.connect().subscribe(INTELLIJ_STATE_TOPIC, object : OrionIntellijStateNotifier {
            override fun openedExercise(opened: Long, currentView: ExerciseView) {
                Thread.sleep(500)
                executeJSFunction(JavaScriptFunction.ON_EXERCISE_OPENED, opened, currentView)
            }

            override fun startedBuild(courseId: Long, exerciseId: Long) {
                executeJSFunction(JavaScriptFunction.TRIGGER_BUILD_FROM_IDE, courseId, exerciseId)
            }

            override fun updateAssessment(submissionId: Long, feedback: String) {
                executeJSFunction(JavaScriptFunction.UPDATE_ASSESSMENT, submissionId, feedback)
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
        // doubly escape all backslashes to counter some strange escaping and unescaping done by javascript when calling the connector
        val executeString = function.executeString(*args).replace("\\", "\\\\")
        if (!::browser.isInitialized) {
            dispatchQueue.add(executeString)
            return
        }
        dispatchJS(executeString)
    }
}
