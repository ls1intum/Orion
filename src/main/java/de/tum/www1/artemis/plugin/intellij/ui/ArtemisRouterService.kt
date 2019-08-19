package de.tum.www1.artemis.plugin.intellij.ui

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.util.containers.toArray

class ArtemisRouterService(project: Project): ArtemisRouter {
    private val myProject: Project = project

    init {
        registerPendingRoutes()
    }

    override fun onNewExercise(name: String, exerciseId: Int, courseId: Int) {
        val properties = PropertiesComponent.getInstance()
        var pending = properties.getValues(PENDING)
        if (pending == null) {
            properties.setValues(PENDING, arrayOfNulls(0))
            return
        }
        val url = EXERCISE_DETAIL_URL.format(courseId, exerciseId)
        pending = pending.plus("$name|$url")
        properties.setValues(PENDING, pending)
    }

    override fun registerPendingRoutes() {
        val properties = PropertiesComponent.getInstance()
        val projectProperties = PropertiesComponent.getInstance(myProject)
        val pending = properties.getValues(PENDING)
        val pendingForCurrent = pending?.firstOrNull { it.split('|')[0] == myProject.basePath!!.split('/').last() }
        if (pendingForCurrent != null) {
            val remainingPending = pending.filter { pendingForCurrent != it }
            properties.setValues(PENDING, remainingPending.toArray(arrayOfNulls(remainingPending.size)))
            projectProperties.setValue(ROUTE, pendingForCurrent.split('|')[1])
        }
    }

    override fun routeForCurrentExercise(): String? {
        return PropertiesComponent.getInstance(myProject).getValue(ROUTE)
    }

    companion object {
        private const val BASE_KEY = "artemis.plugin."
        private const val PENDING = BASE_KEY + "pending"
        private const val ROUTE = BASE_KEY + "route"
        private const val EXERCISE_DETAIL_URL = "https://artemistest.ase.in.tum.de/#/overview/%d/exercises/%d"

        @JvmStatic
        fun getInstance(project: Project): ArtemisRouterService {
            return ServiceManager.getService(project, ArtemisRouterService::class.java)
        }
    }
}
