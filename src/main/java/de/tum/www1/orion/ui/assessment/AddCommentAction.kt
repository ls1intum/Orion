package de.tum.www1.orion.ui.assessment

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.util.ui.codereview.diff.EditorComponentInlaysManager
import javax.swing.JLabel

class AddCommentAction(private val inlaysManager: EditorComponentInlaysManager, private val line: Int): AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        inlaysManager.insertAfter(line, InlineAssessmentComment().component)
    }
}
