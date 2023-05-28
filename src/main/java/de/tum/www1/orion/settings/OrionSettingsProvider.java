package de.tum.www1.orion.settings;

import java.io.File;
import java.util.Map;

public interface OrionSettingsProvider {
    void saveSetting(KEYS key, String setting);
    void saveSettings(Map<KEYS, String> settings);
    String getSetting(KEYS key);
    boolean isModified(Map<KEYS, String> settings);

    enum KEYS {
        ARTEMIS_URL("de.tum.www1.orion.settings.artemis.url", "https://artemis.ase.in.tum.de/"),
        PROJECT_BASE_DIR("de.tum.www1.orion.settings.projects.path", System.getProperty("user.home") + File.separatorChar + "ArtemisProjects"),
        INSTRUCTOR_BASE_DIR("de.tum.www1.orion.settings.projects.instructor.path", System.getProperty("user.home") + File.separatorChar + "ArtemisProjects" + File.separatorChar + "Instructor"),
        TUTOR_BASE_DIR("de.tum.www1.orion.settings.projects.tutor.path", System.getProperty("user.home") + File.separatorChar + "ArtemisProjects" + File.separatorChar + "Tutor"),
        USER_AGENT("de.tum.www1.orion.settings.userAgent", "Mozilla/5.0 (HTML, like Gecko)"),
        COMMIT_MESSAGE("de.tum.www1.orion.settings.commit.message", "Automated commit by Orion"),
        USE_DEFAULT("de.tum.www1.orion.settings.use.default", Boolean.toString(false));

        private final String keyValue;
        private final String defaultValue;

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
