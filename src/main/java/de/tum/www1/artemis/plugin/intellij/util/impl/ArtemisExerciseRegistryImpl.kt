package de.tum.www1.artemis.plugin.intellij.util.impl

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import de.tum.www1.artemis.plugin.intellij.util.ArtemisExerciseRegistry
import java.io.File

class ArtemisExerciseRegistryImpl(private val project: Project) : ArtemisExerciseRegistry {
    override fun onNewExercise(courseId: Int, exerciseId: Int, exerciseName: String) {
        val name = exerciseName.replace(' ', '_')
        val exerciseDir = "$courseId-$exerciseId-$name"
        val properties = PropertiesComponent.getInstance()
        var pending = properties.getValues(PENDING)
        pending = pending?.plus(exerciseDir) ?: arrayOf(exerciseDir)
        properties.setValues(PENDING, pending)
    }

    override fun registerPendingExercises() {
        val properties = PropertiesComponent.getInstance()
        val projectProperties = PropertiesComponent.getInstance(project)
        val currentDir = project.basePath!!.split(File.separatorChar).last()
        val pending = properties.getValues(PENDING)
        pending?.forEach {
            if (it == currentDir) {
                val dirAndName = currentDir.split('-')
                projectProperties.setValue(COURSE_ID, dirAndName[0].toInt(), -1)
                projectProperties.setValue(EXERCISE_ID, dirAndName[1].toInt(), -1)
                projectProperties.setValue(EXERCISE_NAME, dirAndName[2], null)
            }
        }
    }

    override fun isArtemisExercise(): Boolean =
            PropertiesComponent.getInstance(project).getInt(EXERCISE_ID, -1) != -1

    override fun isCurrentlyOpened(exerciseId: Int): Boolean =
            PropertiesComponent.getInstance(project).getInt(EXERCISE_ID, -1) == exerciseId

    override fun getExerciseId(): Int = PropertiesComponent.getInstance(project).getInt(EXERCISE_ID, -1)

    override fun getExerciseName(): String? = PropertiesComponent.getInstance(project).getValue(EXERCISE_NAME)

    override fun getCourseId(): Int = PropertiesComponent.getInstance(project).getInt(COURSE_ID, -1)

    companion object {
        private const val BASE_KEY = "artemis.plugin.registry."
        private const val PENDING = BASE_KEY + "pending"
        private const val EXERCISE_ID = BASE_KEY + "exerciseId"
        private const val EXERCISE_NAME = BASE_KEY + "exerciseName"
        private const val COURSE_ID = BASE_KEY + "courseId"
    }
}
