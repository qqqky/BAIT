package com.bearlycattable.bait.bl.controllers.advancedTab.proxyInterfaces;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.advancedCommons.dataAccessors.SeedMutationConfigDataAccessor;
import com.bearlycattable.bait.advancedCommons.models.ThreadSpawnModel;
import com.bearlycattable.bait.bl.controllers.advancedTab.AdvancedSubTabSearchController;
import com.bearlycattable.bait.commons.enums.JsonResultScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.JsonResultTypeEnum;
import com.bearlycattable.bait.commons.enums.LogTextTypeEnum;

import javafx.scene.paint.Color;
import javafx.util.Pair;

public interface AdvancedSearchAccessProxy {

    void loadAdvancedSearchResultsToUi(String pathToResultFile, Map<String, Map<JsonResultTypeEnum, Map<JsonResultScaleFactorEnum, Pair<String, Integer>>>> advancedSearchResultsAsMap);

    void switchToChildTabX(int index);

    String importDataFromCurrentInputFieldInMainTab(AdvancedSubTabSearchController caller);

    void exportDataToCurrentInputFieldInMainTab(String priv, AdvancedSubTabSearchController caller);

    String buildMutatedSeed(@NonNull String seed, List<Integer> disabledWords, SeedMutationConfigDataAccessor accessor);

    @NonNull Set<String> readUnencodedPubsListIntoSet(String pathToUnencodedAddressesFile);

    boolean isVerboseMode();

    boolean isDarkModeEnabled();

    void logToUi(String message, Color color, LogTextTypeEnum type);

    boolean isBackgroundThreadWorking(String currentThreadNum);

    //special task access with caller check
    Optional<String> spawnBackgroundSearchThread(ThreadSpawnModel threadSpawnModel, @NonNull AdvancedSubTabSearchController caller);

    boolean isTaskCreationAllowed(@NonNull AdvancedSubTabSearchController caller);
}
