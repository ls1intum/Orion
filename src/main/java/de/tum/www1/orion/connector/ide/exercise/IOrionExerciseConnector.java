package de.tum.www1.orion.connector.ide.exercise;

public interface IOrionExerciseConnector {

    /**
     * Imports (clones) an exercises (all three base repositories: template, tests and solution) and creates a new
     * project containing those repos, allowing instructors to edit the whole exercise in one project.
     *
     * @param exerciseJson The exercise that should be imported formatted as a JSON string
     */
    void editExercise(String exerciseJson);

    /**
     * Clones the exercise participation repository and saves it under the artemis home directory
     *
     * @param repositoryUrl The URL of the remote repository
     */
    void importParticipation(String repositoryUrl, String exerciseJson);
}
