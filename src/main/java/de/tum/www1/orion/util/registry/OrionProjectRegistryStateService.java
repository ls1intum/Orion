package de.tum.www1.orion.util.registry;

import com.intellij.openapi.application.ActionsKt;
import com.intellij.openapi.components.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.jetbrains.python.sdk.PythonSdkType;
import de.tum.www1.orion.dto.RepositoryType;
import de.tum.www1.orion.enumeration.ExerciseView;
import de.tum.www1.orion.enumeration.ProgrammingLanguage;
import de.tum.www1.orion.util.JsonUtilsKt;
import de.tum.www1.orion.util.OrionFileUtils;
import de.tum.www1.orion.util.UtilsKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@State(name = "orionRegistry", storages = @Storage(StoragePathMacros.WORKSPACE_FILE))
public class OrionProjectRegistryStateService implements PersistentStateComponent<OrionProjectRegistryStateService.State> {
    private static final Logger log = Logger.getInstance(OrionProjectRegistryStateService.class);
    private State myState;
    private Project myProject;

    public OrionProjectRegistryStateService(Project project) {
        this.myProject = project;
    }

    public static OrionProjectRegistryStateService getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, OrionProjectRegistryStateService.class);
    }

    public static class State {
        public long courseId;
        public String courseTitle;
        public long exerciseId;
        public String exerciseTitle;
        public RepositoryType selectedRepository;
        public ExerciseView view;
        public ProgrammingLanguage language;
    }

    @Nullable
    @Override
    public State getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        myState = state;
    }

    public void importIfPending() {
        final var pendingImportFile = VfsUtil.findRelativeFile(OrionFileUtils.INSTANCE.getRoot(myProject), ".artemisExercise.json");
        if (pendingImportFile != null) {
            try {
                final var imported = JsonUtilsKt.mapper().readValue(pendingImportFile.getInputStream(), ImportedExercise.class);
                myState = new State();
                myState.courseId = imported.getCourseId();
                myState.exerciseId = imported.getExerciseId();
                myState.courseTitle = imported.getCourseTitle();
                myState.exerciseTitle = imported.getExerciseTitle();
                myState.language = imported.getLanguage();
                myState.view = imported.getView();
                if (myState.view == ExerciseView.INSTRUCTOR) {
                    guessProjectSdk();
                    myState.selectedRepository = RepositoryType.TEST;  // init
                }

                ActionsKt.runWriteAction(() -> {
                    try {
                        pendingImportFile.delete(this);
                    } catch (IOException e) {
                        log.error(e.getMessage(),  e);
                    }

                    return null;
                });
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void guessProjectSdk() {
        final List<Sdk> availableSdks;
        switch (myState.language) {
            case JAVA:
                availableSdks = List.of(ProjectJdkTable.getInstance().getAllJdks());
                break;
            case PYTHON:
                availableSdks = PythonSdkType.getAllSdks();
                break;
            default:
                throw new IllegalArgumentException("Programming language " + myState.language + " is not supported yet!");
        }

        final var bestFit = availableSdks.stream().max(Comparator.comparing(sdk -> Objects.requireNonNull(sdk.getVersionString())));
        ActionsKt.runWriteAction(UtilsKt.ktLambda(() ->
                bestFit.ifPresent(sdk -> ProjectRootManager.getInstance(myProject).setProjectSdk(sdk))));
    }

    public boolean isArtemisExercise() {
        return myState != null;
    }

    public ExerciseView getCurrentView() {
        return myState.view;
    }
}
