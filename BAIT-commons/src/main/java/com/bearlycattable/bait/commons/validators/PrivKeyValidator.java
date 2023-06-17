package com.bearlycattable.bait.commons.validators;

import java.util.regex.Pattern;

public class PrivKeyValidator {

    public static final Pattern PATTERN_SIMPLE_64 = Pattern.compile("[0123456789ABCDEFabcdef]{64}");
    public static final Pattern START_MIN = Pattern.compile("^0000000000000000000000000000000[\\w]*");
    public static final Pattern START_MAX = Pattern.compile("^FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF[\\w]*");
    private static final String END_MAX = "EBAAEDCE6AF48A03BBFD25E8CD0364140";
    private static final Pattern UNSUPPORTED_KEYS = Pattern.compile("^000000000000000000000000000000000000000000000000000000000000000[01]$");
    private static final PrivKeyValidator INSTANCE = new PrivKeyValidator();

    private PrivKeyValidator() {}

    public static PrivKeyValidator newInstance() {
        return new PrivKeyValidator();
    }

    public boolean isValidPrivateKey(String currentKeyHex) {
        if (currentKeyHex == null || currentKeyHex.length() != 64 || UNSUPPORTED_KEYS.matcher(currentKeyHex).matches()) {
            return false;
        }

        if (!PATTERN_SIMPLE_64.matcher(currentKeyHex).matches()) {
            return false;
        }

        if (!START_MAX.matcher(currentKeyHex).matches()) {
            return true;
        }

        char current;
        char compared;
        for (int i = 0; i < 33; i++) {
            current = currentKeyHex.charAt(31 + i);
            compared = END_MAX.charAt(i);
            if (current == compared) {
                continue;
            }
            return current < compared;
        }
        return true;
    }

    public static boolean isValidPK(String currentKeyHex) {
        return INSTANCE.isValidPrivateKey(currentKeyHex);
    }

}
