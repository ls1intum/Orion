package de.tum.www1.orion.settings

import com.intellij.AbstractBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.lang.ref.Reference
import java.lang.ref.SoftReference
import java.util.*

/**
 * A resource bundle to manage translations
 */
object OrionBundle {
    private var ourBundle: Reference<ResourceBundle>? = null
    private const val BUNDLE: @NonNls String = "i18n.OrionBundle"

    /**
     * Requests a key from the bundle.
     * @param key  The unique key to request from a resource
     */
    fun message(key: @PropertyKey(resourceBundle = BUNDLE) String, vararg params: Any?): String {
        return AbstractBundle.message(bundle!!, key, *params)
    }

    private val bundle: ResourceBundle?
        get() {
            // Extract ourBundle
            val bundleReference = ourBundle?.get()
            return if (bundleReference == null) {
                val bundle = ResourceBundle.getBundle(BUNDLE)
                ourBundle = SoftReference(bundle)
                bundle
            } else {
                bundleReference
            }
        }
}
