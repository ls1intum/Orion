package de.tum.www1.orion.bridge.downcall;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import javafx.scene.web.WebEngine;
import org.jetbrains.annotations.NotNull;

public interface ArtemisJavascriptDowncallBridge {
    /**
     * Notifies the ArtemisBridge, that all web content has been loaded. This is used to trigger all remaining
     * downcalls to Angular, which were queued, because ArTEMiS was not fully loaded, yet.
     *
     * @param engine The web engine used for loading the ArTEMiS webapp.
     */
    void artemisLoadedWith(WebEngine engine);

    void initStateListeners();

    static ArtemisJavascriptDowncallBridge getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, ArtemisJavascriptDowncallBridge.class);
    }
}
