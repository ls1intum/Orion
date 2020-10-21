package de.tum.www1.orion.ui.util

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.io.FileUtil
import com.intellij.ui.layout.panel
import de.tum.www1.orion.dto.ProgrammingExercise
import de.tum.www1.orion.enumeration.ExerciseView
import de.tum.www1.orion.settings.OrionBundle
import de.tum.www1.orion.settings.OrionSettingsProvider
import de.tum.www1.orion.util.appService
import java.io.File
import javax.swing.JComponent
import javax.swing.JPanel

class ImportPathChooser(val project: Project, val exercise: ProgrammingExercise, val view: ExerciseView) : DialogWrapper(project) {
    private lateinit var pathChooserPanel: JPanel
    private lateinit var chosenPathField: TextFieldWithBrowseButton
    val chosenPath: String
        get() = chosenPathField.text

    init {
        title = translate("orion.dialog.pathchooser.title")
        init()
        isOKActionEnabled = true
    }

    override fun createCenterPanel(): JComponent? {
        pathChooserPanel = panel {
            row {
                label("Where do you want to save the imported exercise?")
            }
            row {
                chosenPathField = textFieldWithBrowseButton("Select a directory",
                        suggestImportPath(),
                        null,
                        FileChooserDescriptorFactory.createSingleFolderDescriptor()
                ) { it.path }.component
            }
        }

        return pathChooserPanel
    }

    private fun translate(key: String) = OrionBundle.message(key)

    private fun suggestImportPath(): String {
        val key = if (view == ExerciseView.STUDENT) OrionSettingsProvider.KEYS.PROJECT_BASE_DIR else OrionSettingsProvider.KEYS.INSTRUCTOR_BASE_DIR
        val baseDir = appService(OrionSettingsProvider::class.java).getSetting(key)
        val sanitizedCourseTitle = FileUtil.sanitizeFileName(exercise.course.title, false, "")
        val sanitizedExerciseTitle = FileUtil.sanitizeFileName(exercise.title, false, "")

        return baseDir + File.separatorChar + sanitizedCourseTitle + File.separatorChar + sanitizedExerciseTitle
    }
}
