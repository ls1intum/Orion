package de.tum.www1.orion.ui.assessment

import com.intellij.collaboration.ui.codereview.diff.EditorComponentInlaysManager
import com.intellij.diff.util.FileEditorBase
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import de.tum.www1.orion.dto.AttachToType
import de.tum.www1.orion.dto.TodoReference
import de.tum.www1.orion.exercise.OrionAssessmentService
import de.tum.www1.orion.ui.comment.InlineAssessmentComment
import de.tum.www1.orion.ui.comment.InlineTodoComment
import de.tum.www1.orion.ui.util.StaticRegex.Companion.JAVA_METHOD_REGEX
import de.tum.www1.orion.util.OrionAssessmentUtils
import de.tum.www1.orion.util.OrionAssessmentUtils.createHeader
import de.tum.www1.orion.util.translate
import java.io.File
import javax.swing.JComponent
import javax.swing.JLabel

/**
 * FileEditor showing the contents of the given editor with the additional ability to show feedback comments and gutter icons to create them
 *
 * @property myEditor content to show, a read-only view of the student submission version of the file
 * @property relativePath of the opened file, relative to the assignment folder. Matches the way Artemis denotes files in feedback; passed over to any newly created feedback comment
 * @property file whose student submission is opened, see [getFile]
 */
class OrionAssessmentEditor(
    private var myEditor: Editor,
    private val relativePath: String,
    private val file: VirtualFile
) : FileEditorBase() {
    val headerLabel: JLabel = createHeader(translate("orion.exercise.assessmentModeLoading").uppercase())

    init {
        myEditor.headerComponent = headerLabel
        initializeFeedback()
    }

    override fun getComponent(): JComponent = myEditor.component

    override fun getName(): String = translate("orion.exercise.assessment")

    override fun getPreferredFocusedComponent(): JComponent? = null

    // needed to avoid deprecation warning; Should return the file for which the provider was called, note however this editor is showing a different file
    override fun getFile(): VirtualFile = file

    /**
     * Requests the [OrionAssessmentService] for feedback comments for the opened file.
     * If successful, adds the returned feedback comments as well as the gutter icons to create new comments to the editor
     * If not, does nothing. Relies on the [OrionAssessmentService] to be called again if feedback becomes available
     */
    fun initializeFeedback() {
        // request feedback, if not yet initialized, abort
        val feedback = myEditor.project?.service<OrionAssessmentService>()?.getFeedbackFor(relativePath) ?: return

        val editorImpl = myEditor as? EditorImpl ?: return
        // inlays manager that manages the inline comments
        val inlaysManager = EditorComponentInlaysManager(editorImpl)
        // add feedback
        feedback.forEach {
            InlineAssessmentComment(it, inlaysManager)
        }
        // add gutter icons
        OrionGutterIconController(relativePath, inlaysManager)

        // remove loading text
        headerLabel.text = translate("orion.exercise.assessmentMode").uppercase()

        val todos = myEditor.project?.service<OrionTodoProviderService>()?.getTodoForFile(relativePath)
        val lines =
            FileUtil.loadLines("${myEditor.project!!.basePath}${File.separatorChar}${OrionAssessmentUtils.TEMPLATE}${File.separatorChar}${relativePath}")

        var fileTodoText = ""

        if (todos == null) {
            return
        }
        for (todo in todos) {
            // file reference
            if (todo.attachToType == AttachToType.FILE) {
                fileTodoText = "${fileTodoText}${todo.todo}\n"
                continue
            }
            // find reference
            var foundLocation = false
            lines.forEachIndexed { index, line ->
                val lineWithoutComments = line.replace(Regex("//.*"), "")
                if (todo.attachToType == AttachToType.CLASS) {
                    if (lineWithoutComments.contains(" class ")) {
                        val elements = lineWithoutComments.split(" class ")
                        val name = elements[1]
                        if (name == todo.attachedTo) {
                            InlineTodoComment(TodoReference(index, todo.todo), inlaysManager)
                            foundLocation = true
                            return@forEachIndexed
                        }
                    }
                    if (lineWithoutComments.contains(" interface ")) {
                        val elements = lineWithoutComments.split(" interface ")
                        val name = elements[1]
                        if (name == todo.attachedTo) {
                            InlineTodoComment(TodoReference(index, todo.todo), inlaysManager)
                            foundLocation = true
                            return@forEachIndexed
                        }
                    }
                    if (lineWithoutComments.contains(" enum ")) {
                        val elements = lineWithoutComments.split(" enum ")
                        val name = elements[1]
                        if (name == todo.attachedTo) {
                            InlineTodoComment(TodoReference(index, todo.todo), inlaysManager)
                            foundLocation = true
                            return@forEachIndexed
                        }
                    }
                }
                // has to be method because file is already taken
                else if (lineWithoutComments.matches(JAVA_METHOD_REGEX)) {
                    val structureList = lineWithoutComments.split("(")
                    val methodSplit = structureList[0].trim().split(" ")
                    if (methodSplit[methodSplit.size - 1] == todo.attachedTo) {
                        InlineTodoComment(TodoReference(index, todo.todo), inlaysManager)
                        foundLocation = true
                        return@forEachIndexed
                    }
                }
            }
            // add to the file todolist
            if (!foundLocation) {
                fileTodoText = "${fileTodoText}${todo.todo}\n"
            }
        }
        if (fileTodoText.isNotEmpty()) {
            InlineTodoComment(TodoReference(0, fileTodoText.removeSuffix("\n")), inlaysManager)
        }

    }

    override fun dispose() {
        super.dispose()
        if (EditorFactory.getInstance().allEditors.contains(myEditor)) {
            EditorFactory.getInstance().releaseEditor(myEditor)
        }
    }
}
