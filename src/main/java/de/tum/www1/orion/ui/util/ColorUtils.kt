package de.tum.www1.orion.ui.util

import com.intellij.ui.JBColor

/**
 * Provides color values for Dark and Light-mode in several places
 */
class ColorUtils {
    companion object {

        /**
         * Returns the jetbrains color values for dark and light mode for a feedback box
         * colors are the same as in Artemis
         */
        fun getFeedbackColor(value: Double): JBColor {

            // colors are the same as in Artemis
            return when {
                // JB Color for Light and Dark themes
                value > 0 -> JBColor(0xd4edda, 0xd4edda)
                value < 0 -> JBColor(0xf8d7da, 0xf8d7da)
                else -> JBColor(0xfff3cd, 0xfff3cd)
            }
        }

        /**
         * Returns the jetbrains color values for dark and light mode for a feedback text
         * colors are the same as in Artemis
         */
        fun getFeedbackTextColor(value: Double): JBColor {
            return when {
                value > 0 -> JBColor(0x186429, 0x186429)
                value < 0 -> JBColor(0x842029, 0x842029)
                else -> JBColor(0x664d03, 0x664d03)
            }
        }
    }
}