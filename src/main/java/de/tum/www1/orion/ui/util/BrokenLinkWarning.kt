package de.tum.www1.orion.ui.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.panel
import javax.swing.JComponent

class BrokenLinkWarning(project: Project?) : DialogWrapper(project) {

    init {
        title = "Unregistered Artemis Exercise"
        setOKButtonText("Yes")
        setCancelButtonText("No")
        init()
        isOKActionEnabled = true
    }

    override fun createCenterPanel(): JComponent? {
        return panel {
            row {
                label("It looks like this is an Artemis exercise, but has been moved to another directory.", bold = true)
            }
            row { label("Do you want to link this exercise to Artemis again?") }
        }
    }
}