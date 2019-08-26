package de.tum.www1.artemis.plugin.intellij.ui;

public interface ArtemisRouter {
    /**
     * Get the route for the currently opened exercise/project. The route is the full URL for the web browser, leading
     * to the exercise description including all test results
     *
     * @return The URL to the exercise in the ArTEMiS webapp
     */
    String routeForCurrentExercise();

    /**
     * Get the default URL to the ArTEMiS homepage
     *
     * @return The URL leading to the ArTEMiS homepage
     */
    String defaultRoute();
}
