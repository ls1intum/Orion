package de.tum.www1.orion.connector.instructor;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import de.tum.www1.orion.build.instructor.OrionInstructorBuildUtil;
import de.tum.www1.orion.connector.ArtemisConnector;
import de.tum.www1.orion.dto.RepositoryType;
import de.tum.www1.orion.util.registry.OrionInstructorExerciseRegistry;

public class OrionInstructorUpcallBridge extends ArtemisConnector implements ArtemisInstructorUpcallBridge {
    private final Project project;

    public OrionInstructorUpcallBridge(Project project) {
        this.project = project;
    }

    @Override
    public void selectRepository(String repository) {
        final var parsedRepo = RepositoryType.valueOf(repository);
        ServiceManager.getService(project, OrionInstructorExerciseRegistry.class).setSelectedRepository(parsedRepo);
    }

    @Override
    public void buildAndTestLocally() {
        ServiceManager.getService(project, OrionInstructorBuildUtil.class).runTestsLocally();
    }
}
