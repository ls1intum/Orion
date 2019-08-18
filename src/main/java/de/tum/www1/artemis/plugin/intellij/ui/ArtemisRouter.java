package de.tum.www1.artemis.plugin.intellij.ui;

public interface ArtemisRouter {
    void onNewExercise(String name, int id);
    void registerPendingRoutes();
    String routeForCurrentExercise();
}
