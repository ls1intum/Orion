package de.tum.www1.orion.ui.comment

import com.intellij.collaboration.ui.codereview.diff.EditorComponentInlaysManager
import com.intellij.openapi.fileTypes.FileTypes
import com.intellij.ui.EditorTextField
import de.tum.www1.orion.dto.TodoReference
import de.tum.www1.orion.ui.util.ColorUtils
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.border.TitledBorder


/**
 * A non modifiable Comment for mentioning todos even if the student has removed todos from the template.
 */
class InlineTodoComment(
    todo: TodoReference,
    inlaysManager: EditorComponentInlaysManager
) : InlineComment(inlaysManager) {

    init {
        textField = EditorTextField(todo.todoText, project, FileTypes.PLAIN_TEXT)
        textField.isEnabled = false
        textField.setOneLineMode(false)
        textField.border = null
        textField.background = ColorUtils.getTodoColor()

        val textPanel = JPanel()
        textPanel.border = BorderFactory.createTitledBorder(
            BorderFactory.createEmptyBorder(25, 15, 5, 15),
            "TODO"
        )

        textPanel.layout = BorderLayout()
        textPanel.add(textField, BorderLayout.CENTER)

        component.layout = BorderLayout()
        component.add(textPanel, BorderLayout.CENTER)

        coloredBackgroundComponentList =
            listOf(
                component,
                textPanel,
            )
        coloredForegroundComponentList = listOf(textPanel.border)

        updateColor()
        disposer = try {
            inlaysManager.insertAfter(todo.line, component)
        } catch (e: IndexOutOfBoundsException) {
            inlaysManager.insertAfter(0, component)
        }
    }

    private fun updateColor() {
        coloredBackgroundComponentList.forEach {
            it.background = ColorUtils.getTodoColor()
        }
        coloredForegroundComponentList.forEach {
            (it as? TitledBorder)?.titleColor = ColorUtils.getTodoTextColor()
            (it as? JComponent)?.foreground = ColorUtils.getTodoTextColor()
        }
    }
}