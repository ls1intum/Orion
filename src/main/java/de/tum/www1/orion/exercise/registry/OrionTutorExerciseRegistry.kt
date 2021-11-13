package de.tum.www1.orion.exercise.registry

/**
 * Registry for tutor exercises
 */
interface OrionTutorExerciseRegistry : OrionExerciseRegistry {
    val submissionId: Long?

    val correctionRound: Long?

    val testRun: Boolean?

    /**
     * Set submission data. The variables only change together
     *
     * @param submissionId id to set
     * @param correctionRound correctionRound to set
     */
    fun setSubmission(submissionId: Long?, correctionRound: Long?, testRun: Boolean?)
}
