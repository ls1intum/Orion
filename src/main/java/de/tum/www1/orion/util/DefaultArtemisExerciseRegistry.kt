package de.tum.www1.orion.util

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.rd.util.remove
import java.io.File

class DefaultArtemisExerciseRegistry(private val project: Project) : ArtemisExerciseRegistry {
    override fun onNewExercise(courseId: Int, exerciseId: Int, exerciseName: String) {
        val name = exerciseName.replace(' ', '_')
        val exerciseDir = "$courseId-$exerciseId-$name"
        val properties = PropertiesComponent.getInstance()
        var pending = properties.getValues(PENDING)
        pending = pending?.takeIf { !it.contains(exerciseDir) }?.plus(exerciseDir) ?: arrayOf(exerciseDir)
        properties.setValues(PENDING, pending)
    }

    override fun registerPendingExercises() {
        val properties = PropertiesComponent.getInstance()
        val projectProperties = PropertiesComponent.getInstance(project)
        val currentDir = project.basePath!!.split('/').last()
        val pending = properties.getValues(PENDING)
        pending?.firstOrNull { it == currentDir }?.also {
            val dirAndName = currentDir.split('-')
            projectProperties.setValue(COURSE_ID, dirAndName[0].toInt(), -1)
            projectProperties.setValue(EXERCISE_ID, dirAndName[1].toInt(), -1)
            projectProperties.setValue(EXERCISE_NAME, dirAndName[2], null)
            projectProperties.setValues(PENDING, pending.remove(it))
        }
    }

    override fun isArtemisExercise(): Boolean {
        val alreadyLoaded = PropertiesComponent.getInstance(project).getInt(EXERCISE_ID, -1) != -1
        if (alreadyLoaded) {
            return true
        }

        return PropertiesComponent.getInstance().getValues(PENDING)?.let {
            val currentDir = project.basePath!!.split('/').last()
            return it.contains(currentDir)
        } ?: false
    }

    override fun getExerciseId(): Int = PropertiesComponent.getInstance(project).getInt(EXERCISE_ID, -1)

    override fun getExerciseName(): String? = PropertiesComponent.getInstance(project).getValue(EXERCISE_NAME)

    override fun getCourseId(): Int = PropertiesComponent.getInstance(project).getInt(COURSE_ID, -1)

    override fun alreadyImported(exerciseId: Int): Boolean {
        if (!isArtemisExercise || exerciseId != this.exerciseId) {
            val lfs = LocalFileSystem.getInstance()
            val basePath = ServiceManager.getService(ArtemisSettingsProvider::class.java).getSetting(ArtemisSettingsProvider.KEYS.PROJECT_BASE_DIR)
            val artemisHome = lfs.findFileByPath(basePath)
            VfsUtil.markDirtyAndRefresh(false, true, true, artemisHome)
            val projectDirs = lfs.refreshAndFindFileByPath(basePath)?.children
            return projectDirs?.any { p -> p.path.contains("-$exerciseId-") } ?: false
        }

        return true
    }

    companion object {
        private const val BASE_KEY = "de.tum.www1.orion.registry."
        private const val PENDING = BASE_KEY + "pending"
        private const val EXERCISE_ID = BASE_KEY + "exerciseId"
        private const val EXERCISE_NAME = BASE_KEY + "exerciseName"
        private const val COURSE_ID = BASE_KEY + "courseId"
    }
}
