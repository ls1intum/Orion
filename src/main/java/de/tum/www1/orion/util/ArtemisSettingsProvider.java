package de.tum.www1.orion.util;

import com.intellij.openapi.components.ServiceManager;

import java.io.File;

public interface ArtemisSettingsProvider {
    void saveSetting(KEYS key, String setting);
    String getSetting(KEYS key);

    static ArtemisSettingsProvider getInstance() {
        return ServiceManager.getService(ArtemisSettingsProvider.class);
    }

    enum KEYS {
        ARTEMIS_URL("de.tum.www1.orion.settings.artemis.url", "https://artemis.ase.in.tum.de"),
        PROJECT_BASE_DIR("de.tum.www1.orion.settings.projects.path", System.getProperty("user.home") + File.separatorChar + "ArtemisProjects");

        private String keyValue;
        private String defaultValue;

        KEYS(String value, String defaultValue) {
            this.keyValue = value;
            this.defaultValue = defaultValue;
        }

        @Override
        public String toString() {
            return keyValue;
        }

        public String getDefaultValue() {
            return defaultValue;
        }
    }
}
