package de.tum.www1.orion.util

import com.intellij.openapi.application.ApplicationManager
import java.util.concurrent.Callable
import java.util.concurrent.FutureTask

fun <T> invokeOnEDTAndWait(call: () -> T): T {
    val edtTask = FutureTask<T>(Callable { call() })
    ApplicationManager.getApplication().invokeLater(edtTask)
    return edtTask.get()
}