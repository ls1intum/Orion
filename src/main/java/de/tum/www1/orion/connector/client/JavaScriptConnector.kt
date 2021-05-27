package de.tum.www1.orion.connector.client

import com.jetbrains.rd.util.printlnError
import de.tum.www1.orion.enumeration.ExerciseView
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * Interface of a class which will listen to the plugin's state and inform the browser about it, so that it displays the
 * UI correctly
 */
interface JavaScriptConnector {
    /**
     * Initializes all listeners to the internal IDE/Orion states, that should get propagated to the client. Including
     * e.g. ongoing submits, clone processes, commits, etc.
     */
    fun initIDEStateListeners()

    /**
     * All supported operations, mirrored by Artemis's Orion facade
     *
     * @property functionName name of the function that can be called
     * @param argTypes the associated parameter types, used to detect errors in the call
     */
    enum class JavaScriptFunction(private val functionName: String, vararg argTypes: KClass<*>) {
        ON_EXERCISE_OPENED("onExerciseOpened", Long::class, ExerciseView::class),
        IS_CLONING("isCloning", Boolean::class),
        IS_BUILDING("isBuilding", Boolean::class),
        TRIGGER_BUILD_FROM_IDE("startedBuildInOrion", Long::class, Long::class);

        private val argTypes: List<KClass<*>> = argTypes.asList()

        private fun areArgumentsValid(vararg args: Any): Boolean {
            if (args.size != argTypes.size) {
                return false
            }
            for (i in args.indices) {
                if (args[i]::class != argTypes[i]) {
                    return false
                }
            }
            return true
        }

        /**
         * @return a Javascript function call in text form: ex: connector.functionName(param1, param2)
         */
        fun executeString(vararg args: Any): String {
            require(areArgumentsValid(*args)) {
                "JS function $functionName called with the wrong argument types!".also { printlnError(it) }
            }
            val params = args.joinToString(",", "(", ")") { arg: Any ->
                if (arg::class == String::class || arg::class.isSubclassOf(Enum::class))
                    "'$arg'"
                else
                    arg.toString()
            }
            // The third argument, line, is the base line number used for error reporting, doesn't matter much
            return ARTEMIS_CLIENT_CONNECTOR + functionName + params
        }

        companion object {
            private const val ARTEMIS_CLIENT_CONNECTOR = "window.artemisClientConnector."
        }
    }
}