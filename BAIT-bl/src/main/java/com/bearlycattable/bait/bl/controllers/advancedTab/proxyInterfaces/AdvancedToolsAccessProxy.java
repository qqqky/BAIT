package com.bearlycattable.bait.bl.controllers.advancedTab.proxyInterfaces;

import java.util.List;
import java.util.Optional;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.advancedCommons.contexts.P2PKHSingleResultData;
import com.bearlycattable.bait.bl.controllers.advancedTab.AdvancedSubTabToolsController;
import com.bearlycattable.bait.commons.enums.LogTextTypeEnum;

import javafx.scene.paint.Color;

public interface AdvancedToolsAccessProxy {

    int readAndTestFile(String pathToUnencodedAddresses, AdvancedSubTabToolsController caller);

    @NonNull
    P2PKHSingleResultData[] createTemplateFromFile(String pathToUnencodedAddressesFile, int max, AdvancedSubTabToolsController caller);

    Optional<P2PKHSingleResultData[]> createTemplateFromStringList(List<String> unencodedAddresses, int max, AdvancedSubTabToolsController caller);

    void logToUi(String message, Color color, LogTextTypeEnum type);

    boolean isVerboseMode();

    boolean isDarkModeEnabled();
}
