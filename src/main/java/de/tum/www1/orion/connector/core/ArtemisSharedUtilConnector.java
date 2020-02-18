package de.tum.www1.orion.connector.core;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import de.tum.www1.orion.connector.JavaUpcallBridge;
import org.jetbrains.annotations.NotNull;

public interface ArtemisSharedUtilConnector extends JavaUpcallBridge {
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

    static ArtemisSharedUtilConnector getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, ArtemisSharedUtilConnector.class);
    }
}
