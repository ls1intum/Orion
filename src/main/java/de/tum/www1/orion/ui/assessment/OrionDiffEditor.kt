package de.tum.www1.orion.ui.assessment

import com.intellij.codeHighlighting.BackgroundEditorHighlighter
import com.intellij.diff.editor.DiffRequestProcessorEditor
import com.intellij.diff.editor.DiffVirtualFile
import com.intellij.ide.structureView.StructureViewBuilder
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.FileEditorStateLevel
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import de.tum.www1.orion.util.translate
import java.beans.PropertyChangeListener
import javax.swing.JComponent

/**
 * FileEditor showing the contents of the given editor with the additional ability to show feedback comments and gutter icons to create them
 *
 * @property myEditor content to show, a read-only view of the student submission version of the file
 * @property relativePath of the opened file, relative to the assignment folder. Matches the way Artemis denotes files in feedback; passed over to any newly created feedback comment
 * @property file whose student submission is opened, see [getFile]
 */
class OrionDiffEditor(
    private var myEditor: DiffRequestProcessorEditor,
    private val editorName: String
) : DiffRequestProcessorEditor(myEditor.file as DiffVirtualFile, myEditor.processor) {

    override fun getName(): String = translate(editorName)

    override fun deselectNotify() {
        myEditor.deselectNotify()
    }

    override fun isDisposed(): Boolean {
        return myEditor.isDisposed()
    }

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {
        myEditor.addPropertyChangeListener(listener)
    }

    override fun copyCopyableDataTo(clone: UserDataHolderBase) {
        myEditor.copyCopyableDataTo(clone)
    }

    override fun copyUserDataTo(other: UserDataHolderBase) {
        myEditor.copyUserDataTo(other)
    }

    override fun dispose() {
        myEditor.dispose()
    }

    override fun getBackgroundHighlighter(): BackgroundEditorHighlighter? {
        return myEditor.getBackgroundHighlighter()
    }

    override fun getComponent(): JComponent {
        return myEditor.getComponent()
    }

    override fun <T : Any?> getCopyableUserData(key: Key<T>): T {
        return myEditor.getCopyableUserData(key)
    }

    override fun getCurrentLocation(): FileEditorLocation? {
        return myEditor.getCurrentLocation()
    }

    override fun getFile(): VirtualFile {
        return myEditor.getFile()
    }

    override fun getFilesToRefresh(): List<VirtualFile> {
        return myEditor.getFilesToRefresh()
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return myEditor.getPreferredFocusedComponent()
    }

    override fun getState(level: FileEditorStateLevel): FileEditorState {
        return myEditor.getState(level)
    }

    override fun getStructureViewBuilder(): StructureViewBuilder? {
        return myEditor.getStructureViewBuilder()
    }

    override fun getTabActions(): ActionGroup? {
        return myEditor.getTabActions()
    }

    override fun <T : Any?> getUserData(key: Key<T>): T? {
        return myEditor.getUserData(key)
    }

    override fun getUserDataString(): String {
        return myEditor.getUserDataString()
    }

    override fun isModified(): Boolean {
        return myEditor.isModified()
    }

    override fun isUserDataEmpty(): Boolean {
        return myEditor.isUserDataEmpty()
    }

    override fun isValid(): Boolean {
        return myEditor.isValid()
    }

    override fun <T : Any?> putCopyableUserData(key: Key<T>, value: T) {
        myEditor.putCopyableUserData(key, value)
    }

    override fun <T : Any?> putUserData(key: Key<T>, value: T?) {
        myEditor.putUserData(key, value)
    }

    override fun <T : Any?> putUserDataIfAbsent(key: Key<T>, value: T & Any): T & Any {
        return myEditor.putUserDataIfAbsent(key, value)
    }

    override fun <T : Any?> replace(key: Key<T>, oldValue: T?, newValue: T?): Boolean {
        return myEditor.replace(key, oldValue, newValue)
    }

    override fun selectNotify() {
        myEditor.selectNotify()
    }

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {
        myEditor.removePropertyChangeListener(listener)
    }

    override fun setState(state: FileEditorState) {
        myEditor.setState(state)
    }

    override fun setState(state: FileEditorState, exactState: Boolean) {
        myEditor.setState(state, exactState)
    }

    override fun toString(): String {
        return myEditor.toString()
    }
}
