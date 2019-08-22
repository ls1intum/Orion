package de.tum.www1.artemis.plugin.intellij.bridge;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import javafx.scene.web.WebEngine;
import org.jetbrains.annotations.NotNull;

public interface ArtemisBridge {
    void clone(String repository, String exerciseName, int exerciseId, int courseId);
    void addCommitAndPushAllChanges();
    void login(String username, String password);
    void log(String message);
    void onOpenedExercise(int exerciseId);
    void artemisLoadedWith(WebEngine engine);

    static ArtemisBridge getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, ArtemisBridge.class);
    }
}
