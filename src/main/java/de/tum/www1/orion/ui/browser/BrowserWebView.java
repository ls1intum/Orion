package de.tum.www1.orion.ui.browser;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import de.tum.www1.orion.bridge.ArtemisBridge;
import de.tum.www1.orion.ui.OrionRouter;
import de.tum.www1.orion.ui.OrionRouterService;
import de.tum.www1.orion.util.OrionSettingsProvider;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import javax.swing.*;
import java.util.Objects;

public class BrowserWebView {
    private WebView browser;
    private WebEngine engine;
    private JFXPanel browserPanel;
    private Project project;
    private ArtemisBridge jsBridge;   // We need a strong reference to the bridge, so it doesn't get garbage collected

    /**
     * Inits the actual browser panel. We use a JFXPanel in a {@link WebView} gets initialized. This web view only
     * displays the ArTEMiS Angular webapp containing a few adaptions, so that we only show the most important information
     * in the IDE.
     */
    public void init() {
        browserPanel = new JFXPanel();
        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            browser = new WebView();
            engine = browser.getEngine();
            engine.setUserAgent(ServiceManager.getService(OrionSettingsProvider.class).getSetting(OrionSettingsProvider.KEYS.USER_AGENT) + " IntelliJ");
            project = Objects.requireNonNull(DataManager.getInstance().getDataContext(browserPanel).getData(CommonDataKeys.PROJECT));

            final OrionRouter orionRouter = ServiceManager.getService(project, OrionRouterService.class);
            final String route = orionRouter.routeForCurrentExercise();
            engine.load(Objects.requireNonNullElseGet(route, orionRouter::defaultRoute));

            injectJSBridge();
        });
    }

    private void injectJSBridge() {
        jsBridge = ServiceManager.getService(project, ArtemisBridge.class);
        engine.getLoadWorker().stateProperty().addListener((observableValue, state, t1) -> {
            if (state == Worker.State.SUCCEEDED || t1 == Worker.State.SUCCEEDED) {
                final JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("intellij", jsBridge);
                jsBridge.artemisLoadedWith(engine);
            }
        });
    }

    /**
     * Get the browser panel in which the ArTEMiS webapp is displayed.
     *
     * @return The {@link JFXPanel} containing the {@link WebView} with ArTEMiS
     */
    public JComponent getBrowser() {
        Platform.runLater(() -> {
            final BorderPane borderPane = new BorderPane();
            borderPane.setCenter(browser);
            final Scene scene = new Scene(borderPane);
            browserPanel.setScene(scene);
        });

        return browserPanel;
    }
}
