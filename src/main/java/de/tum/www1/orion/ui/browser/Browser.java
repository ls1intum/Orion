package de.tum.www1.orion.ui.browser;

import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import de.tum.www1.orion.util.ProjectUtil;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

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
            final var project = ProjectUtil.Companion.newEmptyProject(1, 42, "IntelliJ Test");
            ProjectUtil.Companion.newModule(Objects.requireNonNull(project), "exercise");
            ProjectUtil.Companion.newModule(Objects.requireNonNull(project), "tests");
            ProjectUtil.Companion.newModule(Objects.requireNonNull(project), "solution");

            final var curr = Objects.requireNonNull(DataManager.getInstance().getDataContext(settingsButton).getData(CommonDataKeys.PROJECT));

//            OrionGitUtil.Companion.clone(curr, "http://localhost:7990/scm/testrmeutnbdf/testrmeutnbdf-exercise.git",
//                    project.getBasePath(), project.getBasePath() + "/exercise", null);
//            OrionGitUtil.Companion.clone(curr, "http://localhost:7990/scm/testrmeutnbdf/testrmeutnbdf-tests.git",
//                    project.getBasePath(), project.getBasePath() + "/tests", null);
//            OrionGitUtil.Companion.clone(curr, "http://localhost:7990/scm/testrmeutnbdf/testrmeutnbdf-solution.git",
//                    project.getBasePath(), project.getBasePath() + "/solution", null);
//
//            com.intellij.ide.impl.ProjectUtil.openOrImport(project.getBasePath(), ProjectManager.getInstance().getOpenProjects()[0], false);
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

            final var controllers = getControllers();
            add(controllers);

            browserView.init();
            final JComponent webPanel = browserView.getBrowser();
            add(webPanel);

            final GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.BOTH;
            constraints.gridheight = 0;
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
