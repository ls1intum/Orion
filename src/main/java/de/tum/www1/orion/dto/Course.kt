package de.tum.www1.orion.dto

import de.tum.www1.orion.enumeration.ProgrammingLanguage
import java.net.URL
import java.time.ZonedDateTime

enum class AssessmentType {
    AUTOMATIC, SEMI_AUTOMATIC, MANUAL
}

enum class DifficultyLevel {
    EASY, MEDIUM, HARD
}

data class Course(val id: Long, val title: String, val description: String, val shortName: String, val studentGroupName: String,
                  val teachingAssistentGroupName: String, val instructorGroupName: String, val startDate: ZonedDateTime?,
                  val endDate: ZonedDateTime?, val color: String, val courseIcon: String, val onlineCourse: Boolean,
                  val registrationEnabled: Boolean, val presentationScore: Int, val maxComplaints: Int)

data class ProgrammingExerciseDTO(val id: Long, val title: String, val gradingInstructions: String, val shortName: String,
                                  val releaseDate: ZonedDateTime?, val dueDate: ZonedDateTime?, val assessmentDueDate: ZonedDateTime?,
                                  val maxScore: Int, val assessmentType: AssessmentType, val difficulty: DifficultyLevel?,
                                  val categories: List<String>?, val course: Course, val projectKey: String,
                                  val templateParticipation: ProgrammingExerciseParticipation,
                                  val solutionParticipation: ProgrammingExerciseParticipation,
                                  val testRepositoryUrl: URL,
                                  val publishBuildPlanUrl: Boolean, val allowOnlineEditor: Boolean,
                                  val programmingLanguage: ProgrammingLanguage, val packageName: String,
                                  val problemStatement: String, val sequentialTestRuns: Boolean?,
                                  val buildAndTestStudentSubmissionsAfterDueDate: ZonedDateTime?)

data class ProgrammingExerciseParticipation(val repositoryUrl: URL, val buildPlanId: String)