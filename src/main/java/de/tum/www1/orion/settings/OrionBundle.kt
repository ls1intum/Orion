package de.tum.www1.orion.settings;

import com.intellij.AbstractBundle;

import java.lang.ref.SoftReference;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.util.Objects;
import java.util.ResourceBundle;

public class OrionBundle {
    private static Reference<ResourceBundle> ourBundle;

    @NonNls
    private static final String BUNDLE = "i18n.OrionBundle";

    private OrionBundle() {
    }

    public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, @NotNull Object... params) {
        return AbstractBundle.message(getBundle(), key, params);
    }

    private static ResourceBundle getBundle() {
        var bundle = Objects.requireNonNull(new SoftReference<>(ourBundle).get()).get();
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(BUNDLE);
            ourBundle = new SoftReference<>(bundle);
        }

        return bundle;
    }
}
