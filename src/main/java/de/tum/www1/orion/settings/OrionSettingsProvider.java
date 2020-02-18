package de.tum.www1.orion.settings;

import com.intellij.openapi.components.ServiceManager;
import javafx.application.Platform;
import javafx.scene.web.WebView;

import java.io.File;
import java.util.Map;

public interface OrionSettingsProvider {
    void saveSetting(KEYS key, String setting);
    void saveSettings(Map<KEYS, String> settings);
    String getSetting(KEYS key);
    boolean isModified(Map<KEYS, String> settings);

    static OrionSettingsProvider getInstance() {
        return ServiceManager.getService(OrionSettingsProvider.class);
    }

    static void initSettings() {
        KEYS.initSettings();
    }

    enum KEYS {
        ARTEMIS_URL("de.tum.www1.orion.settings.artemis.url", "https://artemis.ase.in.tum.de"),
        PROJECT_BASE_DIR("de.tum.www1.orion.settings.projects.path", System.getProperty("user.home") + File.separatorChar + "ArtemisProjects"),
        INSTRUCTOR_BASE_DIR("de.tum.www1.orion.settings.projects.instructor.path", System.getProperty("user.home") + File.separatorChar + "ArtemisProjects" + File.separatorChar + "Instructor"),
        USER_AGENT("de.tum.www1.orion.settings.userAgent", null);

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

        public static void initSettings() {
            try {
                Platform.startup(KEYS::initUserAgent);
            } catch (IllegalStateException e) {
                Platform.runLater(KEYS::initUserAgent);
            }
        }

        private static void initUserAgent() {
            KEYS.USER_AGENT.defaultValue = new WebView().getEngine().getUserAgent();
        }
    }
}
