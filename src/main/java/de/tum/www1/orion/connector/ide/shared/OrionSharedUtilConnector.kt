package de.tum.www1.orion.connector.ide.shared

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.jcef.JBCefJSQuery
import de.tum.www1.orion.connector.ide.OrionConnector
import de.tum.www1.orion.ui.browser.IBrowser
import de.tum.www1.orion.util.nextAll
import de.tum.www1.orion.vcs.OrionGitCredentialsService
import com.intellij.openapi.diagnostic.Logger
import java.util.*

/**
 * A Java Handler for when user logs into Artemis
 */
@Service(Service.Level.PROJECT)
class OrionSharedUtilConnector(val project: Project) : OrionConnector(), IOrionSharedUtilConnector {
    override fun login(username: String, password: String) {
        service<OrionGitCredentialsService>().storeGitCredentials(username, password)
    }

    override fun log(message: String) {
        Logger.getInstance(OrionSharedUtilConnector::class.java).info(message)
    }

    override fun initializeHandlers(browser: IBrowser, queryInjector: JBCefJSQuery) {
        val reactions = mapOf("login" to { scanner: Scanner -> login(scanner.nextLine(), scanner.nextLine()) },
            "log" to { scanner: Scanner -> log(scanner.nextAll()) })
        addJavaHandler(browser, reactions)

        val parameterNames = mapOf(
            "login" to listOf("username", "password"),
            "log" to listOf("message")
        )
        addLoadHandler(browser, queryInjector, parameterNames)
    }
}
