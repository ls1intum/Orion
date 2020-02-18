package de.tum.www1.orion.connector.core;

import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.application.ActionsKt;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import de.tum.www1.orion.connector.submit.ChangeSubmissionContext;
import de.tum.www1.orion.dto.ProgrammingExercise;
import de.tum.www1.orion.enumeration.ExerciseView;
import de.tum.www1.orion.messaging.OrionIntellijStateNotifier;
import de.tum.www1.orion.ui.util.ImportPathChooser;
import de.tum.www1.orion.util.JsonUtils;
import de.tum.www1.orion.util.OrionProjectUtil;
import de.tum.www1.orion.util.UtilsKt;
import de.tum.www1.orion.util.project.OrionJavaInstructorProjectCreator;
import de.tum.www1.orion.util.registry.OrionGlobalExerciseRegistryService;
import de.tum.www1.orion.util.registry.OrionInstructorExerciseRegistry;
import de.tum.www1.orion.util.registry.OrionStudentExerciseRegistry;
import de.tum.www1.orion.vcs.OrionGitUtil;

import java.io.File;
import java.io.IOException;

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
    public void editExercise(String exerciseJson) {
        final var exercise = JsonUtils.INSTANCE.gson().fromJson(exerciseJson, ProgrammingExercise.class);
        final var registry = ServiceManager.getService(project, OrionInstructorExerciseRegistry.class);
        if (!registry.alreadyImported(exercise.getId(), ExerciseView.INSTRUCTOR)) {
            ActionsKt.runInEdt(ModalityState.NON_MODAL, UtilsKt.ktLambda(() -> {
                final var chooser = new ImportPathChooser(project, exercise, ExerciseView.INSTRUCTOR);
                if (chooser.showAndGet()) {
                    final var path = chooser.getChosenPath();
                    try {
                        FileUtil.ensureExists(new File(path));
                        // Create a new empty project
                        final var newProject = OrionProjectUtil.INSTANCE.newEmptyProject(exercise.getTitle(), path);
                        // Clone all base repositories
                        OrionGitUtil.INSTANCE.clone(project, exercise.getTemplateParticipation().getRepositoryUrl().toString(),
                                newProject.getBasePath(), newProject.getBasePath() + "/exercise", null);
                        OrionGitUtil.INSTANCE.clone(project, exercise.getTestRepositoryUrl().toString(),
                                newProject.getBasePath(), newProject.getBasePath() + "/tests", null);
                        OrionGitUtil.INSTANCE.clone(project, exercise.getSolutionParticipation().getRepositoryUrl().toString(),
                                newProject.getBasePath(), newProject.getBasePath() + "/solution", UtilsKt.ktLambda(() -> {
                                    // After cloning all repos, create the necessary project files and notify the webview about the opened project
                                    OrionJavaInstructorProjectCreator.INSTANCE.prepareProjectForImport(new File(newProject.getBasePath()));
                                    registry.onNewExercise(exercise, ExerciseView.INSTRUCTOR, path);
                                    ProjectUtil.openOrImport(newProject.getBasePath(), project, false);
                                }));
                    } catch (IOException e) {
                        LOG.error(e.getMessage(), e);
                    }
                } else {
                    project.getMessageBus().syncPublisher(OrionIntellijStateNotifier.INTELLIJ_STATE_TOPIC).isCloning(false);
                }
            }));
        } else {
            // Exercise is already imported
            project.getMessageBus().syncPublisher(OrionIntellijStateNotifier.INTELLIJ_STATE_TOPIC).isCloning(false);
            final var exercisePath = ServiceManager.getService(OrionGlobalExerciseRegistryService.class).getPathForImportedExercise(exercise.getId(), ExerciseView.INSTRUCTOR);
            ApplicationManager.getApplication().invokeLater(() -> ProjectUtil.openOrImport(exercisePath, project, false));
        }
    }

    /**
     * Clones the exercise participation repository and saves it under the artemis home directory
     *
     * @param repository The FQDN of the remote repository
     */
    public void importParticipation(String repository, String exerciseJson) {
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

    public void submitChanges() {
        ServiceManager.getService(project, ChangeSubmissionContext.class).submitChanges();
    }
}
