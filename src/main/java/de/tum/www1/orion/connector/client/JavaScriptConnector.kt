package de.tum.www1.orion.connector.client

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import de.tum.www1.orion.enumeration.ExerciseView
import org.cef.browser.CefBrowser
import java.util.*
import java.util.stream.Collectors

interface JavaScriptConnector {
    /**
     * Initializes all listeners to the internal IDE/Orion states, that should get propagated to the client. Inlcuding
     * e.g. ongoing submits, clone processes, commits, etc.
     */
    fun initIDEStateListeners()

    companion object {
        fun getInstance(project: Project): JavaScriptConnector? {
            return ServiceManager.getService(project, JavaScriptConnector::class.java)
        }
    }
}

enum class JavaScriptFunction(private val functionName: String, vararg argTypes: Class<*>) {
    ON_EXERCISE_OPENED("onExerciseOpened", Long::class.java, ExerciseView::class.java),
    IS_CLONING("isCloning", Boolean::class.java),
    IS_BUILDING("isBuilding", Boolean::class.java),
    TRIGGER_BUILD_FROM_IDE("startedBuildInOrion", Long::class.java, Long::class.java);

    private val argTypes: List<Class<*>> = listOf(*argTypes)
    private fun areArgumentsValid(vararg args: Any): Boolean {
        if (args.size != argTypes.size) {
            return false
        }
        for (i in args.indices) {
            if (args[i].javaClass != argTypes[i]) {
                return false
            }
        }
        return true
    }

    fun execute(engine: CefBrowser, vararg args: Any) {
        require(areArgumentsValid(*args)) { "JS function $functionName called with the wrong argument types!" }
        val params = Arrays.stream(args)
                .map { arg: Any ->
                    if (arg.javaClass == String::class.java || arg.javaClass.isEnum) {
                        return@map "'$arg'"
                    }
                    arg.toString()
                }
                .collect(Collectors.joining(",", "(", ")"))
        //The third argument, line, is the base line number used for error reporting, doesn't matter much
        engine.executeJavaScript(ARTEMIS_CLIENT_CONNECTOR + functionName + params, engine.url, 0)
    }

    companion object {
        private const val ARTEMIS_CLIENT_CONNECTOR = "window.artemisClientConnector."
    }

}