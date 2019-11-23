package de.tum.www1.orion.util

import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleTypeId
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
import com.intellij.openapi.util.io.FileUtil
import com.intellij.testFramework.runInEdtAndGet
import org.jetbrains.annotations.SystemIndependent
import java.io.File

object OrionProjectUtil {
    fun newEmptyProject(name: String, path: @SystemIndependent String): Project? {
        val projectManager = ProjectManagerEx.getInstanceEx()

        val newProject = projectManager.newProject(name, path, false, false)
        newProject?.save()

        return newProject
    }

    fun newModule(project: Project, name: String): Module {
        val modulePath = project.basePath + File.separatorChar + name
        FileUtil.ensureExists(File(modulePath))
        val moduleFilePath = modulePath + File.separatorChar + "$name.iml"
        val moduleFile = File(moduleFilePath)
        FileUtil.createIfNotExists(moduleFile)

        val moduleManager = ModuleManager.getInstance(project)
        return runInEdtAndGet {
            runWriteAction {
                val module = moduleManager.newModule(moduleFilePath, ModuleTypeId.JAVA_MODULE)
                project.save()
                module
            }
        }
    }
}