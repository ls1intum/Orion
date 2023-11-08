package de.tum.www1.orion.ui.assessment

import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.project.PossiblyDumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import de.tum.www1.orion.util.OrionAssessmentUtils

/**
 * An abstract super class that provides shared functionality
 */
abstract class OrionEditorProvider : FileEditorProvider, PossiblyDumbAware {

    // check if it is a file containing student code for the assessment
    override fun accept(project: Project, file: VirtualFile): Boolean {
        return file.fileSystem.getNioPath(file)?.startsWith(OrionAssessmentUtils.getAssignmentOf(project)) ?: false
    }

    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR
}