package de.tum.www1.orion.ui.browser

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service

/**
 * Action that returns to the configured homepage when used, enabled only if browser loaded properly
 */
class BrowserReturnAction : AnAction() {
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = (e.project?.service<IBrowser>()?.isInitialized ?: false)
    }

    override fun actionPerformed(e: AnActionEvent) {
        e.project?.service<IBrowser>()?.returnToExercise()
    }
}