package de.tum.www1.orion.util

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.jcef.JBCefJSQuery
import de.tum.www1.orion.enumeration.ProgrammingLanguage
import de.tum.www1.orion.settings.OrionBundle
import org.cef.browser.CefMessageRouter
import org.jetbrains.annotations.SystemIndependent
import org.jetbrains.concurrency.runAsync
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException

/**
 * Get private property of an object via reflection
 */
inline fun <reified E> Any.getPrivateProperty(propertyName: String): E {
    val privatePropertyField = this.javaClass.getDeclaredField(propertyName).apply {
        isAccessible = true
    }
    return privatePropertyField.get(this) as E
}

val JBCefJSQuery.cefRouter: CefMessageRouter
    get() {
        val innerClasses = this.javaClass.declaredClasses
        val jsQueryFuncClass = innerClasses.find { innerClass -> innerClass.simpleName.contains("JSQueryFunc") }
        val myRouterField = jsQueryFuncClass!!.getDeclaredField("myRouter").apply {
            isAccessible = true
        }
        return myRouterField.get(this.getPrivateProperty("myFunc")) as CefMessageRouter
    }

fun <T> appService(serviceClass: Class<T>): T = ServiceManager.getService(serviceClass)

/**
 * Looks up the given key in the translation files at resources/i18n at the current locale
 *
 * @param key to look up in the translation files
 * @return the text for the given key in the current language
 */
fun translate(key: String): String = OrionBundle.message(key)

fun Project.selectedProgrammingLanguage(): ProgrammingLanguage? {
    return this.getComponent(ProjectRootManager::class.java).projectSdk?.sdkType?.name?.let {
        when (this.getComponent(ProjectRootManager::class.java).projectSdk?.sdkType?.name) {
            "JavaSDK" -> ProgrammingLanguage.JAVA
            else -> null
        }
    }
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

    fun systemIndependentPathOf(basePath: @SystemIndependent String, vararg pathComponents: String): String {
        return basePath + "/" + pathComponents.joinToString("/")
    }
}

fun <T> runAndWaitWithTimeout(timeMillis: Int, block: () -> T): T? {
    val future = runAsync { block() }
    return try {
        future.blockingGet(timeMillis)
    } catch (e: TimeoutException) {
        null
    } catch (e: ExecutionException) {
        null
    }
}

fun Scanner.nextAll(): String {
    this.useDelimiter("\\A")
    return if (this.hasNext()) this.next() else ""
}

// Helper for Java
fun ktLambda(runnable: Runnable): () -> Unit = runnable::run

/**
 * Runs the given task in the background while showing a non-progressing progress bar
 *
 * @param project project to open the modal on
 * @param descriptionKey key for the translation to show in the modal
 * @param task task to execute in the background while showing the modal
 */
fun runWithIndeterminateProgressModal(project: Project, descriptionKey: String, task: Runnable) {
    ProgressManager.getInstance().run(object :
        Task.Modal(project, translate(descriptionKey), false) {
        override fun run(indicator: ProgressIndicator) {
            indicator.isIndeterminate = true
            task.run()
        }
    })
}