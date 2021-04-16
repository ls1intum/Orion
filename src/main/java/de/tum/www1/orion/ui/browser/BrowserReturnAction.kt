package de.tum.www1.orion.ui.browser

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import de.tum.www1.orion.ui.util.notify

class BrowserReturnAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val browserService = e.project?.service<IBrowser>()
        if (browserService != null)
            browserService.returnToArtemis()
        else
        // TODO: Use project-independent notification
            e.project?.notify("Could not return to Homepage")
    }
}