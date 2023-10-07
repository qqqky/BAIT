package com.bearlycattable.bait.utility.addressModifiers.stringModifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.bearlycattable.bait.commons.HeatVisualizerConstants;
import com.bearlycattable.bait.commons.enums.OutputCaseEnum;

import javafx.util.Pair;

public class IncrementModifierImpl extends AbstractModifier implements IncrementModifier {

    private static final Logger LOG = Logger.getLogger(IncrementModifierImpl.class.getName());
    private final StringBuilder sb = new StringBuilder();
    private final boolean uppercase;

    public IncrementModifierImpl(OutputCaseEnum letterCase) {
        this.uppercase = OutputCaseEnum.UPPERCASE == letterCase;
    }

    /**
     * Increments all words equally (disabled words are not incremented)
     * @param address
     * @param disabledWords
     * @return
     */
    @Override
    public String incrementAllWords(String address, List<Integer> disabledWords) {
        if (!HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(address).matches()) {
            return null;
        }

        clearBuilder(sb);

        for (int i = 1; i <= 8; i++) {
            String currentWord = extractCurrentWordFromAddress(address, i);
            int finalI = i; //lambda wants final

            if (disabledWords == null || disabledWords.stream().noneMatch(num -> num == finalI)) {
                sb.append(incrementWord(currentWord));
            } else {
                sb.append(currentWord);
            }
        }

        return sb.toString().toUpperCase();
    }

    @Override
    public String incrementWordsBy(String address, long incrementBy, List<Integer> disabledWords) {
        if (!HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(address).matches()) {
            return null;
        }

        clearBuilder(sb);

        for (int i = 1; i <= 8; i++) {
            String currentWord = extractCurrentWordFromAddress(address, i);
            int finalI = i; //lambda wants final

            if (disabledWords == null || disabledWords.stream().noneMatch(num -> num == finalI)) {
                sb.append(incrementWordBy(currentWord, incrementBy));
            } else {
                sb.append(currentWord);
            }
        }

        return sb.toString().toUpperCase();
    }

    /**
     * Increments word by a requested value (overflows)
     * @param hexWord
     * @param incrementBy
     * @return
     */
    private String incrementWordBy(String hexWord, long incrementBy) {
        if (hexWord.length() != 8) {
            throw new IllegalArgumentException("Hex word must be of exactly 8 characters");
        }

        if (incrementBy > HeatVisualizerConstants.OVERFLOW_REFERENCE_8_HEX) {
            incrementBy = incrementBy % HeatVisualizerConstants.OVERFLOW_REFERENCE_8_HEX;
        }

        long item = Long.parseLong(hexWord, 16);
        long finalNum = item + incrementBy;

        if (HeatVisualizerConstants.OVERFLOW_REFERENCE_8_HEX > finalNum) {
            item = finalNum;
        } else if (HeatVisualizerConstants.OVERFLOW_REFERENCE_8_HEX < finalNum) {
            item = finalNum - HeatVisualizerConstants.OVERFLOW_REFERENCE_8_HEX;
        } else {
            item = HeatVisualizerConstants.ZERO_LONG;
        }

        return helper.padToX(Long.toHexString(item), 8, uppercase);
    }

    /**
     * Increments a word by 1. For input 'FFFFFFFF' returns '00000000'.
     * @param hexWord
     * @return
     */
    private String incrementWord(String hexWord) {
        if (!HeatVisualizerConstants.PATTERN_HEX_08.matcher(hexWord).matches()) {
            throw new IllegalArgumentException("Method only accepts 8-hex-character string at #incrementWord [received input: " + hexWord + "]");
        }

        long item = Long.parseLong(hexWord, 16);

        if (HeatVisualizerConstants.OVERFLOW_REFERENCE_8_HEX <= ++item) {
            item = HeatVisualizerConstants.ZERO_LONG;
        }

        return helper.padToX(Long.toHexString(item), 8, uppercase);
    }

    /**
     * Increments a hex string (1-8 characters), overflows based on its own original length.
     * @param hexString
     * @param incrementBy
     * @return - incremented hex string of same length as input
     */
    @Override
    public String incrementHexStringBy(String hexString, long incrementBy) {
        if (Objects.requireNonNull(hexString).length() > 8) {
            throw new IllegalArgumentException("Only valid for hex strings of length <9 at #incrementHexStringBy");
        }

        int length = hexString.length();

        String maxValue = Stream.generate(() -> "F").limit(length).collect(Collectors.joining());
        long overflowReference = Long.parseLong(maxValue, 16) + 1;

        if (incrementBy > overflowReference) {
            incrementBy = incrementBy % overflowReference;
        }

        long initialResult = Long.parseLong(hexString, 16);
        long finalNum = initialResult + incrementBy;

        return incrementByHelper(finalNum, overflowReference, length, Long::toHexString);
    }

    @Override
    public String incrementHexStringBy(String hexString, String incrementBy) {
        if (Objects.requireNonNull(hexString).length() > 8) {
            throw new IllegalArgumentException("Only valid for hex strings of length <9 at #incrementHexStringBy");
        }

        int length = hexString.length();

        String maxValue = Stream.generate(() -> "F").limit(length).collect(Collectors.joining());
        long overflowReference = Long.parseLong(maxValue, 16) + 1;
        long incAsLong = Long.parseLong(incrementBy, 16);

        if (incAsLong > overflowReference) {
            incAsLong = incAsLong % overflowReference;
        }

        long initialResult = Long.parseLong(hexString, 16);
        long finalNum = initialResult + incAsLong;

        return incrementByHelper(finalNum, overflowReference, length, Long::toHexString);
    }

    @Override
    public String incrementIntegerStringBy(String integerString, long incrementBy) {
        if (Objects.requireNonNull(integerString).length() > 8) {
            throw new IllegalArgumentException("Only valid for hex strings of length <9 at #incrementIntegerStringBy");
        }

        int length = integerString.length();

        String maxValue = Stream.generate(() -> "9").limit(length).collect(Collectors.joining());
        long overflowReference = Long.parseLong(maxValue) + 1;

        if (incrementBy > overflowReference) {
            incrementBy = incrementBy % overflowReference;
        }

        long initialResult = Long.parseLong(integerString);
        long finalNum = initialResult + incrementBy;

        return incrementByHelper(finalNum, overflowReference, length, (Long l) -> Long.toString(l));
    }

    private String incrementByHelper(long finalNum, long overflowReference, int length, Function<Long, String> conversionF) {
        long initialResult;

        if (overflowReference > finalNum) {
            initialResult = finalNum;
        } else if (overflowReference < finalNum) {
            initialResult = finalNum - overflowReference;
        } else {
            initialResult = HeatVisualizerConstants.ZERO_LONG;
        }

        String result = conversionF.apply(initialResult);
        int newLength = result.length();

        if (newLength > length) {
            throw new IllegalStateException("Calculation was wrong at #incrementHexStringBy [oldLength: " + length + ", newLength: " + newLength);
        }

        clearBuilder(sb);

        while (newLength < length) {
            sb.append("0");
            newLength++;
            if (newLength > length) {
                newLength = result.length();
                sb.delete(0, sb.length());
            }
        }

        sb.append(result);
        return uppercase ? sb.toString().toUpperCase(Locale.ROOT) : sb.toString().toLowerCase(Locale.ROOT);
    }

    @Override
    public String incrementPrivAbsolute(String address, List<Integer> disabledWords) {
        if (!HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(address).matches()) {
            return null;
        }

        if (8 == Objects.requireNonNull(disabledWords).size()) {
            return address;
        }

        List<Integer> ignored = new ArrayList<>(disabledWords); //make a copy

        clearBuilder(sb);
        sb.append(address);

        String currentWord;
        int highestUnlockedIndex = 0;

        while (highestUnlockedIndex == 0) {
            highestUnlockedIndex = findHighestUnlockedIndex(ignored, highestUnlockedIndex);

            if (highestUnlockedIndex == 0) {
                break;
            }

            currentWord = incrementWord(extractCurrentWordFromAddress(address, highestUnlockedIndex)).toUpperCase();
            // System.out.println("Current priv (before increment): " + sb);
            replaceBuilderWordAtIndex(sb, currentWord, highestUnlockedIndex);
            // System.out.println("New priv (after increment): " + sb);

            if ("00000000".equals(currentWord)) {
                ignored.add(highestUnlockedIndex);
                highestUnlockedIndex = 0;
            }
        }

        return sb.toString().toUpperCase();
    }

    /**
     * Increments 64-hex string by a specified number (1-8 hex characters). Disabled words are left intact.
     * Requested increment range is capped between 0x0 and 0xFFFFFFFF - higher or lower values are not allowed
     * @param seed
     * @param incrementBy
     * @param disabledWords
     * @return
     */
    @Override
    public String incrementPrivAbsoluteBy(String seed, long incrementBy, List<Integer> disabledWords) {
        if (!HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(seed).matches()) {
            throw new IllegalArgumentException("Seed not valid at #incrementPrivAbsoluteBy (must be 64 hex characters), [received=" + seed + "]");
        }
        if (HeatVisualizerConstants.OVERFLOW_REFERENCE_8_HEX <= incrementBy || incrementBy < 0) {
            throw new IllegalArgumentException("Method only accepts increment requests for values from '0' to 'FFFFFFFF' [received=" + Long.toHexString(incrementBy) + "]");
        }

        if (disabledWords.size() == 8) {
            return seed;
        }

        List<Integer> ignored = new ArrayList<>(disabledWords); //make a copy

        clearBuilder(sb);
        sb.append(seed);

        String currentWord;
        int highestUnlockedIndex = 0;

        while (true) {
            highestUnlockedIndex = findHighestUnlockedIndex(ignored, highestUnlockedIndex);

            if (highestUnlockedIndex == 0) {
                break;
            }

            currentWord = extractCurrentWordFromAddress(sb.toString(), highestUnlockedIndex).toUpperCase();
            Pair<Long, String> incHelperResult = incrementWordByHelper(currentWord, Long.toHexString(incrementBy));
            if (0 == incHelperResult.getKey()) {
                //no overflow
                replaceBuilderWordAtIndex(sb, incHelperResult.getValue(), highestUnlockedIndex);
                break;
            }

            replaceBuilderWordAtIndex(sb, incHelperResult.getValue(), highestUnlockedIndex);
            incrementBy = incHelperResult.getKey();
            ignored.add(highestUnlockedIndex);
            highestUnlockedIndex = 0; //reset
        }

        return sb.toString().toUpperCase();
    }

    /**
     * Helper increments a hex word and also returns number of times it overflowed.
     * Since input must be 8 hex words and increment is limited to max 8 hex characters, we can only overflow once.
     * @param hexWord
     * @param incrementBy
     * @return - num of overflows and result
     */
    private Pair<Long, String> incrementWordByHelper(String hexWord, String incrementBy) {
        if (!HeatVisualizerConstants.PATTERN_HEX_08.matcher(hexWord).matches()) {
            throw new IllegalArgumentException("Input must be 8 hex word at #incrementWordByHelper");
        }

        if (!HeatVisualizerConstants.PATTERN_HEX_01_TO_08.matcher(incrementBy).matches()) {
            throw new IllegalArgumentException("Increment request must be from '0' to 'FFFFFFFF' at #incrementWordByHelper");
        }

        long start = Long.parseLong(hexWord, 16);
        long incRequest = Long.parseLong(incrementBy, 16);

        long result = start + incRequest;

        if (result < HeatVisualizerConstants.OVERFLOW_REFERENCE_8_HEX) {
            return new Pair<>(0L, helper.padToX(Long.toHexString(result), 8, true));
        }

        if (result > HeatVisualizerConstants.OVERFLOW_REFERENCE_8_HEX) {
            return new Pair<>(1L, helper.padToX(Long.toHexString(result - HeatVisualizerConstants.OVERFLOW_REFERENCE_8_HEX), 8, true));
        }

        return new Pair<>(1L, "00000000");
    }
}
