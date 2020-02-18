package de.tum.www1.orion.connector.ide;

import com.intellij.openapi.project.Project;
import de.tum.www1.orion.ui.browser.BrowserWebView;
import netscape.javascript.JSObject;

public abstract class OrionConnector implements ArtemisConnector {
    private String connectorName;

    protected OrionConnector(Project project, String connectorName) {
        this.connectorName = connectorName;
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
