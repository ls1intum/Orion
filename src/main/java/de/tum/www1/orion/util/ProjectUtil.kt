package de.tum.www1.orion.util

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleTypeId
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
import com.intellij.openapi.util.io.FileUtil
import java.io.File

class ProjectUtil {
    companion object {
        fun newEmptyProject(courseId: Long, exerciseId: Long, exerciseName: String): Project? {
            val projectManager = ProjectManagerEx.getInstanceEx()
            val exercisePath = setupExerciseDirPath(courseId, exerciseId, exerciseName)
            val exercisePathFile = File(exercisePath)
            FileUtil.ensureExists(exercisePathFile)

            val newProject = projectManager.newProject(exerciseName, exercisePath, false, false)
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
            return moduleManager.newModule(moduleFilePath, ModuleTypeId.JAVA_MODULE)
        }
    }
}