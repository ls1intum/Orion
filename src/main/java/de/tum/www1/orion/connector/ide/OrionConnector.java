package de.tum.www1.orion.connector.ide;

import com.intellij.openapi.project.Project;
import de.tum.www1.orion.ui.browser.BrowserWebView;
import netscape.javascript.JSObject;

public abstract class OrionConnector implements ArtemisConnector {
    protected Project project;
    private String connectorName = this.getClass().getName();

    protected OrionConnector(Project project) {
        this.project = project;
        project.getMessageBus().connect().subscribe(BrowserWebView.OrionBrowserNotifier.ORION_BROWSER_TOPIC, engine -> {
            final var window = (JSObject) engine.executeScript("window");
            attachTo(window, connectorName);
        });
    }

    @Override
    public void attachTo(JSObject jsObject, String memberName) {
        jsObject.setMember(memberName, this);
    }
}
