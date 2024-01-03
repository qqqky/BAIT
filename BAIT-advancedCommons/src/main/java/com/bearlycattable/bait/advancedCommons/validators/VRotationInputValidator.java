package com.bearlycattable.bait.advancedCommons.validators;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.bearlycattable.bait.commons.BaitConstants;

public class VRotationInputValidator {

    private static final Pattern anyUnsupportedSymbol = Pattern.compile("^.*[^\\d-, ]+.*$");
    private static final Pattern twoOrMoreDelimitersInARow = Pattern.compile("^.*[-]{2,}.*$");

    public static ValidatorResponse validateInputForIndexRangeText(String vRotIndexes) {
        ValidatorResponse response = ValidatorResponse.builder().build();

        if (vRotIndexes.isEmpty()) {
            response.setResponseType(OptionalConfigValidationResponseType.SILENT_ABORT);
            return response;
        }

        if (vRotIndexes.length() > 256) {
            response.setErrorMessage("What are you trying to achieve here exactly?");
            response.setResponseType(OptionalConfigValidationResponseType.ABORT);
            return response;
        }

        if (anyUnsupportedSymbol.matcher(vRotIndexes).matches()) {
            response.setErrorMessage("Input contains some unsupported symbols. Please see the tooltip for a correct input example");
            response.setResponseType(OptionalConfigValidationResponseType.ABORT);
            return response;
        }

        List<String> split = Arrays.stream(vRotIndexes.split(","))
                .map(String::trim)
                .filter(str -> !str.isEmpty())
                .collect(Collectors.toList());

        if (split.stream().anyMatch(str -> twoOrMoreDelimitersInARow.matcher(str).matches())) {
            response.setErrorMessage("Input cannot be recognized. Please see the tooltip for a correct input example");
            response.setResponseType(OptionalConfigValidationResponseType.ABORT);
            return response;
        }

        if (split.stream().anyMatch(str -> str.startsWith("-")) || split.stream().anyMatch(str -> str.endsWith("-"))) {
            response.setErrorMessage("Input cannot be recognized. Make sure there are no negative numbers. Please see the tooltip for a correct input example");
            response.setResponseType(OptionalConfigValidationResponseType.ABORT);
            return response;
        }

        System.out.println("After trimming and cleanup: " + split);

        List<Integer> result = split.stream()
                .map(str -> {
                    if (!str.contains("-")) {
                        return Collections.singletonList(Integer.parseInt(str));
                    }

                    List<Integer> range =  Arrays.stream(str.split("-"))
                            .map(String::trim)
                            .mapToInt(Integer::parseInt)
                            .boxed()
                            .collect(Collectors.toList());

                    if (range.size() != 2) {
                        throw new IllegalStateException("Wrong range input. Correct example: 22-34");
                    }

                    int min = range.stream().min(Integer::compareTo).get();
                    range.remove((Integer) min);
                    int max = range.get(0);

                    if (min > 63 || max < 0) {
                        return null;
                    }

                    if (min < 0) {
                        min = 0;
                    }

                    if (max > 63) {
                        max = 63;
                    }

                    final int finalMax = max;
                    return Stream.iterate(min, i -> ++i)
                            .filter(item -> item <= finalMax)
                            .limit(max + 1 - min)
                            .collect(Collectors.toList());

                })
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        System.out.println("Selected indexes for vertical rotation: " + result);

        response.setResponseType(OptionalConfigValidationResponseType.CONTINUE);
        response.setResponseData(result);
        return response;
    }

    public static ValidatorResponse validateInputForVRotValue(String rotateVBy) {
        ValidatorResponse response = ValidatorResponse.builder().build();
        if (!BaitConstants.DIGITS_ONLY_MAX2.matcher(rotateVBy).matches()) {
            response.setResponseType(OptionalConfigValidationResponseType.ABORT);
            response.setErrorMessage("Input for vertical rotation must not exceed 2 integers");
            return response;
        }

        int rotations = Integer.parseInt(rotateVBy);
        if (rotations < 1 || rotations > 16) {
            response.setResponseType(OptionalConfigValidationResponseType.ABORT);
            response.setErrorMessage("Vertical rotations must be between 1 and 16.");
            return response;
        }

        response.setResponseType(OptionalConfigValidationResponseType.CONTINUE);
        response.setResponseData(rotateVBy);
        return response;
    }
}
