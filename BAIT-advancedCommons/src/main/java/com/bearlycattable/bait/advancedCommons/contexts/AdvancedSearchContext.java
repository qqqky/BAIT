package com.bearlycattable.bait.advancedCommons.contexts;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

import com.bearlycattable.bait.commons.contexts.TaskDiagnosticsModel;
import com.bearlycattable.bait.commons.enums.LogTextTypeEnum;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.functions.TriConsumer;
import com.bearlycattable.bait.commons.validators.SearchContextValidator;

import javafx.beans.value.ObservableStringValue;
import javafx.scene.paint.Color;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
public class AdvancedSearchContext {

    private final P2PKHSingleResultData[] dataArray;
    private final List<Integer> disabledWords;
    @Setter
    private String seed;
    private final ObservableStringValue observableProgressLabelValue;
    private final int progressSpacing;
    private final int printSpacing;
    private final int pointThresholdForNotify;
    private final TaskDiagnosticsModel taskDiagnosticsModel;
    private final SearchModeEnum searchMode;
    private final String parentThreadId;
    private final TriConsumer<String, Color, LogTextTypeEnum> logConsumer;
    private final boolean exactMatchCheckEnabled;
    private final boolean verbose;
    @Setter
    private int iterations;
    @Setter
    String wordPrefix; //only used by random modes which use word prefix (this field is always optional)

    //functions that are used to generate next key:
    @Setter
    Function<String, String> nextPrivFunction; //used by inc/dec modes, full random mode and most horizontal rotation modes
    @Setter
    BiPredicate<String, Integer> validityCheckFunctionForVertical; //used by vertical rotation mode
    @Setter
    BiFunction<String, Integer, String> nextPrivFunctionVertical; //used by vertical rotation mode
    @Setter
    BiFunction<String, Integer, String> nextPrivFunctionFullPrefixed; //used by full prefixed rotation mode

    public Optional<String> validate(boolean forGUI) {
        if (dataArray == null || dataArray.length == 0) {
            return Optional.of("Data model for search is empty. Cannot proceed");
        }
        if (Arrays.stream(dataArray).anyMatch(item -> item.getHash() == null || item.getHash().length() != 40)) {
            return Optional.of("Data model was found, but it contains illegal data");
        }
        if (searchMode == null) {
            return Optional.of("Search mode was not specified");
        }

        if (!SearchModeEnum.isRandomRelatedMode(searchMode) && (seed == null || seed.length() != 64)) {
            return Optional.of("Selected search mode requires a 64-hex digit seed");
        }

        Optional<String> functionsValid = SearchContextValidator.validateNextPrivFunctionsForMode(searchMode, nextPrivFunction, validityCheckFunctionForVertical, nextPrivFunctionVertical, nextPrivFunctionFullPrefixed);
        if (functionsValid.isPresent()) {
            return functionsValid;
        }

        if (parentThreadId == null || parentThreadId.length() != 8) {
            return Optional.of("Parent thread id was not found");
        }

        if (forGUI) {
            if (observableProgressLabelValue == null) {
                return Optional.of("Progress label component was not found");
            }
            if (taskDiagnosticsModel == null) {
                return Optional.of("Model for task diagnostics not found");
            }
            if (logConsumer == null) {
                return Optional.of("UI log method not found");
            }
            if (progressSpacing < 0) {
                return Optional.of("Progress spacing parameter is not legal");
            }
        }

        if (printSpacing < 0) {
            return Optional.of("Print spacing parameter is not legal");
        }

        if (pointThresholdForNotify < 0) {
            return Optional.of("Notification threshold parameter is not legal");
        }

        return Optional.empty();
    }
}
