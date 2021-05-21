package de.tum.www1.orion.ui.browser

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory.SERVICE

class BrowserFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = SERVICE.getInstance()
        val browserUIInitializationService = project.service<BrowserUIInitializationService>()
        val content = contentFactory.createContent(browserUIInitializationService, "", false)
        toolWindow.contentManager.addContent(content)
        val actionManager = ActionManager.getInstance()
        toolWindow.setTitleActions(listOf(actionManager.getAction("de.tum.www1.orion.ui.browser.BrowserReturnAction")))
        browserUIInitializationService.init()
    }
}