package com.bearlycattable.bait.bl.controllers;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.bl.contexts.HeatComparisonContext;

public interface AdvancedTabAccessProxy {

    void setCurrentInputForced(String input);

    @NonNull String getCurrentInput();

    void showFullHeatComparison(HeatComparisonContext heatComparisonContext);

    void switchToParentTabX(int index);

    void setDarkModeFlag(boolean enabled);

    boolean isDarkModeEnabled();

    boolean isVerboseMode();
}
