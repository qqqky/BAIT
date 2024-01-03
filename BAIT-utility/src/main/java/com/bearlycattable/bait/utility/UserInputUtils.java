package com.bearlycattable.bait.utility;

import java.util.Optional;

import com.bearlycattable.bait.commons.BaitConstants;

public class UserInputUtils {

    public static Optional<String> findDelimiter(String userInput) {
        if (userInput == null || userInput.isEmpty()) {
            return Optional.empty();
        }
        if (userInput.contains("\r\n")) {
            return Optional.of("\r\n");
        }
        if (userInput.contains("\n")) {
            return Optional.of("\n");
        }
        if (userInput.contains(",")) {
            return Optional.of(",");
        }
        if (userInput.contains("\t")) {
            return Optional.of("\t");
        }
        if (userInput.contains(" ")) {
            return Optional.of(" ");
        }

        return Optional.of("");
    }

    public static String cleanUserInput(String userInput, String delimiter) {
        switch (delimiter) {
            case "":
                return userInput;
            case "\r\n" :
                return userInput.replaceAll("[\t ,]", BaitConstants.EMPTY_STRING);
            case "\n":
                return userInput.replaceAll("[\r\t ,]", BaitConstants.EMPTY_STRING);
            case ",":
                return userInput.replaceAll("[\r\n\t ]", BaitConstants.EMPTY_STRING);
            case "\t":
                return userInput.replaceAll("[\r\n ,]", BaitConstants.EMPTY_STRING);
            case " ":
                return userInput.replaceAll("[\r\n\t,]", BaitConstants.EMPTY_STRING);
            default:
                throw new IllegalArgumentException("Delimiter not legal at AdvancedTabPageController#cleanUserInput");
        }
    }
}
