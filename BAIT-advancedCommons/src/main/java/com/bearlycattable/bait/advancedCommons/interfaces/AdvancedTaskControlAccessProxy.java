package com.bearlycattable.bait.advancedCommons.interfaces;

import java.util.List;
import java.util.Optional;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.bearlycattable.bait.advancedCommons.dataAccessors.ThreadComponentDataAccessor;
import com.bearlycattable.bait.commons.enums.BackgroundColorEnum;
import com.bearlycattable.bait.commons.enums.LogTextTypeEnum;
import com.bearlycattable.bait.commons.enums.TextColorEnum;

import javafx.scene.paint.Color;

public interface AdvancedTaskControlAccessProxy {

    void insertThreadInfoLabelsToUi(String parentThreadId, String childThreadId, List<String> infoLabels);

    void removeThreadInfoLabelsFromUi(String parentThreadId, String childThreadId);

    Optional<ThreadComponentDataAccessor> addNewThreadProgressContainerToProgressAndResultsTab(@Nullable String parentThreadNum, @Nullable String titleMessage);

    boolean removeThreadProgressContainerFromProgressAndResultsTab(String threadNum);

    void logToUi(String message, Color color, LogTextTypeEnum type);

    void logToUiBold(String message, Color color, LogTextTypeEnum type);

    boolean isVerboseMode();

    boolean isDarkModeEnabled(); //TODO: rename?

    void setBackgroundColorForProgressHBox(String threadNum, String childThreadNum, BackgroundColorEnum color);

    boolean isAutomergePossible();

    Optional<String> getAutomergePathFromProgressAndResultsTab();

    void showErrorMessageInAdvancedSearchSubTab(String error);

    void insertErrorOrSuccessMessageInAdvancedProgressSubTab(String message, TextColorEnum color);

    void modifyAutomergeAccessInProgressSubTab(boolean enabled);

    void disableAdvancedSearchBtn(boolean disable);
}
