package de.tum.www1.orion.bridge;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import de.tum.www1.orion.enumeration.ExerciseView;
import javafx.scene.web.WebEngine;
import org.jetbrains.annotations.NotNull;

public interface ArtemisBridge {
    /**
     * Clones the exercise participation repository and saves it under the artemis home directory
     *
     * @param repository The FQDN of the remote repository
     */
    void clone(String repository, String exerciseJson);

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
     * Notify external build started
     */
    void onBuildStarted();

    /**
     * Notify external build finished without any compile errors
     */
    void onBuildFinished();

    /**
     * Notify external build failed with compile errors
     *
     * @param buildLogsJsonString The build log errors. Will be parsed into {@link de.tum.www1.orion.dto.BuildLogFileErrorsDTO}
     */
    void onBuildFailed(String buildLogsJsonString);

    /**
     * Notify about incoming test result
     *
     * @param success True, if the test was successful, false otherwise
     * @param message Any message related to the test, which should be displayed on the console
     */
    void onTestResult(boolean success, String message);

    /**
     * Downcall from Java to Angular. Notifies the web app about a newly opened exercise in the IDE. Should be called
     * as soon as a project gets opened.
     *
     * @param opened The ID of the opened exercise
     */
    void onOpenedExercise(long opened, ExerciseView view);

    void selectInstructorRepository(String repository);

    void submitInstructorRepository();

    void buildAndTestInstructorRepository();

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

    void editExercise(String exerciseJson);

    static ArtemisBridge getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, ArtemisBridge.class);
    }
}
