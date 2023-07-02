package com.bearlycattable.bait.advanced.interfaces;

import java.util.List;
import java.util.Optional;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.advancedCommons.models.ThreadSpawnModel;
import com.bearlycattable.bait.advancedCommons.dataAccessors.SeedMutationConfigDataAccessor;

public interface AdvancedTaskControl {

    /**
     * Constructs and spawns a background advanced search thread according to provided
     * options in threadSpawnModel.
     * @param threadSpawnModel - all the required options to construct a new thread.
     * @return - threadId of the newly-spawned thread, wrapped into an Optional.
     */
    Optional<String> spawnBackgroundSearchThread(ThreadSpawnModel threadSpawnModel);

    /**
     * Checks if specified thread is still running and returns true if so.
     * @param currentThreadNum - id of the target thread
     * @return
     */
    boolean isBackgroundThreadWorking(String currentThreadNum);

    /**
     * Returns true if all current tasks are done
     * @return
     */
    boolean isAllCurrentTasksDone();

    /**
     * Returns true if implementation currently holds more than 1 task result
     * @return
     */
    boolean isMoreThanOneResultAvailable();

    /**
     * Returns true if implementation allows for more tasks to be created
     * @return
     */
    boolean isTaskCreationAllowed();

    /**
     * Generic initialization method.
     * Upon first initialization, should set 'initialized' flag to true.
     * @param args
     */
    <T> void initialize(T args);

    /**
     * Only returns true if object is initialized
     * @return
     */
    boolean isInitialized();

    /**
     * Mutates seed depending on selected options and returns it
     * @param seed - seed
     * @param disabledWords - words in seed that won't be mutated
     * @param accessor - contains all selected seed mutation options
     * @return
     */
    @NonNull
    String buildMutatedSeed(@NonNull String seed, List<Integer> disabledWords, SeedMutationConfigDataAccessor accessor);
}
