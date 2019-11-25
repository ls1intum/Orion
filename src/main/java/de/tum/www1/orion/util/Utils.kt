package de.tum.www1.orion.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import de.tum.www1.orion.enumeration.ProgrammingLanguage
import de.tum.www1.orion.util.settings.OrionBundle
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.FutureTask

/**
 * Invokes the given function on the EDT and waits for its execution. Especially useful if you want to read, or update
 * files in the local file system and have to wait for the result.
 *
 * @return The result of the given function
 */
inline fun <T> invokeOnEDTAndWait(crossinline call: () -> T): T {
    val edtTask = FutureTask<T>(Callable { call() })
    ApplicationManager.getApplication().invokeLater(edtTask)
    return edtTask.get()
}

fun <T> Project.service(service: Class<T>) = ServiceManager.getService(this, service)

fun <T> appService(serviceClass: Class<T>) = ServiceManager.getService(serviceClass)

fun translate(key: String) = OrionBundle.message(key)

fun Project.selectedProgrammingLangauge(): ProgrammingLanguage {
    return this.service(ProjectRootManager::class.java).projectSdk?.sdkType?.name?.let {
        when (this.service(ProjectRootManager::class.java).projectSdk?.sdkType?.name) {
            "JavaSDK" -> ProgrammingLanguage.JAVA
            else -> throw IllegalArgumentException("Unsupported SDK: " + it)
        }
    } ?: throw IllegalStateException("No SDK selected!")
}

object PropertiesUtil {
    fun readProperties(file: String): Properties {
        val loader = javaClass.classLoader
        val properties = Properties()
        properties.load(loader.getResourceAsStream(file))

        return properties
    }
}

object OrionFileUtils {
    fun getRoot(project: Project): VirtualFile? {
        if (project.basePath != null) {
            val lfs = LocalFileSystem.getInstance()
            return lfs.findFileByPath(project.basePath!!)
        }

        return null
    }
}

// Helper for Java
fun ktLambda(runnable: Runnable): () -> Unit = runnable::run