package de.tum.www1.orion.ui.comment

import com.intellij.collaboration.ui.codereview.diff.EditorComponentInlaysManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.ui.EditorTextField
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * A Super class to hold some basic shared values of a comment object
 */
abstract class InlineComment(
    inlaysManager: EditorComponentInlaysManager

) {
    var disposer: Disposable? = null
    val project: Project
    lateinit var coloredBackgroundComponentList: List<JComponent>
    lateinit var coloredForegroundComponentList: List<Any>
    val component: JComponent = JPanel()
    lateinit var textField: EditorTextField

    init {
        project = inlaysManager.editor.project!!
    }
}