package de.tum.www1.orion.connector.ide.vcs

interface IOrionVCSConnector {
    fun submit()

    /**
     * Switches the focused repository for instructors. This is the repository that gets used when submitting or testing code
     *
     * @param repository The repository the instructor wants to focus on [de.tum.www1.orion.dto.RepositoryType]
     */
    fun selectRepository(repository: String)
}