package de.tum.www1.orion.connector.client;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import javafx.scene.web.WebEngine;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface JavaScriptConnector {
    /**
     * Notifies the JavaScript connector, that all web content has been loaded. This is used to trigger all remaining
     * calls to the web client, which were queued because Artemis has not fully been loaded, yet.
     *
     * @param engine The web engine used for loading the Artemis webapp.
     */
    void artemisLoadedWith(WebEngine engine);

    /**
     * Initializes all listeners to the internal IDE/Orion states, that should get propagated to the client. Inlcuding
     * e.g. ongoing submits, clone processes, commits, etc.
     */
    void initIDEStateListeners();

    static JavaScriptConnector getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, JavaScriptConnector.class);
    }

    enum JavaScriptFunction {
        ON_EXERCISE_OPENED("onExerciseOpened", Integer.class, String.class),
        IS_CLONING("isCloning", Boolean.class),
        IS_BUILDING("isBuilding", Boolean.class),
        TRIGGER_BUILD_FROM_IDE("startedBuildInOrion", Integer.class, Integer.class);

        private static final String ARTEMIS_CLIENT_CONNECTOR = "window.artemisClientConnector.";
        private String name;
        private List<Class> argTypes;

        JavaScriptFunction(String name, Class... argTypes) {
            this.name = name;
            this.argTypes = Arrays.asList(argTypes);
        }

        private boolean areArgumentsValid(Object... args) {
            return Arrays.stream(args).allMatch(arg -> argTypes.contains(arg.getClass()));
        }

        public void execute(WebEngine engine, Object... args) {
            if (!areArgumentsValid(args)) {
                throw new IllegalArgumentException("JS function " + name + " called with the wrong arument types!");
            }

            final var params = Arrays.stream(args).map(arg -> arg.toString()).collect(Collectors.joining(",", "(", ")"));
            engine.executeScript(name + params);
        }
    }
}
