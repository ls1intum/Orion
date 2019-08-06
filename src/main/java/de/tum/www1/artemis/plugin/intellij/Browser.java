package de.tum.www1.artemis.plugin.intellij;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.checkout.GitCheckoutProvider;
import git4idea.commands.Git;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Objects;

public class Browser extends JPanel {
    private BrowserWebView browserView;
    private JTextField urlField;

    private JPanel getControllers() {
        JPanel controllers = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        controllers.setLayout(layout);
        urlField = new JTextField();
        JButton buttonClone = new JButton("Clone");
        buttonClone.setPreferredSize(new Dimension(40, 30));
        JButton buttonReload = new JButton("≈");
        buttonReload.setPreferredSize(new Dimension(40, 30));
        controllers.add(urlField);
        controllers.add(buttonClone);
        controllers.add(buttonReload);
        GridBagConstraints s = new GridBagConstraints();
        s.fill = GridBagConstraints.BOTH;
        s.gridwidth = 1;
        s.weightx = 0;
        s.weighty = 0;
        layout.setConstraints(buttonClone, s);
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

        buttonClone.addActionListener(even -> {
            String repository = "https://repobruegge.in.tum.de/scm/alxtstabc/alxtstabc-exercise.git";
            Project project = Objects.requireNonNull(DataManager.getInstance().getDataContext(buttonClone).getData(CommonDataKeys.PROJECT));
            LocalFileSystem lfs = LocalFileSystem.getInstance();
            File parentFile = new File(project.getBasePath());
            VirtualFile parent = lfs.findFileByIoFile(parentFile);
            String directory = "testClone";
            GitCheckoutProvider.Listener listener = ProjectLevelVcsManager.getInstance(project).getCompositeCheckoutListener();

            GitCheckoutProvider.clone(project, Git.getInstance(), listener, parent, repository, directory, project.getBasePath());
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
