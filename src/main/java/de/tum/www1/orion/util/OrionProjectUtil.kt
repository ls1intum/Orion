package de.tum.www1.orion.util

import com.intellij.execution.RunManager
import com.intellij.ide.impl.OpenProjectTask
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleTypeId
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
import com.intellij.openapi.util.io.FileUtil
import com.intellij.testFramework.runInEdtAndGet
import de.tum.www1.orion.build.OrionLocalRunConfigurationSettingsFactory
import org.jetbrains.annotations.SystemIndependent
import java.io.File
import java.nio.file.Paths

/**
 * Helper class providing methods to create new projects or modules
 */
object OrionProjectUtil {
    enum class ModuleType { GRADLE_MODULE, MAVEN_MODULE }


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
        val projectTask = OpenProjectTask()
        val newProject = projectManager.newProject(realPath, projectTask.withProjectName(name))
        newProject?.save()

        return newProject
    }

    @Deprecated("Deprecated newModule function")
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

    /**
     * Creates a new gradle module
     * @param project the current project
     * @param name the current name
     */
    fun newModule(project: Project, name: String, moduleType: ModuleType) {

        val modulePath = "${project.basePath}${File.separatorChar}${name}${File.separatorChar}" + when (moduleType) {
            ModuleType.MAVEN_MODULE -> "pom.xml"
            ModuleType.GRADLE_MODULE -> "build.gradle"
            else -> ""
        }

        val moduleManager = ModuleManager.getInstance(project)
        val runConfiguration = OrionLocalRunConfigurationSettingsFactory.runConfigurationForLocalTesting(project)
        if (runConfiguration != null) {
            runConfiguration.storeInDotIdeaFolder()
            RunManager.getInstance(project).addConfiguration(runConfiguration)
        }
        val moduleName = when (moduleType) {
            ModuleType.GRADLE_MODULE -> "Gradle Module"
            ModuleType.MAVEN_MODULE -> "Maven Module"
        }

        runInEdt {
            runWriteAction {
                moduleManager.newModule(modulePath, moduleName)
            }
            project.save()
        }
    }
}
