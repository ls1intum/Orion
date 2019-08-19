package de.tum.www1.artemis.plugin.intellij.bridge;

public interface ArtemisBridge {
    void clone(String repository, String exerciseName, int exerciseId, int courseId);
    void addCommitAndPushAllChanges();
    void login(String username, String password);
    void log(String message);
}
