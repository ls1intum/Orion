package de.tum.www1.orion.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import java.io.File
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

fun setupExerciseDirPath(courseId: Long, exerciseId: Long, exerciseName: String): String {
    val settings = ServiceManager.getService(OrionSettingsProvider::class.java)
    val artemisBaseDir = settings.getSetting(OrionSettingsProvider.KEYS.PROJECT_BASE_DIR)
    val pathToExercise = File("$artemisBaseDir/$courseId-$exerciseId-${exerciseName.replace(' ', '_')}")
    if (!pathToExercise.exists()) {
        pathToExercise.mkdirs()
    }
    return pathToExercise.absolutePath
}