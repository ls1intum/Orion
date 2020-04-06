package de.tum.www1.orion.connector.client;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import de.tum.www1.orion.enumeration.ExerciseView;
import javafx.scene.web.WebEngine;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface JavaScriptConnector {

    /**
     * Initializes all listeners to the internal IDE/Orion states, that should get propagated to the client. Inlcuding
     * e.g. ongoing submits, clone processes, commits, etc.
     */
    void initIDEStateListeners();

    static JavaScriptConnector getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, JavaScriptConnector.class);
    }

    enum JavaScriptFunction {
        ON_EXERCISE_OPENED("onExerciseOpened", Long.class, ExerciseView.class),
        IS_CLONING("isCloning", Boolean.class),
        IS_BUILDING("isBuilding", Boolean.class),
        TRIGGER_BUILD_FROM_IDE("startedBuildInOrion", Long.class, Long.class);

        private static final String ARTEMIS_CLIENT_CONNECTOR = "window.artemisClientConnector.";
        private String name;
        private List<Class> argTypes;

        JavaScriptFunction(String name, Class... argTypes) {
            this.name = name;
            this.argTypes = Arrays.asList(argTypes);
        }

        private boolean areArgumentsValid(Object... args) {
            if (args.length != this.argTypes.size()) {
                return false;
            }

            for (int i = 0; i < args.length; i++) {
                if (args[i].getClass() != argTypes.get(i)) {
                    return false;
                }
            }

            return true;
        }

        public void execute(WebEngine engine, Object... args) {
            if (!areArgumentsValid(args)) {
                throw new IllegalArgumentException("JS function " + name + " called with the wrong argument types!");
            }

            final var params = Arrays.stream(args)
                    .map(arg -> {
                        if (arg.getClass() == String.class || arg.getClass().isEnum()) {
                            return "'" + arg + "'";
                        }

                        return arg.toString();
                    })
                    .collect(Collectors.joining(",", "(", ")"));
            engine.executeScript(ARTEMIS_CLIENT_CONNECTOR + name + params);
        }
    }
}
