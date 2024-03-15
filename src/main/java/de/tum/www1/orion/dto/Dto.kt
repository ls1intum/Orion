package de.tum.www1.orion.dto

import de.tum.www1.orion.enumeration.ProgrammingLanguage
import java.net.URL

// All entities are defined like the entities in Artemis

/**
 * Course with properties as defined in Artemis at entities/course.model.ts
 */
data class Course(val id: Long, val title: String)

/**
 * Programming exercise with properties as defined in Artemis at entities/programming-exercise.model.ts
 */
data class ProgrammingExercise(
    val id: Long,
    val title: String,
    private val course: Course?,
    val templateParticipation: ProgrammingExerciseParticipation,
    val solutionParticipation: ProgrammingExerciseParticipation,
    val testRepositoryUri: URL,
    val programmingLanguage: ProgrammingLanguage,
    val auxiliaryRepositories: List<AuxiliaryRepository>?,
    val exerciseGroup: ExerciseGroup?,
    val studentParticipations: Array<ProgrammingExerciseStudentParticipation>
) {
    /**
     * Returns the course of the exercise, either directly or, if it is not set, from the associated exam
     * Artemis does not guarantee the course to be set directly
     * Throws an exception if neither the course nor the exercise is set
     *
     * @return course the exercise is associated with
     */
    fun getCourse(): Course {
        return course ?: exerciseGroup!!.exam.course
    }
}

/**
 * A group of Exercises
 * @param id the unique id
 * @param exam the [Exam] the exercise group belongs to
 */
data class ExerciseGroup(
    val id: Long,
    val exam: Exam
)

/**
 * Exam class providing some values of the exam
 */
data class Exam(
    val id: Long,
    val title: String,
    val course: Course
)

/**
 * Programming exercise participation as defined in Artemis at entities/participation/programming-exercise-student-participation.model.ts
 * @param id the unique id of a programming exercise
 * @param repositoryUrl the URL of the repository
 * @param buildPlanId the id of the buildplan
 */
data class ProgrammingExerciseParticipation(
    val id: Long,
    val repositoryUri: URL,
    val buildPlanId: String,
    var locked: Boolean
)

/**
 * The Result of s student submission
 * @param id the unique id of the result
 * @param rated boolean value indicating if the result has a rating
 * @param feedbacks an array containing tutor-feedback of the type [Feedback]
 */
data class Result(
    val id: Long,
    val rated: Boolean,
    val feedbacks: Array<Feedback>?
)

/**
 * A Programming excercise participation
 * @param id the unique id
 * @param results an Array containing [Result]s
 */
data class ProgrammingExerciseStudentParticipation(
    val id: Long,
    val results: Array<Result>?
)

/**
 * Auxiliary repository as defined in Artemis at entities/programming-exercise-auxiliary-repository-model.ts
 */
data class AuxiliaryRepository(
    val id: Long,
    val name: String,
    val checkoutDirectory: String,
    val repositoryUri: URL,
    val description: String
)

/**
 * Feedback with properties as defined in Artemis at entities/feedback.model.ts
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
    val reference: String?,
    val text: String,
    val type: String,
    var gradingInstruction: GradingInstruction?,
    var line: Int?,
    var path: String?,
)

/**
 * Grading instruction as defined in Artemis at exercises/shared/structured-grading-criterion/grading-instruction.model.ts
 */
data class GradingInstruction(
    var id: Int?, val credits: Double, val gradingScale: String,
    val instructionDescription: String,
    val feedback: String,
    val usageCount: Int?
)
