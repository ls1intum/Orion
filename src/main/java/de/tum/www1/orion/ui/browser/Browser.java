package de.tum.www1.orion.ui.browser;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.components.ServiceManager;
import de.tum.www1.orion.ui.settings.ArtemisSettingsDialog;
import de.tum.www1.orion.util.ArtemisSettingsProvider;

import javax.swing.*;
import java.awt.*;

public class Browser extends JPanel {
    private static final Browser INSTANCE = new Browser();

    private BrowserWebView browserView;

    private Browser() {}

    public static Browser getInstance() {
        return INSTANCE;
    }

    private JPanel getControllers() {
        JPanel controllers = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        controllers.setLayout(layout);

        final JButton settingsButton = new JButton(AllIcons.Toolwindows.ToolWindowHierarchy);
        add(settingsButton);

        GridBagConstraints s = new GridBagConstraints();
        s.fill = GridBagConstraints.BOTH;
        s.gridwidth = 1;
        s.weightx = 1;
        s.weighty = 0;

        settingsButton.addActionListener(event -> {
            final ArtemisSettingsDialog settingsDialog = new ArtemisSettingsDialog();
            if (settingsDialog.showAndGet()) {
                final ArtemisSettingsProvider settings = ServiceManager.getService(ArtemisSettingsProvider.class);
                settings.saveSetting(ArtemisSettingsProvider.KEYS.ARTEMIS_URL, settingsDialog.getArtemisUrl());
                settings.saveSetting(ArtemisSettingsProvider.KEYS.PROJECT_BASE_DIR, settingsDialog.getProjectPath());
                init();
            }
        });

        return controllers;
    }

    /**
     * Inits the web browser UI panel. It only contains the actual browser panel, which fills out the whole
     * tool window.
     */
    public void init() {
        browserView = new BrowserWebView();
        SwingUtilities.invokeLater(() -> {
            removeAll();
            final GridBagLayout layout = new GridBagLayout();
            setLayout(layout);

            final JComponent controllers = getControllers();
            add(controllers);

            browserView.init();
            final JComponent webPanel = browserView.getBrowser();
            add(webPanel);

            final GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.BOTH;
            constraints.gridwidth = 0;
            constraints.weightx = 1;
            constraints.weighty = 0;
            layout.setConstraints(controllers, constraints);
            constraints.gridwidth = 0;
            constraints.weightx = 1;
            constraints.weighty = 1;
            layout.setConstraints(webPanel, constraints);

            validate();
            repaint();
        });
    }
}
