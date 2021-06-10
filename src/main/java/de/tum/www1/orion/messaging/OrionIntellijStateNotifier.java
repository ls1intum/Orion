package de.tum.www1.orion.messaging;

import com.intellij.util.messages.Topic;
import de.tum.www1.orion.enumeration.ExerciseView;

/**
 * Interface to transfer data to the JavaScript client through the {@link de.tum.www1.orion.connector.client.JavaScriptConnector}
 */
public interface OrionIntellijStateNotifier {
    Topic<OrionIntellijStateNotifier> INTELLIJ_STATE_TOPIC = Topic.create("Orion IntelliJ State", OrionIntellijStateNotifier.class);

    void isCloning(boolean cloning);
    void isBuilding(boolean building);
    void openedExercise(long opened, ExerciseView currentView);
    void startedBuild(long courseId, long exerciseId);
}
