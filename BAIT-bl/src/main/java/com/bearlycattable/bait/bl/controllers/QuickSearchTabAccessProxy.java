package com.bearlycattable.bait.bl.controllers;

import com.bearlycattable.bait.bl.contexts.HeatComparisonContext;

public interface QuickSearchTabAccessProxy {

    void setUnencodedPubInConverterTab(String unencodedPub);

    String getUnencodedPubFromConverterTab();

    String getCurrentInput();

    void showFullHeatComparison(HeatComparisonContext heatComparisonContext);

    void switchToComparisonTab();

    boolean isDarkModeEnabled();

    boolean isVerboseMode();

}
