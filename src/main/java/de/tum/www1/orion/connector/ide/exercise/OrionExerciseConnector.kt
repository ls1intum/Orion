package de.tum.www1.orion.connector.ide.exercise

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.jcef.JBCefJSQuery
import de.tum.www1.orion.connector.ide.OrionConnector
import de.tum.www1.orion.dto.Feedback
import de.tum.www1.orion.dto.ProgrammingExercise
import de.tum.www1.orion.exercise.OrionAssessmentService
import de.tum.www1.orion.exercise.OrionExerciseService
import de.tum.www1.orion.messaging.OrionIntellijStateNotifier
import de.tum.www1.orion.ui.browser.IBrowser
import de.tum.www1.orion.ui.util.notify
import de.tum.www1.orion.util.JsonUtils.gson
import de.tum.www1.orion.util.nextAll
import de.tum.www1.orion.util.translate
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

    override fun downloadSubmission(submissionId: Long, correctionRound: Long, base64data: String) {
        project.service<OrionExerciseService>().downloadSubmission(submissionId, correctionRound, base64data)
    }

    override fun initializeAssessment(submissionId: Long, feedback: String) {
        val feedbackArray = gson().fromJson(feedback, Array<Feedback>::class.java)
        project.service<OrionAssessmentService>().initializeFeedback(submissionId, feedbackArray)
    }

    override fun initializeHandlers(browser: IBrowser, queryInjector: JBCefJSQuery) {
        val reactions = mapOf("editExercise" to { scanner: Scanner -> editExercise(scanner.nextAll()) },
            "importParticipation" to { scanner: Scanner -> importParticipation(scanner.nextLine(), scanner.nextAll()) },
            "assessExercise" to { scanner: Scanner -> assessExercise(scanner.nextAll()) },
            "downloadSubmission" to { scanner: Scanner ->
                try {
                    downloadSubmission(
                        scanner.nextLine().toLong(),
                        scanner.nextLine().toLong(),
                        scanner.nextAll()
                    )
                } catch (e: OutOfMemoryError) {
                    // Error handling has to be this high level since the heap size error is thrown by scanner.nextAll()
                    // With default heap size of 512mb already submissions as small as 17mb lead to this error
                    project.notify(translate("orion.exercise.submissiondownloadfailed"))
                    project.messageBus.syncPublisher(OrionIntellijStateNotifier.INTELLIJ_STATE_TOPIC).isCloning(false)
                }
            },
            "initializeAssessment" to { scanner ->
                initializeAssessment(scanner.nextLine().toLong(), scanner.nextAll())
            })
        addJavaHandler(browser, reactions)

        val parameterNames = mapOf(
            "editExercise" to listOf("exerciseJson"),
            "importParticipation" to listOf("repositoryUrl", "exerciseJson"),
            "assessExercise" to listOf("exerciseJson"),
            "downloadSubmission" to listOf("submissionId", "correctionRound", "downloadURL"),
            "initializeAssessment" to listOf("submissionId", "feedback")
        )
        addLoadHandler(browser, queryInjector, parameterNames)
    }
}
