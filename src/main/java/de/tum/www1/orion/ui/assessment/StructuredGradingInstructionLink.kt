package de.tum.www1.orion.ui.assessment

import com.google.gson.JsonSyntaxException
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.ui.EditorTextField
import de.tum.www1.orion.dto.GradingInstruction
import de.tum.www1.orion.util.JsonUtils
import de.tum.www1.orion.util.translate
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JSpinner

/**
 * Provides a clickable link which informs the user about any used structured grading instruction. If clicked, it allows to delete the link.
 * Also manages the label with the grading instruction feedback text.
 * From the point of the [InlineAssessmentComment] is behaves like the spinner for the score or the text field for the text, but for the grading instruction
 *
 * @property instructionLabel to display the instruction feedback in
 * @property score to set when applying a new instruction
 * @param textField to register the drop listener in
 */
class StructuredGradingInstructionLink(
    private val instructionLabel: JLabel,
    private val score: JSpinner,
    textField: EditorTextField,
) {
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
                    translate("orion.exercise.assessment.sgi.link").format(gradingInstruction?.instructionDescription)
            } else {
                component.icon = AllIcons.Actions.GC
                component.toolTipText = translate("orion.exercise.assessment.sgi.delete")
            }
        }

    var gradingInstruction: GradingInstruction? = null
        set(value) {
            field = value
            updateGui()
        }

    // isEnabled of the underlying button exposed to the [InlineAssessmentComment]
    var isEnabled: Boolean
        get() = component.isEnabled
        set(value) {
            component.isEnabled = value
        }

    init {
        // listener to handle dropping of structured grading instructions
        // for any inserted string it tries to parse it as a sgi. If it fails, nothing is done. If it succeeds, the sgi is handled
        textField.addDocumentListener(object : DocumentListener {
            override fun beforeDocumentChange(event: DocumentEvent) {
                val newFragment = event.newFragment
                // basic performance improvement, don't try to parse if its hopeless
                if (newFragment.startsWith('{') && newFragment.endsWith('}')) {
                    try {
                        val instruction =
                            JsonUtils.gson().fromJson(newFragment.toString(), GradingInstruction::class.java)
                        gradingInstruction = instruction
                        // revert insertion
                        val text = event.document.text
                        // listeners should not change the text, queue the change instead
                        ApplicationManager.getApplication().invokeLater {
                            WriteAction.run<Throwable> {
                                event.document.setText(text)
                            }
                        }
                    } catch (e: JsonSyntaxException) {
                        // ignore, if the parse failed, nothing needs to be done
                    }
                }
            }
        })

        component.addActionListener {
            if (showMode) {
                showMode = false
            } else {
                gradingInstruction = null
                updateGui()
            }
        }

        component.addMouseListener(object : MouseAdapter() {
            override fun mouseExited(e: MouseEvent?) {
                showMode = true
            }
        })
    }

    /**
     * Update the visibility and icon, has to be called after changing the grading instruction
     */
    private fun updateGui() {
        component.isVisible = gradingInstruction != null
        instructionLabel.isVisible = gradingInstruction != null
        score.isEnabled = gradingInstruction == null

        gradingInstruction?.let {
            score.value = it.credits
            showMode = true
            // wrap in html to get linebreaks
            instructionLabel.text = "<html>${it.feedback}</html>"
        }
    }
}
