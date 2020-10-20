package de.tum.www1.orion.connector.client

import com.jetbrains.rd.util.printlnError
import de.tum.www1.orion.enumeration.ExerciseView
import java.util.*
import java.util.stream.Collectors
import kotlin.reflect.KClass

interface JavaScriptConnector {
    /**
     * Initializes all listeners to the internal IDE/Orion states, that should get propagated to the client. Inlcuding
     * e.g. ongoing submits, clone processes, commits, etc.
     */
    fun initIDEStateListeners()
}

enum class JavaScriptFunction(private val functionName: String, vararg argTypes: KClass<*>) {
    ON_EXERCISE_OPENED("onExerciseOpened", Long::class, ExerciseView::class),
    IS_CLONING("isCloning", Boolean::class),
    IS_BUILDING("isBuilding", Boolean::class),
    TRIGGER_BUILD_FROM_IDE("startedBuildInOrion", Long::class, Long::class);

    private val argTypes: List<KClass<*>> = listOf(*argTypes)
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

    fun executeString(vararg args: Any) : String {
        require(areArgumentsValid(*args)) {
            "JS function $functionName called with the wrong argument types!".also { printlnError(it) }
        }
        val params = Arrays.stream(args)
                .map { arg: Any ->
                    if (arg::class == String::class || arg::class.isFinal) {
                        return@map "'$arg'"
                    }
                    arg.toString()
                }
                .collect(Collectors.joining(",", "(", ")"))
        //The third argument, line, is the base line number used for error reporting, doesn't matter much
        return ARTEMIS_CLIENT_CONNECTOR + functionName + params
    }

    companion object {
        private const val ARTEMIS_CLIENT_CONNECTOR = "window.artemisClientConnector."
    }

}