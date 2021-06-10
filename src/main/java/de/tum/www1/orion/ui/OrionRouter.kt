package de.tum.www1.orion.ui

/**
 * Provides Orion specific URLs to the Browser
 */
interface OrionRouter {
    /**
     * Get the route for the currently opened exercise/project. The route is the full URL for the web browser,
     * leading to the exercise for students, the editor for instructors or the assessment dashboard/editor
     * for tutors.
     *
     * If no Artemis exercise is opened, the configured base URL is returned
     *
     * @return The URL to the exercise in the Artemis webapp or the base URL if no Artemis exercise is opened
     */
    fun routeForCurrentExerciseOrDefault(): String
}