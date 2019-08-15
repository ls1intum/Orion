package de.tum.www1.artemis.plugin.intellij;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import de.tum.www1.artemis.plugin.intellij.vcs.ArtemisGitUtil;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Objects;

public class Browser extends JPanel {
    private BrowserWebView browserView;
    private JTextField urlField;

    private JPanel getControllers() {
        JPanel controllers = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        controllers.setLayout(layout);
        urlField = new JTextField();
        JButton buttonReload = new JButton("#");
        buttonReload.setPreferredSize(new Dimension(40, 30));
        controllers.add(urlField);
        controllers.add(buttonReload);
        GridBagConstraints s = new GridBagConstraints();
        s.fill = GridBagConstraints.BOTH;
        s.gridwidth = 5;
        s.weightx = 1;
        s.weighty = 0;
        layout.setConstraints(urlField, s);
        s.gridwidth = 1;
        s.weightx = 0;
        s.weighty = 0;
        layout.setConstraints(buttonReload, s);

        urlField.addActionListener(event -> {
            String trim = urlField.getText().trim();
            if (!trim.startsWith("http")) {
                trim = "http://" + trim;
            }
            browserView.load(trim);
        });

        buttonReload.addActionListener(event -> {
            init();
        });

        return controllers;
    }

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
            constraints.weightx = 0;
            constraints.weighty = 1;
            layout.setConstraints(webPanel, constraints);

            validate();
            repaint();
        });
    }
}
