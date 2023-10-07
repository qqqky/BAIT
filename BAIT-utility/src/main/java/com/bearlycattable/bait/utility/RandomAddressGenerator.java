package com.bearlycattable.bait.utility;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.commons.HeatVisualizerConstants;

/*
 *
 * Program generates a random private address
 *
 * Valid range is as follows:
 *
 * BTC max key value is: 0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364140 (inclusive)
 * BTC min key value is: 0x0000000000000000000000000000000000000000000000000000000000000001 (one)
 *
 */
public class RandomAddressGenerator {

    private final Map<Integer, String> HEX_ALPHABET_MAPPINGS = new HashMap<>();
    private final String[] HEX_ALPHABET = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
    private final byte[] BYTE_MAPPINGS = {
            -128, -127, -126, -125, -124, -123, -122, -121, -120, -119, -118, -117, -116, -115, -114, -113,
            -112, -111, -110, -109, -108, -107, -106, -105, -104, -103, -102, -101, -100, -99, -98, -97,
            -96, -95, -94, -93, -92, -91, -90, -89, -88, -87, -86, -85, -84, -83, -82, -81,
            -80, -79, -78, -77, -76, -75, -74, -73, -72, -71, -70, -69, -68, -67, -66, -65,
            -64, -63, -62, -61, -60, -59, -58, -57, -56, -55, -54, -53, -52, -51, -50, -49,
            -48, -47, -46, -45, -44, -43, -42, -41, -40, -39, -38, -37, -36, -35, -34, -33,
            -32, -31, -30, -29, -28, -27, -26, -25, -24, -23, -22, -21, -20, -19, -18, -17,
            -16, -15, -14, -13, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1,
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
            16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31,
            32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47,
            48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63,
            64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79,
            80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95,
            96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111,
            112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127};
    private final Pattern START_MAX = Pattern.compile("^FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF[\\w]*");
    private final String END_MAX = "EBAAEDCE6AF48A03BBFD25E8CD0364140";
    private final byte[] ALL_BYTES_MIN = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2};
    private final byte[] ALL_BYTES_MAX = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -2, -70, -82, -36, -26, -81, 72, -96, 59, -65, -46, 94, -116, -48, 54, 65, 64};

    //key 0x0 is invalid, key 0x1 should be valid but is not supported by bitcoinj library (0.15.9)
    private final Pattern UNSUPPORTED_BITCOINJ_KEYS = Pattern.compile("^000000000000000000000000000000000000000000000000000000000000000[01]$");

    private final StringBuilder mainBuilder = new StringBuilder();
    private final StringBuilder helperBuilder = new StringBuilder();
    private int length;
    private final Random generator;
    private static final Logger LOG = Logger.getLogger(RandomAddressGenerator.class.getName());

    private RandomAddressGenerator(int length, boolean secure) {
        this.length = length;
        if (secure) {
            generator = new SecureRandom();
        } else {
            generator = ThreadLocalRandom.current();
        }

        for (int i = 0; i < HEX_ALPHABET.length; i++) {
            HEX_ALPHABET_MAPPINGS.put(i, HEX_ALPHABET[i]);
        }
    }

    public static RandomAddressGenerator getSecureGenerator(int length) {
        return new RandomAddressGenerator(length, true);
    }

    public String generateValidKeyString() {
        clearBuilder(mainBuilder);

        while (mainBuilder.length() != length) {
            for (int i = 1; i <= length; i++) {
                mainBuilder.append(upToF());
            }
            if (mainBuilder.length() != length) {
                clearBuilder(mainBuilder);
            }
        }

        if (!isCurrentKeyValid(mainBuilder)) {
           return generateValidKeyString();
        }

        return mainBuilder.toString();
    }

    public byte[] generateValidKeyBytes() {
        byte[] result = new byte[length / 2];

        for (int i = 0; i < result.length; i++) {
            result[i] = takeRandomByte();
        }

        if (!isCurrentKeyValid(result)) {
            return generateValidKeyBytes();
        }

        return result;
    }

    /*
    Complete priv key validity check (assumes input is hex)
     */
    private boolean isCurrentKeyValid(StringBuilder currentKeyHex) {
        if (currentKeyHex == null || currentKeyHex.length() != 64 || UNSUPPORTED_BITCOINJ_KEYS.matcher(currentKeyHex).matches()) {
            return false;
        }

        if (!HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(currentKeyHex).matches()) {
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

    public boolean isCurrentKeyValid(byte[] bytes) {
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

    public String generateWithBlacklist(String currentPriv, List<Integer> disabledWords) {
        if (currentPriv.length() != 64 && (disabledWords != null && !disabledWords.isEmpty())) {
            throw new IllegalArgumentException("Priv must be of length 64 at #generateWithBlacklist (probably, a word has been locked even though it was empty)");
        }

        if (disabledWords == null || disabledWords.isEmpty()) {
            return generateValidKeyString();
        }

        clearBuilder(mainBuilder);

        while (mainBuilder.length() != length) {
            for (int i = 1; i <= 8; i++) {
                int finalI = i;
                if (disabledWords.stream().noneMatch(num -> num == finalI)) {
                    mainBuilder.append(generateHexString(8));
                } else {
                    mainBuilder.append(currentPriv, (i - 1) * 8, ((i - 1) * 8) + 8);
                }
            }
            if (mainBuilder.length() != 64) {
                clearBuilder(mainBuilder);
            }
        }

        return mainBuilder.toString();
    }

    public byte[] generateWithBlacklist(byte[] currentKeyBytes, List<Integer> disabledWords) {
        if (currentKeyBytes == null || currentKeyBytes.length != 32) {
            throw new IllegalArgumentException("Priv bytes must be of length " + 32 + "at #generateWithBlacklist(byte[], List<Integer>)");
        }

        if (disabledWords == null || disabledWords.isEmpty()) {
            return generateValidKeyBytes();
        }

        for (int i = 0; i < 8; i++) {
            if (!disabledWords.contains(i + 1)) {
                System.arraycopy(generateBytes(4), 0, currentKeyBytes, (i * 4), 4);
            }
        }

        return currentKeyBytes;
    }

    @NonNull
    public String generateHexString(int hexLength) {
        clearBuilder(helperBuilder);

        if (hexLength <= 0) {
            return HeatVisualizerConstants.EMPTY_STRING;
        }

        while (helperBuilder.length() != hexLength) {
            for (int i = 0; i < hexLength; i++) {
                helperBuilder.append(upToF());
            }
            if (helperBuilder.length() != hexLength) {
                clearBuilder(helperBuilder);
            }
        }

        return helperBuilder.toString();
    }

    public byte[] generateBytes(int numBytes) {
        if (numBytes <= 0) {
            return new byte[] {0};
        }
        byte[] result = new byte[numBytes];

        for (int i = 0; i < numBytes; i++) {
            result[i] = takeRandomByte();
        }

        return result;
    }

    /**
     * Generates key with identical words (for testing purposes)
     * @param wordPrefix - optional prefix for every word. It won't be randomized
     * @return
     */
    public String generateRandomSameWord(String wordPrefix) {
        clearBuilder(mainBuilder);

        String base;
        if (wordPrefix == null || wordPrefix.isEmpty()) {
            base = generateHexString(8);
        } else if (wordPrefix.length() > 8) {
            throw new IllegalArgumentException("seed prefix cannot be longer than 8 hex characters");
        } else {
            base = wordPrefix + generateHexString(8 - wordPrefix.length());
        }

        for (int i = 0; i < 8; i++) {
            mainBuilder.append(base);
        }

        return mainBuilder.toString();
    }

    /**
     * Used to generate a random key, where the prefix for every word is the same
     * @param wordPrefix - 1-8 hex digit prefix for every word
     * @return
     */
    public String generateRandomPrefixed(String wordPrefix) {
        clearBuilder(mainBuilder);

        if (wordPrefix == null || wordPrefix.isEmpty()) {
            return generateValidKeyString();
        } else if (wordPrefix.length() > 8) {
            throw new IllegalArgumentException("Word prefix cannot be longer than 8 hex characters");
        }

        for (int i = 0; i < 8; i++) {
            mainBuilder.append(wordPrefix).append(generateHexString(8 - wordPrefix.length()));
        }
        return mainBuilder.toString();
    }

    /**
     * Used to generate a random key, where the prefix for every word is the same. Disabled words are ignored.
     * @param wordPrefix - 1-8 hex digit prefix for every word
     * @param current - current key (64 hex digit string)
     * @param disabledWords - list of disabled words (1-8)
     * @return
     */
    public String generateRandomPrefixedWithBlacklist(String wordPrefix, String current, List<Integer> disabledWords) {
        if (disabledWords == null || disabledWords.isEmpty()) {
            return generateRandomPrefixed(wordPrefix);
        }

        clearBuilder(mainBuilder);

        if (wordPrefix == null || wordPrefix.isEmpty()) {
            return generateWithBlacklist(current, disabledWords);
        } else if (wordPrefix.length() > 8) {
            throw new IllegalArgumentException("Word prefix cannot be longer than 8 hex characters at #generateRandomPrefixedWithBlacklist");
        }

        for (int i = 1; i <= 8; i++) {
            if (disabledWords.contains((i))) {
                mainBuilder.append(current, (i - 1) * 8, ((i - 1) * 8) + 8);
            } else {
                mainBuilder.append(wordPrefix).append(generateHexString(8 - wordPrefix.length()));
            }
        }

        return mainBuilder.toString();
    }

    public String generateRandomSameWordWithBlacklist(String prefix, String currentPriv, List<Integer> disabledWords) {
        if (disabledWords == null || disabledWords.isEmpty()) {
            return generateRandomSameWord(prefix);
        }

        clearBuilder(mainBuilder);

        String base;
        if (prefix == null || prefix.isEmpty()) {
            base = generateHexString(8);
        } else if (prefix.length() > 8) {
            throw new IllegalArgumentException("seed prefix cannot be longer than 8 hex characters");
        } else {
            base = prefix + generateHexString(8 - prefix.length());
        }

        while (mainBuilder.length() != length) {
            for (int i = 1; i <= 8; i++) {
                int finalI = i;
                if (disabledWords.stream().noneMatch(num -> num == finalI)) {
                    mainBuilder.append(base);
                } else {
                    mainBuilder.append(currentPriv, (i - 1) * 8, ((i - 1) * 8) + 8);
                }
            }
            if (mainBuilder.length() != 64) {
                clearBuilder(mainBuilder);
            }
        }
        return mainBuilder.toString();
    }

    private void clearBuilder(StringBuilder currentBuilder) {
        currentBuilder.delete(0, currentBuilder.length());
    }

    private String upToF () {
        return HEX_ALPHABET_MAPPINGS.get(generator.nextInt(16));
    }

    private byte takeRandomByte() {
        return BYTE_MAPPINGS[generator.nextInt(256)];
    }
}
