package de.tum.www1.orion.ui.assessment

import com.intellij.icons.AllIcons
import de.tum.www1.orion.util.translate
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JButton

class StructuredGradingInstructionLink(private val assessmentComment: InlineAssessmentComment) {
    val component: JButton = object : JButton() {
        // enforce square shape
        override fun getPreferredSize(): Dimension {
            return Dimension(this.height, this.height)
        }
    }

    // the button is either showing the link or offering to delete it
    private var showMode: Boolean = true
        set(value) {
            field = value
            if (value) {
                component.icon = AllIcons.Toolwindows.ToolWindowModuleDependencies
                component.toolTipText =
                    translate("orion.exercise.assessment.sgi.link").format(assessmentComment.gradingInstruction?.instructionDescription)
            } else {
                component.icon = AllIcons.Actions.GC
                component.toolTipText = translate("orion.exercise.assessment.sgi.delete")
            }
        }

    // isEnabled of the underlying button exposed to the [InlineAssessmentComment]
    var isEnabled: Boolean
        get() = component.isEnabled
        set(value) {
            component.isEnabled = value
        }

    init {
        component.addActionListener {
            if (showMode) {
                showMode = false
            } else {
                assessmentComment.gradingInstruction = null
                updateGui()
            }
        }

        component.addMouseListener(object : MouseAdapter() {
            override fun mouseExited(e: MouseEvent?) {
                showMode = true
            }
        })

        updateGui()
    }

    fun updateGui() {
        showMode = true
        component.isVisible = assessmentComment.gradingInstruction != null
    }
}
