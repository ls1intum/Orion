package de.tum.www1.orion.util

import com.intellij.ide.impl.OpenProjectTask
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
import java.nio.file.Paths

/**
 * Helper class providing methods to create new projects or modules
 */
object OrionProjectUtil {
    /**
     * Creates an empty project with the given name at the given path
     *
     * @param name of the project
     * @param path of the project
     * @return the created project or null if the creation failed
     */
    fun newEmptyProject(name: String, path: @SystemIndependent String): Project? {
        val projectManager = ProjectManagerEx.getInstanceEx()
        val realPath = Paths.get(FileUtil.toSystemDependentName(path))
        val projectTask = OpenProjectTask().withProjectName(name);

        val newProject = projectManager.newProject(realPath, projectTask)
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
