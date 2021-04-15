package de.tum.www1.orion.ui.browser

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class BrowserReturnAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        println("BrowserReturnAction activated")
    }
}
