package de.tum.www1.orion.ui.browser

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import de.tum.www1.orion.ui.OrionRouter

/**
 * Action that opens Orion's documentation page, enabled only after the browser loaded
 */
class BrowserHelpAction : AnAction() {
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = (e.project?.service<IBrowser>()?.isInitialized ?: false)
    }

    override fun actionPerformed(e: AnActionEvent) {
        e.project?.let {
            it.service<IBrowser>().loadUrl(it.service<OrionRouter>().routeForDocumentation())
        }
    }
}
