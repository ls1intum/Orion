package de.tum.www1.orion.connector.ide.vcs

interface IOrionVCSConnector {
    /**
     * This method now does nothing, the submitting is now delegated to onBuildStarted() so it has more information on
     * whether or not the commit is successful and acts accordingly. The server always calls onBuildStarted() after submit() anyways.
     */
    fun submit()

    /**
     * Switches the focused repository for instructors. This is the repository that gets used when submitting or testing code
     *
     * @param repository The repository the instructor wants to focus on [de.tum.www1.orion.dto.RepositoryType]
     */
    fun selectRepository(repository: String)
}