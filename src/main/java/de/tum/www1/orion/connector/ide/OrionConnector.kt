package de.tum.www1.orion.connector.ide;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import de.tum.www1.orion.ui.browser.BrowserWebView;
import netscape.javascript.JSObject;
import org.jetbrains.annotations.NotNull;

public abstract class OrionConnector implements ArtemisConnector, StartupActivity {
    protected Project project;
    private String connectorName;

    @Override
    public void runActivity(@NotNull Project project) {
        this.project = project;

        // Most performant way to set the first letter to lowercase according to
        // https://stackoverflow.com/questions/4052840/most-efficient-way-to-make-the-first-character-of-a-string-lower-case
        final var classNameChars = this.getClass().getSimpleName().toCharArray();
        classNameChars[0] = Character.toLowerCase(classNameChars[0]);
        this.connectorName = new String(classNameChars);

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
