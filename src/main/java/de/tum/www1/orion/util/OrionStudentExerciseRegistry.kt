package de.tum.www1.orion.util;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public interface OrionStudentExerciseRegistry extends OrionExerciseRegistry {
    static OrionStudentExerciseRegistry getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, OrionStudentExerciseRegistry.class);
    }
}
