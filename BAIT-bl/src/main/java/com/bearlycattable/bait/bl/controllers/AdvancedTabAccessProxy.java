package com.bearlycattable.bait.bl.controllers;

import com.bearlycattable.bait.bl.contexts.HeatComparisonContext;

public interface AdvancedTabAccessProxy {

    void setCurrentInputForced(String input);

    String getCurrentInput();

    void showFullHeatComparison(HeatComparisonContext heatComparisonContext);

    void switchToParentTabX(int index);

    boolean isExactMatchOnly();

    void setDarkModeEnabled(boolean enabled);

    boolean isDarkModeEnabled();

    boolean isVerboseMode();
}
