package de.tum.www1.orion.connector.ide.exercise

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.jcef.JBCefJSQuery
import de.tum.www1.orion.connector.ide.OrionConnector
import de.tum.www1.orion.dto.ProgrammingExercise
import de.tum.www1.orion.exercise.OrionExerciseService
import de.tum.www1.orion.ui.browser.IBrowser
import de.tum.www1.orion.util.JsonUtils.gson
import de.tum.www1.orion.util.nextAll
import java.util.*

/**
 * Java handler for when an exercise is first opened
 */
@Service
class OrionExerciseConnector(val project: Project) : OrionConnector(), IOrionExerciseConnector {
    override fun editExercise(exerciseJson: String) {
        val exercise = gson().fromJson(exerciseJson, ProgrammingExercise::class.java)
        project.service<OrionExerciseService>().editExercise(exercise)
    }

    override fun importParticipation(repositoryUrl: String, exerciseJson: String) {
        val exercise = gson().fromJson(exerciseJson, ProgrammingExercise::class.java)
        project.service<OrionExerciseService>().importParticipation(repositoryUrl, exercise)
    }

    override fun assessExercise(exerciseJson: String) {
        val exercise = gson().fromJson(exerciseJson, ProgrammingExercise::class.java)
        project.service<OrionExerciseService>().assessExercise(exercise)
    }

    override fun initializeHandlers(browser: IBrowser, queryInjector: JBCefJSQuery) {
        val reactions = mapOf("editExercise" to { scanner: Scanner -> editExercise(scanner.nextAll()) },
            "importParticipation" to { scanner: Scanner -> importParticipation(scanner.nextLine(), scanner.nextAll()) },
            "assessExercise" to { scanner: Scanner -> assessExercise(scanner.nextAll()) })
        addJavaHandler(browser, reactions)

        val parameterNames = mapOf(
            "editExercise" to listOf("exerciseJson"),
            "importParticipation" to listOf("repositoryUrl", "exerciseJson"),
            "assessExercise" to listOf("exerciseJson")
        )
        addLoadHandler(browser, queryInjector, parameterNames)
    }
}