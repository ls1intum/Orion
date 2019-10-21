package de.tum.www1.orion.build

import com.intellij.openapi.options.SettingsEditor
import com.intellij.ui.layout.panel
import javax.swing.JComponent
import javax.swing.JPanel

class OrionSettingsEditor : SettingsEditor<OrionRunConfiguration>() {
    private lateinit var panel: JPanel

    override fun resetEditorFrom(s: OrionRunConfiguration) {
    }

    override fun createEditor(): JComponent {
        panel = panel {
            row {
                label("Test")
            }
        }

        return panel
    }

    override fun applyEditorTo(s: OrionRunConfiguration) {
    }
}