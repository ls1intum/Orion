package de.tum.www1.orion.bridge.downcall;

import com.intellij.openapi.project.Project;
import de.tum.www1.orion.enumeration.ExerciseView;
import de.tum.www1.orion.messaging.OrionIntellijStateNotifier;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;

import java.util.LinkedList;
import java.util.Queue;

public class OrionJSDowncallBridge implements ArtemisJavascriptDowncallBridge {
    private final Project project;
    private boolean artemisLoaded;
    private WebEngine webEngine;
    /**
     * A queue used for storing jobs that should run as soon as the ArTEMiS webapp has been loaded. Until then, the tasks
     * are stored in this list.
     */
    private final Queue<Runnable> dispatchQueue;

    public OrionJSDowncallBridge(Project project) {
        this.project = project;
        this.dispatchQueue = new LinkedList<>();
        initStateListeners();
    }

    private void initStateListeners() {
        final var bus = project.getMessageBus().connect();
        bus.subscribe(OrionIntellijStateNotifier.INTELLIJ_STATE_TOPIC, new OrionIntellijStateNotifier() {
            @Override
            public void isCloning(boolean cloning) {
                executeJSFunction(JavascriptFunction.IS_CLONING, cloning);
            }

            @Override
            public void isBuilding(boolean building) {
                executeJSFunction(JavascriptFunction.IS_BUILDING, building);
            }

            @Override
            public void openedExercise(long opened, ExerciseView currentView) {
                executeJSFunction(JavascriptFunction.ON_EXERCISE_OPENED, opened, currentView);
            }

            @Override
            public void startedBuild(long courseId, long exerciseId) {
                executeJSFunction(JavascriptFunction.TRIGGER_BUILD_FROM_IDE, courseId, exerciseId);
            }
        });
    }

    @Override
    public void artemisLoadedWith(WebEngine engine) {
        artemisLoaded = true;
        webEngine = engine;
        dispatchQueue.forEach(Platform::runLater);
    }

    private void runAfterLoaded(final Runnable task) {
        if (!artemisLoaded) {
            dispatchQueue.add(task);
        } else {
            Platform.runLater(task);
        }
    }

    private void executeJSFunction(JavascriptFunction function, Object... args) {
        runAfterLoaded(() -> webEngine.executeScript(String.format(function.getHeader(), args)));
    }

    private enum JavascriptFunction {
        ON_EXERCISE_OPENED("onExerciseOpened(%d, '%s')"),
        IS_CLONING("isCloning(%b)"),
        IS_BUILDING("isBuilding(%b)"),
        TRIGGER_BUILD_FROM_IDE("startedBuildInIntelliJ(%d, %d)");

        private static final String DOWNCALL_BRIDGE = "window.javaDowncallBridge.";
        private String header;

        JavascriptFunction(String header) {
            this.header = header;
        }

        public java.lang.String getHeader() {
            return DOWNCALL_BRIDGE + header;
        }
    }
}
