package com.bearlycattable.bait.bl.controllers.advancedTab;

import java.util.Optional;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.advancedCommons.models.ThreadSpawnModel;

public interface AdvancedTaskProxy {

    Optional<String> spawnBackgroundSearchThread(ThreadSpawnModel threadSpawnModel, @NonNull AdvancedSubTabSearchController controller);

    boolean isTaskCreationAllowed(@NonNull AdvancedSubTabSearchController controller);
}
