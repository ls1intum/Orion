package de.tum.www1.orion.bridge;

import com.intellij.openapi.project.Project;

public class OrionCoreUpcallBridge extends SimpleOrionUpcallBridge {
    public OrionCoreUpcallBridge(Project project) {
        super(project);
    }

    /**
     * Imports (clones) an exercises (all three base repositories: template, tests and solution) and creates a new
     * project containing those repos, allowing instructors to edit the whole exercise in one project.
     *
     * @param exerciseJson The exercise that should be imported formatted as a JSON string
     */
    void editExercise(String exerciseJson) {

    }

    /**
     * Clones the exercise participation repository and saves it under the artemis home directory
     *
     * @param repository The FQDN of the remote repository
     */
    void editExercise(String repository, String exerciseJson) {

    }
}
