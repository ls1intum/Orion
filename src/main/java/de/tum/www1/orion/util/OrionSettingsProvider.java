package de.tum.www1.orion.util;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;

import java.io.File;
import java.util.Map;

public interface OrionSettingsProvider {
    void saveSetting(Project project, KEYS key, String setting);
    void saveSettings(Project project, Map<KEYS, String> settings);
    String getSetting(KEYS key);
    boolean isModified(Map<KEYS, String> settings);

    static OrionSettingsProvider getInstance() {
        return ServiceManager.getService(OrionSettingsProvider.class);
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
