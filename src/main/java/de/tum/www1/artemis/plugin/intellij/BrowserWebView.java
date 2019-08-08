package de.tum.www1.artemis.plugin.intellij;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import de.tum.www1.artemis.plugin.intellij.vcs.bridge.ArtemisJSBridge;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
    private String url;
    private JFXPanel browserPanel;

    public void init() {
        browserPanel = new JFXPanel();
        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            browser = new WebView();
            engine = browser.getEngine();

            engine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
                @Override
                public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State state, Worker.State t1) {
                    if (state == Worker.State.SUCCEEDED) {
                        final Project project = Objects.requireNonNull(DataManager.getInstance().getDataContext(browserPanel).getData(CommonDataKeys.PROJECT));
                        final JSObject window = (JSObject) engine.executeScript("window");
                        window.setMember("intellij", new ArtemisJSBridge(project));
                    }
                }
            });
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
