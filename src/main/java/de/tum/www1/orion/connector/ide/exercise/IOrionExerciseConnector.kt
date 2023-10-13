package de.tum.www1.orion.connector.ide.exercise

interface IOrionExerciseConnector {
    /**
     * Imports (clones) an exercises (all three base repositories: template, tests and solution) and creates a new
     * project containing those repos, allowing instructors to edit the whole exercise in one project.
     *
     * @param exerciseJson The exercise that should be imported formatted as a JSON string
     */
    fun editExercise(exerciseJson: String)

    /**
     * Clones the test repository to allow for later downloading of submissions
     *
     * @param exerciseJson The exercise that should be imported formatted as a JSON string
     */
    fun assessExercise(exerciseJson: String)

    /**
     * Downloads a submission into the opened tutor project
     *
     * @param submissionId id of the submission, used to navigate to the corresponding URL
     * @param correctionRound correction round, also needed to navigate to the correct URL
     * @param base64data data of the zip file containing the student's repository
     * @param testRun true if in a test run, also needed for navigation
     */
    // Uncomment this to activate transfer of the testRun flag
    // THIS IS A BREAKING CHANGE that will require a matching Artemis version
    // fun downloadSubmission(submissionId: Long, correctionRound: Long, testRun: Boolean, base64data: String)
    fun downloadSubmission(submissionId: Long, correctionRound: Long, base64data: String)

    /**
     * Initializes the [OrionAssessmentService] with all current feedback
     *
     * @param submissionId of the current submission, for validation
     * @param feedback list of current feedback
     */
    fun initializeAssessment(submissionId: Long, feedback: String)

    /**
     * Initializes the project with a test repository.
     *
     * @param testRepository the url or the test repository
     */
    fun initializeTestRepository(testRepository: String)

    /**
     * Initializes the [OrionFeedbackService]
     *
     * @param feedback a serialized [Programming]
     */
    fun initializeFeedback(feedback: String)

    /**
     * Clones the exercise participation repository and saves it under the artemis home directory
     *
     * @param repositoryUrl The URL of the remote repository
     */
    fun importParticipation(repositoryUrl: String, exerciseJson: String)
}
