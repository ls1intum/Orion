package de.tum.www1.artemis.plugin.intellij.bridge;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import javafx.scene.web.WebEngine;
import org.jetbrains.annotations.NotNull;

public interface ArtemisBridge {
    /**
     * Clones the exercise participation repository and saves it under the artemis home directory
     *
     * @param repository The FQDN of the remote repository
     * @param exerciseName The name of the programming exercise (can be any name in theory, just used for readability)
     * @param exerciseId The ID of the programming exercise
     * @param courseId THe ID of the course, to which the exercise is registered
     */
    void clone(String repository, String exerciseName, int exerciseId, int courseId);

    /**
     * Adds all changed files to the repository, except for the files specified in the .gitignore file.
     * The changes are then committed and pushed to the remote repository
     */
    void addCommitAndPushAllChanges();

    /**
     * Logs the user in. As of now, this method should at least inject the specified credentials into the stored
     * list of Git credentials, so the import of exercises is possible without asking the user for the credentials every
     * time
     *
     * @param username The username used in ArTEMiS, e.g. ga12abc
     * @param password The password used in ArTEMiS
     */
    void login(String username, String password);

    /**
     * Logs a message from the web in the IDE logging system. This is most useful for debugging purposes
     *
     * @param message A message to be logged in IntelliJ
     */
    void log(String message);

    /**
     * Downcall from Java to Angular. Notifies the web app about a newly opened exercise in the IDE. Should be called
     * as soon as a project gets opened.
     *
     * @param exerciseId The ID of the opened exercise
     */
    void onOpenedExercise(int exerciseId);

    /**
     * Notifies the ArtemisBridge, that all web content has been loaded. This is used to trigger all remaining
     * downcalls to Angular, which were queued, because ArTEMiS was not fully loaded, yet.
     *
     * @param engine The web engine used for loading the ArTEMiS webapp.
     */
    void artemisLoadedWith(WebEngine engine);

    static ArtemisBridge getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, ArtemisBridge.class);
    }
}
