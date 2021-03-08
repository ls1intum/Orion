package de.tum.www1.orion.connector.ide.shared

interface IOrionSharedUtilConnector {
    /**
     * Logs the user in. As of now, this method should at least inject the specified credentials into the stored
     * list of Git credentials, so the import of exercises is possible without asking the user for the credentials every
     * time
     *
     * @param username The username used in Artemis, e.g. ga12abc
     * @param password The password used in Artemis
     */
    fun login(username: String, password: String)

    /**
     * Logs a message from the web in the IDE logging system. This is most useful for debugging purposes
     *
     * @param message A message to be logged in IntelliJ
     */
    fun log(message: String)

    enum class FunctionName {
        login, log
    }
}