package de.tum.www1.orion.connector.ide.vcs;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;

public interface IOrionVCSConnector {

    void submit();

    /**
     * Switches the focused repository for instructors. This is the repository that gets used when submitting or testing code
     *
     * @param repository The repository the instructor wants to focus on {@link de.tum.www1.orion.dto.RepositoryType}
     */
    void selectRepository(String repository);

    static IOrionVCSConnector getInstance(Project project) {
        return ServiceManager.getService(project, IOrionVCSConnector.class);
    }
}
