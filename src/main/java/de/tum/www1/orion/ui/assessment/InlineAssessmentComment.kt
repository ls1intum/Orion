package de.tum.www1.orion.ui.assessment

import java.awt.BorderLayout
import javax.swing.*

class InlineAssessmentComment(text: String, score: Int) {
    constructor() : this("Test", 5)

    var component: JComponent = JPanel()
    var textField: JTextField = JTextField()
    var spinner: JSpinner = JSpinner()

    init {
        textField.text = text
        spinner.model = SpinnerNumberModel(score, null, null, 0.5)

        component.layout = BorderLayout()
        component.add(textField, BorderLayout.CENTER)
        component.add(spinner, BorderLayout.EAST)
    }
}
