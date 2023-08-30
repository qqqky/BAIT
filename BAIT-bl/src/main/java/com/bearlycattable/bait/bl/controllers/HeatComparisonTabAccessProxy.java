package com.bearlycattable.bait.bl.controllers;

import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;

public interface HeatComparisonTabAccessProxy {

    String getCurrentInput();

    String getUnencodedPubFromConverterTab();

    void setUnencodedPubInConverterTab(String unencodedPub);

    int getNormalizedMapIndexFromComparisonResult(int resultPoints, ScaleFactorEnum scaleFactor);

    boolean isDarkModeEnabled();
}
