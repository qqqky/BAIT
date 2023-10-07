package com.bearlycattable.bait.commons.validators;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.bearlycattable.bait.commons.enums.SearchModeEnum;

public class SearchContextValidator {

    public static synchronized Optional<String> validateNextPrivFunctionsForMode(SearchModeEnum searchMode, Function<String, String> nextPrivFunction, BiPredicate<String, Integer> validityCheckFunction, BiFunction<String, Integer, String> nextPrivFunctionVertical, BiFunction<String, Integer, String> nextPrivFunctionFullPrefixed) {
        if (searchMode == null) {
            return Optional.of("Search mode is required for QuickSearchContext");
        }

        String testKey = Stream.generate(() -> "1").limit(64).collect(Collectors.joining());

        if ((SearchModeEnum.isIncDecRelatedMode(searchMode) || SearchModeEnum.ROTATION_FULL == searchMode
                || SearchModeEnum.ROTATION_WORDS == searchMode || SearchModeEnum.isRandomRelatedMode(searchMode))) {
            if (nextPrivFunction == null) {
                return Optional.of("Function for generating a key was not found for requested mode [mode=" + searchMode + "]");
            }
            if (nextPrivFunction.apply(testKey).length() != 64) {
                return Optional.of("Function for generating a new key appears to be invalid for requested mode [mode=" + searchMode + "]");
            }
        }

        if (SearchModeEnum.ROTATION_FULL_WITH_HEADER == searchMode) {
            if (nextPrivFunctionFullPrefixed == null) {
                return Optional.of("Function for generating a key was not found for requested mode [mode=" + searchMode + "]");
            }
            if (nextPrivFunctionFullPrefixed.apply(testKey, 1).length() != 64) {
                return Optional.of("Function for generating a new key appears to be invalid for requested mode [mode=" + searchMode + "]");
            }
        }

        if (SearchModeEnum.ROTATION_INDEX_VERTICAL == searchMode) {
            if (nextPrivFunctionVertical == null) {
                return Optional.of("Function for generating a key not found for requested mode [mode=" + searchMode + "]");
            }
            if (validityCheckFunction == null) {
                return Optional.of("Validation function not found for requested mode [mode=" + searchMode + "]");
            }
            if (Stream.iterate(1, i -> ++i).limit(8).anyMatch(indexTest -> validityCheckFunction.test(testKey, indexTest) && nextPrivFunctionVertical.apply(testKey, indexTest).length() != 64)) {
                return Optional.of("Validation function or function for generating a new key appears to be invalid for requested mode [mode=" + searchMode + "]");
            }
        }
        return Optional.empty();
    }
}
