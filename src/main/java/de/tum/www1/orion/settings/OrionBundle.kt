package de.tum.www1.orion.settings

import com.intellij.AbstractBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.lang.ref.Reference
import java.lang.ref.SoftReference
import java.util.*

object OrionBundle {
    private var ourBundle: Reference<ResourceBundle>? = null
    private const val BUNDLE: @NonNls String = "i18n.OrionBundle"
    fun message(key: @PropertyKey(resourceBundle = BUNDLE) String, vararg params: Any?): String {
        return AbstractBundle.message(bundle!!, key, *params)
    }

    private val bundle: ResourceBundle?
        get() {
            val bundleReference = SoftReference(ourBundle).get()
            return if (bundleReference == null) {
                val bundle = ResourceBundle.getBundle(BUNDLE)
                ourBundle = SoftReference(bundle)
                bundle
            } else {
                bundleReference.get()
            }
        }
}
