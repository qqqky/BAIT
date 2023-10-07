package com.bearlycattable.bait.commons.validators;

import java.util.regex.Pattern;

public class PrivKeyValidator {

    public static final Pattern PATTERN_SIMPLE_64 = Pattern.compile("[0123456789ABCDEFabcdef]{64}");
    public static final Pattern START_MIN = Pattern.compile("^0000000000000000000000000000000[\\w]*");
    public static final Pattern START_MAX = Pattern.compile("^FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF[\\w]*");
    private static final String END_MAX = "EBAAEDCE6AF48A03BBFD25E8CD0364140";
    private static final Pattern UNSUPPORTED_KEYS = Pattern.compile("^000000000000000000000000000000000000000000000000000000000000000[01]$");

    //bytes for min(inclusive) and max(inclusive) keys
    private final byte[] ALL_BYTES_MIN = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2};
    private final byte[] ALL_BYTES_MAX = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -2, -70, -82, -36, -26, -81, 72, -96, 59, -65, -46, 94, -116, -48, 54, 65, 64};

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

    public boolean isValidPrivateKey(byte[] bytes) {
        if (bytes == null || bytes.length != 32) {
            return false;
        }

        final boolean isMinContender = bytes[0] == 0;
        byte current;

        for (int i = 0; i < bytes.length; i++) {
            current = bytes[i];

            if (isMinContender) {
                if (current == ALL_BYTES_MIN[i]) {
                    continue;
                }
                return i != 31 || (current != 0 && current != 1);
            }

            if (current == ALL_BYTES_MAX[i]) {
                continue;
            }
            //0 is the lowest number (0 -> 127 -> -128 -> -1 -> 0)
            //-1 is the biggest number, so valid nums must be lower
            int target = ALL_BYTES_MAX[i];
            return target < 0 ? (current < target || current > -1) : (current >= 0 && current < target);
        }

        return true;
    }

    public static boolean isValidPK(String currentKeyHex) {
        return INSTANCE.isValidPrivateKey(currentKeyHex);
    }

    public static boolean isValidPK(byte[] key) {
        return INSTANCE.isValidPrivateKey(key);
    }

}
