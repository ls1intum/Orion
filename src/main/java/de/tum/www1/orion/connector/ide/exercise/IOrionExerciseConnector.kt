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
     * Clones the exercise participation repository and saves it under the artemis home directory
     *
     * @param repositoryUrl The URL of the remote repository
     */
    fun importParticipation(repositoryUrl: String, exerciseJson: String)

    enum class FunctionName {
        EditExercise, ImportParticipation
    }
}