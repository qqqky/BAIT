package com.bearlycattable.bait.bl.controllers.advancedTab.proxyInterfaces;

import com.bearlycattable.bait.bl.contexts.HeatComparisonContext;
import com.bearlycattable.bait.commons.enums.LogTextTypeEnum;

import javafx.scene.paint.Color;

public interface AdvancedResultsAccessProxy {

    void showFullHeatComparison(HeatComparisonContext heatComparisonContext);

    void switchToComparisonTab();

    void logToUi(String message, Color color, LogTextTypeEnum type);

    boolean isVerboseMode();
}
