package de.tum.www1.artemis.plugin.intellij.ui.browser;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import de.tum.www1.artemis.plugin.intellij.bridge.ArtemisBridge;
import de.tum.www1.artemis.plugin.intellij.ui.ArtemisRouter;
import de.tum.www1.artemis.plugin.intellij.ui.ArtemisRouterService;
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

    public void init() {
        browserPanel = new JFXPanel();
        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            browser = new WebView();
            engine = browser.getEngine();
            engine.setUserAgent(engine.getUserAgent() + " IntelliJ");
            project = Objects.requireNonNull(DataManager.getInstance().getDataContext(browserPanel).getData(CommonDataKeys.PROJECT));

            final ArtemisRouter artemisRouter = ServiceManager.getService(project, ArtemisRouterService.class);
            final String route = artemisRouter.routeForCurrentExercise();
            if (route != null) {
                engine.load(route);
            }

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

    // TODO remove, only for debugging
    public void executeScript(String script) {
        Platform.runLater(() -> {
            engine.executeScript(script);
        });
    }

    public void load(final String url) {
        Platform.runLater(() -> engine.load(url));
    }

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
