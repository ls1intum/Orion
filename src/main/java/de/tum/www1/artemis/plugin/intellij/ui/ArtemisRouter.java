package de.tum.www1.artemis.plugin.intellij.ui;

public interface ArtemisRouter {
    void onNewExercise(String name, int exerciseId, int courseId);
    void registerPendingRoutes();
    String routeForCurrentExercise();
}
