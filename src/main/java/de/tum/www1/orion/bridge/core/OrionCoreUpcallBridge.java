package de.tum.www1.orion.bridge.core;

import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.application.ActionsKt;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import de.tum.www1.orion.dto.ProgrammingExercise;
import de.tum.www1.orion.enumeration.ExerciseView;
import de.tum.www1.orion.ui.util.ImportPathChooser;
import de.tum.www1.orion.util.JsonUtils;
import de.tum.www1.orion.util.UtilsKt;
import de.tum.www1.orion.util.registry.OrionGlobalExerciseRegistryService;
import de.tum.www1.orion.util.registry.OrionStudentExerciseRegistry;
import de.tum.www1.orion.vcs.OrionGitUtil;

public class OrionCoreUpcallBridge extends SimpleOrionUpcallBridge {
    public OrionCoreUpcallBridge(Project project) {
        super(project);
    }

    /**
     * Imports (clones) an exercises (all three base repositories: template, tests and solution) and creates a new
     * project containing those repos, allowing instructors to edit the whole exercise in one project.
     *
     * @param exerciseJson The exercise that should be imported formatted as a JSON string
     */
    void editExercise(String exerciseJson) {
    }

    /**
     * Clones the exercise participation repository and saves it under the artemis home directory
     *
     * @param repository The FQDN of the remote repository
     */
    void editExercise(String repository, String exerciseJson) {
        final var exercise = JsonUtils.INSTANCE.gson().fromJson(exerciseJson, ProgrammingExercise.class);
        final var registry = ServiceManager.getService(project, OrionStudentExerciseRegistry.class);
        if (!registry.alreadyImported(exercise.getId(), ExerciseView.STUDENT)) {
            ActionsKt.runInEdt(ModalityState.NON_MODAL, UtilsKt.ktLambda(() -> {
                final var chooser = new ImportPathChooser(project, exercise, ExerciseView.STUDENT);
                if (chooser.showAndGet()) {
                    final var path = chooser.getChosenPath();
                    OrionGitUtil.INSTANCE.cloneAndOpenExercise(project, repository, path, UtilsKt.ktLambda(() ->
                            registry.onNewExercise(exercise, ExerciseView.STUDENT, path)));
                }
            }));
        } else {
            final var exercisePath = ServiceManager.getService(OrionGlobalExerciseRegistryService.class).getPathForImportedExercise(exercise.getId(), ExerciseView.STUDENT);
            ApplicationManager.getApplication().invokeLater(() -> ProjectUtil.openOrImport(exercisePath, project, false));
        }
    }
}
