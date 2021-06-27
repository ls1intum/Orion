package de.tum.www1.orion.ui.grading

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerAdapter
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.VcsEditorTabFilesManager
import com.intellij.psi.PsiElement
import com.intellij.util.ui.codereview.diff.DiffEditorGutterIconRendererFactory
import com.intellij.util.ui.codereview.diff.EditorRangesController
import de.tum.www1.orion.ui.util.notify
import java.awt.event.MouseEvent

class GutterIcon {
    fun initialize(project: Project) {

    }
}
