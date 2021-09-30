package de.tum.www1.orion.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.ui.jcef.JBCefJSQuery
import de.tum.www1.orion.enumeration.ProgrammingLanguage
import de.tum.www1.orion.settings.OrionBundle
import de.tum.www1.orion.ui.OrionRouter
import de.tum.www1.orion.ui.browser.IBrowser
import org.cef.browser.CefMessageRouter
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

/**
 * Shortcut for service<serviceClass> for java.
 *
 * @return the service or null if the loading failed
 */
fun <T> appService(serviceClass: Class<T>): T = ApplicationManager.getApplication().getService(serviceClass)

/**
 * Looks up the given key in the translation files at resources/i18n at the current locale
 *
 * @param key to look up in the translation files
 * @return the text for the given key in the current language
 */
fun translate(key: String): String = OrionBundle.message(key)

/**
 * Determines the programming language based on the set sdk (NOT based on the exercise data).
 * Currently only supports java
 *
 * @return the project's programming language based on sdk
 */
fun Project.selectedProgrammingLanguage(): ProgrammingLanguage? {
    return ProjectRootManager.getInstance(this).projectSdk?.sdkType?.name?.let {
        when (it) {
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

/**
 * Makes the browser return to the default url as defined by [OrionRouter.routeForCurrentExerciseOrDefault]
 *
 * @param project the browser belongs to
 */
fun returnToExercise(project: Project) {
    val url = project.service<OrionRouter>().routeForCurrentExerciseOrDefault()
    project.service<IBrowser>().loadUrl(url)
}
