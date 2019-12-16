package de.tum.www1.orion.tips;

import com.intellij.ide.util.TipAndTrickBean;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.util.Comparing;
import org.jetbrains.annotations.Nullable;

public class OrionTipAndTrickBean extends TipAndTrickBean {
    public static final ExtensionPointName<OrionTipAndTrickBean> EP_NAME = ExtensionPointName.create("de.tum.www1.orion.orionTipAndTrick");

    @Nullable
    public static OrionTipAndTrickBean findByFileName(String tipFileName) {
        for (OrionTipAndTrickBean tip : EP_NAME.getExtensionList()) {
            if (Comparing.equal(tipFileName, tip.fileName)) {
                return tip;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "OrionTipAndTrickBean{" +
                "fileName='" + fileName + '\'' +
                ", plugin='" + getPluginDescriptor().getPluginId() + '\'' +
                '}';
    }
}
