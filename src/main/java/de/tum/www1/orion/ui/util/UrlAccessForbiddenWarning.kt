package de.tum.www1.orion.ui.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.panel
import de.tum.www1.orion.util.translate
import javax.swing.Action
import javax.swing.JComponent

class UrlAccessForbiddenWarning(project: Project?) : DialogWrapper(project) {

    init {
        title = translate("orion.warning.accessforbidden")
        setCrossClosesWindow(false)
        init()
        isOKActionEnabled = true
    }

    override fun createActions(): Array<Action> {
        return arrayOf(okAction)
    }

    override fun createCenterPanel(): JComponent? {
        return panel {
            row {
                label(translate("orion.warning.accessforbidden.message"), bold = true)
            }
            row {
                label(translate("orion.warning.accessforbidden.backtoprevious"))
            }
        }
    }
}
