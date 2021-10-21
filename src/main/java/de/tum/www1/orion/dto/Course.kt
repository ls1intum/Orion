package de.tum.www1.orion.dto

import de.tum.www1.orion.enumeration.ProgrammingLanguage
import java.net.URL
import java.time.ZonedDateTime

/**
 * AssessmentType as defined by https://github.com/ls1intum/Artemis/blob/develop/src/main/webapp/app/entities/assessment-type.model.ts
 */
enum class AssessmentType {
    AUTOMATIC, SEMI_AUTOMATIC, MANUAL
}

enum class DifficultyLevel {
    EASY, MEDIUM, HARD
}

/**
 * Course with properties as defined by https://github.com/ls1intum/Artemis/blob/develop/src/main/webapp/app/entities/course.model.ts
 */
data class Course(
    val id: Long, val title: String, val description: String, val shortName: String, val studentGroupName: String,
    val teachingAssistantGroupName: String, val instructorGroupName: String, val startDate: ZonedDateTime?,
    val endDate: ZonedDateTime?, val color: String, val courseIcon: String, val onlineCourse: Boolean,
    val registrationEnabled: Boolean, val presentationScore: Int, val maxComplaints: Int
)

/**
 * Programming exercise with properties as defined by https://github.com/ls1intum/Artemis/blob/develop/src/main/webapp/app/entities/programming-exercise.model.ts
 */
data class ProgrammingExercise(
    val id: Long,
    val title: String,
    val gradingInstructions: String,
    val shortName: String,
    val releaseDate: ZonedDateTime?,
    val dueDate: ZonedDateTime?,
    val assessmentDueDate: ZonedDateTime?,
    val maxScore: Int,
    val assessmentType: AssessmentType,
    val difficulty: DifficultyLevel?,
    val categories: List<ExerciseCategory>?,
    val course: Course,
    val projectKey: String,
    val templateParticipation: ProgrammingExerciseParticipation,
    val solutionParticipation: ProgrammingExerciseParticipation,
    val testRepositoryUrl: URL,
    val publishBuildPlanUrl: Boolean,
    val allowOnlineEditor: Boolean,
    val programmingLanguage: ProgrammingLanguage,
    val packageName: String,
    val problemStatement: String,
    val sequentialTestRuns: Boolean?,
    val auxiliaryRepositories: List<AuxiliaryRepository>?,
    val buildAndTestStudentSubmissionsAfterDueDate: ZonedDateTime?
)

/**
 * Exercise category as defined by https://github.com/ls1intum/Artemis/blob/develop/src/main/webapp/app/entities/exercise-category.model.ts
 */
data class ExerciseCategory(val category: String, val color: String)

/**
 * Programming exercise participation as defined by https://github.com/ls1intum/Artemis/blob/develop/src/main/webapp/app/entities/participation/programming-exercise-student-participation.model.ts
 */
data class ProgrammingExerciseParticipation(val id: Long, val repositoryUrl: URL, val buildPlanId: String)

/**
 * Auxiliary repository as defined by https://github.com/ls1intum/Artemis/blob/develop/src/main/webapp/app/entities/programming-exercise-auxiliary-repository-model.ts
 */
data class AuxiliaryRepository(
    val id: Long,
    val name: String,
    val checkoutDirectory: String,
    val repositoryUrl: URL,
    val description: String
)

/**
 * Feedback with properties as defined by https://github.com/ls1intum/Artemis/blob/develop/src/main/webapp/app/entities/feedback.model.ts
 * Does not include the id since Orion does not need it
 *
 * @param credits mutable, can be changed locally
 * @param detailText mutable, can be changed locally
 * @param line not part of Artemis, sent as part of the reference, gets added in the [OrionAssessmentService]
 * @param path not part of Artemis, sent as part of the reference, gets added in the [OrionAssessmentService]. Relative path to the file the feedback belongs to, relative to the assignment folder
 */
data class Feedback(
    var credits: Double,
    var detailText: String,
    val reference: String,
    val text: String,
    val type: String,
    var gradingInstruction: GradingInstruction?,
    var line: Int?,
    var path: String?
)

/**
 * Grading instruction as defined by https://github.com/ls1intum/Artemis/blob/develop/src/main/webapp/app/exercises/shared/structured-grading-criterion/grading-instruction.model.ts
 */
data class GradingInstruction(
    var id: Int?, val credits: Double, val gradingScale: String,
    val instructionDescription: String,
    val feedback: String,
    val usageCount: Int?
)
