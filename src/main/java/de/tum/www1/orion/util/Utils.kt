package de.tum.www1.orion.util

import com.intellij.openapi.application.ApplicationManager
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
import java.util.*
import java.util.concurrent.FutureTask

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

/**
 * Invokes the given function on the EDT and waits for its execution. Especially useful if you want to read, or update
 * files in the local file system and have to wait for the result.
 *
 * @return The result of the given function
 */
inline fun <T> runOnEdt(crossinline call: () -> T): T {
    val edtTask = FutureTask { call() }
    ApplicationManager.getApplication().invokeLater(edtTask)
    return edtTask.get()
}

fun <T> appService(serviceClass: Class<T>): T = ServiceManager.getService(serviceClass)

fun translate(key: String): String = OrionBundle.message(key)

fun Project.selectedProgrammingLangauge(): ProgrammingLanguage {
    return this.getComponent(ProjectRootManager::class.java).projectSdk?.sdkType?.name?.let {
        when (this.getComponent(ProjectRootManager::class.java).projectSdk?.sdkType?.name) {
            "JavaSDK" -> ProgrammingLanguage.JAVA
            else -> throw IllegalArgumentException("Unsupported SDK: $it")
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

    fun systemIndependentPathOf(basePath: @SystemIndependent String, vararg pathComponents: String): String {
        return basePath + "/" + pathComponents.joinToString("/")
    }
}

// Helper for Java
fun ktLambda(runnable: Runnable): () -> Unit = runnable::run
