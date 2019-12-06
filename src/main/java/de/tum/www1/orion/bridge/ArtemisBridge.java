package de.tum.www1.orion.bridge;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public interface ArtemisBridge {


    static ArtemisBridge getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, ArtemisBridge.class);
    }
}
