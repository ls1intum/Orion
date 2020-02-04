package de.tum.www1.orion.ui.browser;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class BrowserFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        final ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Browser browser = ServiceManager.getService(project, Browser.class);
        final Content content = contentFactory.createContent(browser, "", false);
        toolWindow.getContentManager().addContent(content);
        browser.init();
    }
}
