package de.tum.www1.orion.bridge;

import com.intellij.openapi.project.Project;

public abstract class SimpleOrionUpcallBridge extends ArtemisConnector implements ArtemisCoreUpcallBridge {
    protected final Project project;

    public SimpleOrionUpcallBridge(Project project) {
        this.project = project;
    }

    @Override
    public void login(String username, String password) {

    }

    @Override
    public void log(String message) {

    }
}
