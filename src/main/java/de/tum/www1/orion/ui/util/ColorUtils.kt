package de.tum.www1.orion.ui.util

import com.intellij.ui.JBColor

class ColorUtils {
    companion object {
        fun getColor(value: Double): JBColor {

            // colors are the same as in Artemis
            return when {
                // JB Color for Light and Dark themes
                value > 0 -> JBColor(0xd4edda, 0xd4edda)
                value < 0 -> JBColor(0xf8d7da, 0xf8d7da)
                else -> JBColor(0xfff3cd, 0xfff3cd)
            }
        }

        fun getTextColor(value: Double): JBColor {
            return when {
                value > 0 -> JBColor(0x186429, 0x186429)
                value < 0 -> JBColor(0x842029, 0x842029)
                else -> JBColor(0x664d03, 0x664d03)
            }
        }
    }
}