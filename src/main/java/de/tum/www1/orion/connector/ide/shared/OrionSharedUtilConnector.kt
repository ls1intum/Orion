package de.tum.www1.orion.connector.ide.shared

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.ui.jcef.JBCefJSQuery
import de.tum.www1.orion.connector.ide.OrionConnector
import de.tum.www1.orion.ui.browser.IBrowser
import de.tum.www1.orion.util.nextAll
import de.tum.www1.orion.vcs.OrionGitCredentialsService
import org.slf4j.LoggerFactory
import java.util.*

/**
 * A Java Handler for when user logs into Artemis
 */
@Service
class OrionSharedUtilConnector(val project: Project) : OrionConnector(), IOrionSharedUtilConnector {
    override fun login(username: String, password: String) {
        ServiceManager.getService(OrionGitCredentialsService::class.java).storeGitCredentials(username, password)
    }

    override fun log(message: String) {
        LoggerFactory.getLogger(OrionSharedUtilConnector::class.java).info(message)
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