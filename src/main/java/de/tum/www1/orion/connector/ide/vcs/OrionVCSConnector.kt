package de.tum.www1.orion.connector.ide.vcs

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.jcef.JBCefJSQuery
import de.tum.www1.orion.connector.ide.OrionConnector
import de.tum.www1.orion.dto.RepositoryType
import de.tum.www1.orion.exercise.registry.OrionInstructorExerciseRegistry
import de.tum.www1.orion.ui.browser.IBrowser
import java.util.*


@Service(Service.Level.PROJECT)
class OrionVCSConnector(val project: Project) : OrionConnector(), IOrionVCSConnector {
    @Deprecated("Replaced with onBuildStarted")
    override fun submit() {
    }

    override fun selectRepository(repository: String) {
        val parsedRepo = RepositoryType.valueOf(repository)
        project.service<OrionInstructorExerciseRegistry>().selectedRepository = parsedRepo
    }

    override fun initializeHandlers(browser: IBrowser, queryInjector: JBCefJSQuery) {
        val reactions = mapOf("submit" to { submit() },
            "selectRepository" to { scanner: Scanner -> selectRepository(scanner.nextLine()) })
        addJavaHandler(browser, reactions)

        val parameterNames = mapOf(
            "submit" to listOf(),
            "selectRepository" to listOf("repository")
        )
        addLoadHandler(browser, queryInjector, parameterNames)
    }
}
