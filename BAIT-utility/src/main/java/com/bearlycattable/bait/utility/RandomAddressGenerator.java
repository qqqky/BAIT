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
    private final Pattern START_MAX = Pattern.compile("^FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF[\\w]*");
    private final String END_MAX = "EBAAEDCE6AF48A03BBFD25E8CD0364140";

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

    public String generateValidKey() {
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
           return generateValidKey();
        }

        return mainBuilder.toString();
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

    public String generateWithBlacklist(String currentPriv, List<Integer> disabledWords) {
        if (currentPriv.length() != 64 && (disabledWords != null && !disabledWords.isEmpty())) {
            throw new IllegalArgumentException("Priv must be of length 64 at #generateWithBlacklist (probably, a word has been locked even though it was empty)");
        }

        if (disabledWords == null || disabledWords.isEmpty()) {
            return generateValidKey();
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
            return generateValidKey();
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
}
