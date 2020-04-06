package de.tum.www1.orion.connector.ide.vcs.submit;

public interface ChangeSubmissionStrategy {
    /**
     * Adds all changed files to the repository, except for the files specified in the .gitignore file.
     * The changes are then committed and pushed to the remote repository
     */
    void submitChanges();
}
