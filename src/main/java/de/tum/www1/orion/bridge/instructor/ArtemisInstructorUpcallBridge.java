package de.tum.www1.orion.bridge.instructor;

import de.tum.www1.orion.bridge.JavaUpcallBridge;

public interface ArtemisInstructorUpcallBridge extends JavaUpcallBridge {
    /**
     * Switches the focused repository for instructors. This is the repository that gets used when submitting or testing code
     *
     * @param repository The repository the instructor wants to focus on {@link de.tum.www1.orion.dto.RepositoryType}
     */
    void selectRepository(String repository);

    /**
     * This will build and test the focused repository locally using the language specific build/test agent
     */
    void buildAndTestLocally();
}
