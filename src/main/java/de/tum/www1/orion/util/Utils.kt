package de.tum.www1.orion.util

import com.intellij.openapi.application.ApplicationManager
import java.util.concurrent.Callable
import java.util.concurrent.FutureTask

/**
 * Invokes the given function on the EDT and waits for its execution. Especially useful if you want to read, or update
 * files in the local file system and have to wait for the result.
 *
 * @return The result of the given function
 */
fun <T> invokeOnEDTAndWait(call: () -> T): T {
    val edtTask = FutureTask<T>(Callable { call() })
    ApplicationManager.getApplication().invokeLater(edtTask)
    return edtTask.get()
}