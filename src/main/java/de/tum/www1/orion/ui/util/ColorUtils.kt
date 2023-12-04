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
                value > 0 -> JBColor(0xd4edda, 0x00231a)
                value < 0 -> JBColor(0xf8d7da, 0x370b07)
                else -> JBColor(0xfff3cd, 0x362203)
            }
        }

        /**
         * Returns the jetbrains color values for dark and light mode for a feedback text
         * colors are the same as in Artemis
         */
        fun getFeedbackTextColor(value: Double): JBColor {
            return when {
                value > 0 -> JBColor(0x186429, 0x8cb294)
                value < 0 -> JBColor(0x842029, 0xc29094)
                else -> JBColor(0x664d03, 0xb3a681)
            }
        }

        /**
         * Returns a color for todos
         *
         */
        fun getTodoColor(): JBColor {
            return JBColor(0x70d6e6, 0x001012)
        }

        /**
         * Returns a text color for todos
         *
         */
        fun getTodoTextColor(): JBColor {
            return JBColor(0x00000, 0xffffff)
        }
    }
}