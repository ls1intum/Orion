package de.tum.www1.orion.util

import com.intellij.openapi.components.ServiceManager
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
    val privatePropertyField=this.javaClass.getDeclaredField(propertyName).apply {
        isAccessible=true
    }
    return privatePropertyField.get(this) as E
}

val JBCefJSQuery.cefRouter : CefMessageRouter
    get(){
        val innerClasses=this.javaClass.declaredClasses
        val jsQueryFuncClass=innerClasses.find { innerClass -> innerClass.simpleName.contains("JSQueryFunc") }
        val myRouterField= jsQueryFuncClass!!.getDeclaredField("myRouter").apply {
            isAccessible=true
        }
        return myRouterField.get(this.getPrivateProperty("myFunc")) as CefMessageRouter
    }

fun <T> appService(serviceClass: Class<T>): T = ServiceManager.getService(serviceClass)

fun translate(key: String): String = OrionBundle.message(key)

fun Project.selectedProgrammingLangauge(): ProgrammingLanguage? {
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

// Helper for Java
fun ktLambda(runnable: Runnable): () -> Unit = runnable::run
