package de.tum.www1.artemis.plugin.intellij.vcs.bridge;

public interface ArtemisBridge {
    void clone(String repository, String exerciseName);
    void login(String username, String password);
}
