package com.bearlycattable.bait.bl.helpers;


import com.bearlycattable.bait.commons.HeatVisualizerConstants;
import com.bearlycattable.bait.commons.helpers.HeatVisualizerHelper;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

public class HeatVisualizerFormatterFactory {

    public static TextFormatter<String> getDefaultWordInputFieldFormatter() {
        return constructHexInputFieldFormatter(8, true);
    }

    public static TextFormatter<String> getDefaultUnencodedPublicKeyFormatter() {
        return constructHexInputFieldFormatter(40, false);
    }

    public static TextFormatter<String> getDefaultEncodedPublicKeyFormatter() {
        return constructBase58InputFieldFormatter(35);
    }

    public static TextFormatter<String> getDefaultPrivateKeyFormatter() {
        return constructHexInputFieldFormatter(64, true);
    }

    public static TextFormatter<String> getDefaultPositiveNumberFormatter(int maxValue) {
        return constructNumericInputFieldFormatter(0, maxValue);
    }

    private static TextFormatter<String> constructHexInputFieldFormatter(int maxLength, boolean uppercaseOutput) {
        return new TextFormatter<>(changeObject -> {
            if (!changeObject.isContentChange()) {
                return changeObject;
            }

            if (changeObject.isDeleted() && !changeObject.isReplaced()) {
                return changeObject;
            }

            String result = HeatVisualizerHelper.removeNonHexCharacters(changeObject.getText(), uppercaseOutput);

            //TODO: this will need some testing (copy/paste into fields)
            if (result.length() > maxLength) {
                System.out.println("Result before trim: " + result);
                System.out.println("Replace start: " + changeObject.getRangeStart() + ". Replace end: " + changeObject.getRangeEnd());
                result = result.substring(0, maxLength);
                changeObject.setRange(changeObject.getRangeStart(), changeObject.getRangeStart() + result.length());
                System.out.println("Result after trim: " + result);
            }

            changeObject.setText(result);
            String previousText = ((TextField) changeObject.getControl()).getText();

            if (previousText.length() + result.length() <= maxLength) {
                return changeObject;
            }

            int rangeDifference = changeObject.getRangeEnd() - changeObject.getRangeStart();

            if (!changeObject.isReplaced()) { //adding something - must not exceed 8 hex chars
                result = result.substring(0, maxLength - previousText.length());
                changeObject.setText(result);
                return changeObject;
            }

            if (rangeDifference < result.length()) { //replacing with a longer one
                if (previousText.length() != rangeDifference) { //trim if needed
                    result = result.substring(0, rangeDifference);
                }
                // changeObject.setRange(rangeStart, rangeStart + result.length());
                changeObject.setText(result);
            }

            return changeObject;
        });
    }

    private static TextFormatter<String> constructBase58InputFieldFormatter(int maxLength) {
        return new TextFormatter<>(changeObject -> {
            if (!changeObject.isContentChange()) {
                return changeObject;
            }

            if (changeObject.isDeleted() && !changeObject.isReplaced()) {
                return changeObject;
            }

            String result = HeatVisualizerHelper.removeNonBase58Characters(changeObject.getText());

            //TODO: this will need some testing (copy/paste into fields)
            if (result.length() > maxLength) {
                System.out.println("Result before trim: " + result);
                System.out.println("Replace start: " + changeObject.getRangeStart() + ". Replace end: " + changeObject.getRangeEnd());
                result = result.substring(0, maxLength);
                changeObject.setRange(changeObject.getRangeStart(), changeObject.getRangeStart() + result.length());
                System.out.println("Result after trim: " + result);
            }

            changeObject.setText(result);
            String previousText = ((TextField) changeObject.getControl()).getText();

            if (previousText.length() + result.length() <= maxLength) {
                return changeObject;
            }

            int rangeDifference = changeObject.getRangeEnd() - changeObject.getRangeStart();

            if (!changeObject.isReplaced()) { //adding something - must not exceed 8 hex chars
                result = result.substring(0, maxLength - previousText.length());
                changeObject.setText(result);
                return changeObject;
            }

            if (rangeDifference < result.length()) { //replacing with a longer one
                if (previousText.length() != rangeDifference) { //trim if needed
                    result = result.substring(0, rangeDifference);
                }
                // changeObject.setRange(rangeStart, rangeStart + result.length());
                changeObject.setText(result);
            }

            return changeObject;
        });
    }

    private static TextFormatter<String> constructNumericInputFieldFormatter(int minValue, int maxValue) {
        if (maxValue < 0) {
            throw new IllegalArgumentException("Number cannot be negative at HeatVisualizerFormatterFactory#constructNumericInputFieldFormatter");
        }

        int maxLength = Integer.toString(maxValue).length();

        return new TextFormatter<>(changeObject -> {
            if (!changeObject.isContentChange()) {
                return changeObject;
            }

            if (changeObject.isDeleted() && !changeObject.isReplaced()) {
                return changeObject;
            }

            // String existingText = ((TextField) changeObject.getControl()).getText();

            //current text written/pasted
            String result = HeatVisualizerHelper.removeNonIntegerCharacters(changeObject.getText());

            if (result.isEmpty()) {
               changeObject.setText(HeatVisualizerConstants.EMPTY_STRING);
               return changeObject;
            }

            if (result.length() > maxLength) {
                System.out.println("Result before trim: " + result);
                System.out.println("Replace start: " + changeObject.getRangeStart() + ". Replace end: " + changeObject.getRangeEnd());
                result = result.substring(0, maxLength);
                // changeObject.setRange(changeObject.getRangeStart(), changeObject.getRangeStart() + result.length());
                System.out.println("Result after trim: " + result);
            }

            changeObject.setText(result);
            String previousText = ((TextField) changeObject.getControl()).getText();

            if (previousText.length() + result.length() <= maxLength) {
                return changeObject;
            }

            int rangeDifference = changeObject.getRangeEnd() - changeObject.getRangeStart();

            if (!changeObject.isReplaced()) {
                result = result.substring(0, maxLength - previousText.length());
                changeObject.setText(result);
                return changeObject;
            }

            if (rangeDifference < result.length()) { //replacing with a longer one
                if (previousText.length() != rangeDifference) { //trim if needed
                    result = result.substring(0, rangeDifference);
                }
                changeObject.setText(result);
            }

            return changeObject;
        });
    }
}
