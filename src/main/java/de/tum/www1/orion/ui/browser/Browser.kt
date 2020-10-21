package de.tum.www1.orion.ui.browser

import com.intellij.openapi.project.Project
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JPanel
import javax.swing.SwingUtilities

class Browser(val project: Project) : JPanel() {

    /**
     * Inits the web browser UI panel. It only contains the actual browser panel, which fills out the whole
     * tool window.
     */
    fun init() {
        val browserWebView = BrowserWebView(project)
        SwingUtilities.invokeLater {
            removeAll()
            val layout = GridBagLayout()
            setLayout(layout)
            val webPanel = browserWebView.jbCefBrowser.component
            add(webPanel)
            val constraints = GridBagConstraints()
            constraints.fill = GridBagConstraints.BOTH
            constraints.gridwidth = 0
            constraints.weightx = 1.0
            constraints.weighty = 1.0
            layout.setConstraints(webPanel, constraints)
            validate()
            repaint()
        }
    }
}