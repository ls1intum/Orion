package de.tum.www1.orion.messaging;

import com.intellij.util.messages.Topic;
import de.tum.www1.orion.enumeration.ExerciseView;

public interface OrionIntellijStateNotifier {
    Topic<OrionIntellijStateNotifier> INTELLIJ_STATE_TOPIC = Topic.create("Orion IntelliJ State", OrionIntellijStateNotifier.class);

    void isCloning(boolean cloning);
    void isBuilding(boolean building);
    void openedExercise(long opened, ExerciseView currentView);
    void startedBuild(long courseId, long exerciseId);
}
