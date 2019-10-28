package de.tum.www1.orion.util

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.jetbrains.rd.util.remove
import de.tum.www1.orion.dto.ProgrammingExerciseDTO
import de.tum.www1.orion.enumeration.ExerciseView

// TODO can be even more refactored and generalized
abstract class DefaultOrionExerciseRegistry(protected val project: Project) : OrionExerciseRegistry {
    override fun registerPendingExercises() {
        val properties = PropertiesComponent.getInstance()
        val projectProperties = PropertiesComponent.getInstance(project)
        val currentDir = project.basePath!!.split('/').last()
        val pending = properties.getValues(PENDING)
        pending?.firstOrNull { it == currentDir }?.also {
            val dirAndName = currentDir.split('-')
            projectProperties.setValue(COURSE_ID, dirAndName[0], "-1")
            projectProperties.setValue(EXERCISE_ID, dirAndName[1], "-1")
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

    override fun getExerciseId(): Long = PropertiesComponent.getInstance(project).getValue(EXERCISE_ID, "-1").toLong()

    override fun getExerciseName(): String? = PropertiesComponent.getInstance(project).getValue(EXERCISE_NAME)

    override fun getCourseId(): Long = PropertiesComponent.getInstance(project).getValue(COURSE_ID, "-1").toLong()

    override fun alreadyImported(exerciseId: Long): Boolean {
        if (!isArtemisExercise || exerciseId != this.exerciseId) {
            val lfs = LocalFileSystem.getInstance()
            val basePath = ServiceManager.getService(OrionSettingsProvider::class.java).getSetting(OrionSettingsProvider.KEYS.PROJECT_BASE_DIR)
            val artemisHome = lfs.findFileByPath(basePath)
            VfsUtil.markDirtyAndRefresh(false, true, true, artemisHome)
            val projectDirs = lfs.refreshAndFindFileByPath(basePath)?.children
            return projectDirs?.any { p -> p.path.contains("-$exerciseId-") } ?: false
        }

        return true
    }

    protected companion object {
        const val BASE_KEY = "de.tum.www1.orion.registry."
        const val PENDING = BASE_KEY + "pending"
        const val EXERCISE_ID = BASE_KEY + "exerciseId"
        const val EXERCISE_NAME = BASE_KEY + "exerciseName"
        const val COURSE_ID = BASE_KEY + "courseId"
    }
}

class DefaultOrionStudentExerciseRegistry(project: Project) : DefaultOrionExerciseRegistry(project), OrionStudentExerciseRegistry {
    override fun onNewExercise(exercise: ProgrammingExerciseDTO) {
        onNewExercise(exercise.course.id, exercise.id, exercise.title)
    }

    override fun onNewExercise(courseId: Long, exerciseId: Long, exerciseName: String) {
        val exerciseDir = OrionFileUtils.getExerciseDirectory(courseId, exerciseId, exerciseName, ExerciseView.STUDENT)
        val properties = PropertiesComponent.getInstance()
        var pending = properties.getValues(PENDING)
        pending = pending?.takeIf { !it.contains(exerciseDir) }?.plus(exerciseDir) ?: arrayOf(exerciseDir)
        properties.setValues(PENDING, pending)
    }
}

class DefaultOrionInstructorExerciseRegistry(project: Project) : DefaultOrionExerciseRegistry(project), OrionInstructorExerciseRegistry {
    override fun registerPendingExercises() {
        super.registerPendingExercises()
        val currentDir = project.basePath!!.split('/').last()
        // If the currently opened exercise is opened in the instructor view, set the appropriate property
        if (currentDir.contains("_instructor")) {
            PropertiesComponent.getInstance(project).setValue(AS_INSTRUCTOR, true)
        }
    }

    override fun onNewExercise(exercise: ProgrammingExerciseDTO) {
        onNewExercise(exercise.course.id, exercise.id, exercise.title)
    }

    override fun onNewExercise(courseId: Long, exerciseId: Long, exerciseName: String) {
        val exerciseDir = OrionFileUtils.getExerciseDirectory(courseId, exerciseId, exerciseName, ExerciseView.INSTRUCTOR)
        val properties = PropertiesComponent.getInstance()
        var pending = properties.getValues(PENDING)
        pending = pending?.takeIf{ !it.contains(exerciseDir) }?.plus(exerciseDir) ?: arrayOf(exerciseDir)
        properties.setValues(PENDING, pending)
    }

    override fun alreadyImported(exerciseId: Long): Boolean {
        if (super.alreadyImported(exerciseId)) {
            val lfs = LocalFileSystem.getInstance()
            val basePath = ServiceManager.getService(OrionSettingsProvider::class.java).getSetting(OrionSettingsProvider.KEYS.PROJECT_BASE_DIR)
            val artemisHome = lfs.findFileByPath(basePath)
            VfsUtil.markDirtyAndRefresh(false, true, true, artemisHome)
            val projectDirs = lfs.refreshAndFindFileByPath(basePath)?.children
            return projectDirs?.any { p -> p.path.contains("_instructor") } ?: false
        }

        return false
    }

    override fun isOpenedAsInstructor(): Boolean {
        if (isArtemisExercise) {
            val alreadyOpenedAndRegistered = PropertiesComponent.getInstance(project).getBoolean(AS_INSTRUCTOR, false)
            return alreadyOpenedAndRegistered || project.basePath!!.contains("_instructor")
        }

        return false
    }

    private companion object {
        const val AS_INSTRUCTOR = BASE_KEY + "asInstructor"
    }
}

