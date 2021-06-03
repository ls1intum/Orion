package de.tum.www1.orion.exercise.registry

interface OrionTutorExerciseRegistry : OrionExerciseRegistry {
    val submissionId: Long?

    val correctionRound: Long?

    fun setSubmission(submissionId: Long, correctionRound: Long)
}