package de.tum.www1.artemis.plugin.intellij.util;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public interface ArtemisExerciseRegistry {
    void onNewExercise(int courseId, int exerciseId, String exerciseName);
    void registerPendingExercises();
    boolean isArtemisExercise();
    int getExerciseId();
    String getExerciseName();
    int getCourseId();

    static ArtemisExerciseRegistry getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, ArtemisExerciseRegistry.class);
    }
}
