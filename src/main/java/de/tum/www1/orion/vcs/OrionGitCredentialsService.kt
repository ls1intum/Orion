package de.tum.www1.orion.vcs

interface OrionGitCredentialsService {

    /**
     * Stores the specified credentials in the password safe under the Git credentials key, so that when executing
     * any authenticated Git actions, the user doesn't have to input the password again
     *
     * @param username The username used to login into Artemis
     * @param password The password used to login
     */
    fun storeGitCredentials(username: String, password: String)

    /**
     * Removes the stored credentials saved under the specified username
     *
     * @param username The username used to login into Artemis
     */
    fun removeGitCredentials(username: String)
}
