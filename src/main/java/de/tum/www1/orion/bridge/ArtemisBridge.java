package de.tum.www1.orion.bridge;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import de.tum.www1.orion.enumeration.ExerciseView;
import javafx.scene.web.WebEngine;
import org.jetbrains.annotations.NotNull;

public interface ArtemisBridge {

    /**
     * Downcall from Java to Angular. Notifies the web app about a newly opened exercise in the IDE. Should be called
     * as soon as a project gets opened.
     *
     * @param opened The ID of the opened exercise
     */
    void onOpenedExercise(long opened, ExerciseView view);

    /**
     * Notifies Artemis if the IDE is in the process of importing (i.e. cloning) an exercise)
     *
     * @param cloning True, if there is a open clone process, false otherwise
     */
    void isCloning(boolean cloning);

    /**
     * Notifies Artemis if the IDE is currently building (and testing) the checked out exercise
     *
     * @param building True, a building process is currently open, false otherwise
     */
    void isBuilding(boolean building);

    /**
     * Notifies the ArtemisBridge, that all web content has been loaded. This is used to trigger all remaining
     * downcalls to Angular, which were queued, because ArTEMiS was not fully loaded, yet.
     *
     * @param engine The web engine used for loading the ArTEMiS webapp.
     */
    void artemisLoadedWith(WebEngine engine);

    /**
     * Triggers a new build and test run from the IDE out to the Artemis client webapp.
     *
     * @param courseId The ID of the course of the exercise for which to run tests
     * @param exerciseId The exericse itself
     */
    void startedBuildInIntelliJ(long courseId, long exerciseId);


    static ArtemisBridge getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, ArtemisBridge.class);
    }
}
