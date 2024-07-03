package com.bearlycattable.bait.utility.addressModifiers.stringModifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.bearlycattable.bait.commons.BaitConstants;
import com.bearlycattable.bait.commons.enums.OutputCaseEnum;

import javafx.util.Pair;

public class DecrementModifierImpl extends AbstractModifier implements DecrementModifier {

    private static final Logger LOG = Logger.getLogger(DecrementModifierImpl.class.getName());
    private final StringBuilder sb = new StringBuilder();
    private final boolean uppercase;

    public DecrementModifierImpl(OutputCaseEnum letterCase) {
        this.uppercase = OutputCaseEnum.UPPERCASE == letterCase;
    }

    /**
     * Decrements all (non-disabled) words of a 64-hex address by 1. Can underflow
     * @param address
     * @param disabledWords
     * @return
     */
    @Override
    public String decrementAllWords(String address, List<Integer> disabledWords) {
        if (!BaitConstants.PATTERN_SIMPLE_64.matcher(address).matches()) {
            return null;
        }

        if (8 == Objects.requireNonNull(disabledWords).size()) {
            return address;
        }

        clearBuilder(sb);

        for (int i = 1; i <= 8; i++) {
            String currentWord = extractCurrentWordFromAddress(address, i);
            int finalI = i; //lambda wants final

            if (disabledWords.stream().noneMatch(num -> num == finalI)) {
                sb.append(decrementWord(currentWord));
            } else {
                sb.append(currentWord);
            }
        }

        return sb.toString().toUpperCase();
    }

    @Override
    public String decrementWordsBy(String address, long decrementBy, List<Integer> disabledWords) {
        if (!BaitConstants.PATTERN_SIMPLE_64.matcher(address).matches()) {
            return null;
        }

        clearBuilder(sb);

        for (int i = 1; i <= 8; i++) {
            String currentWord = extractCurrentWordFromAddress(address, i);
            int finalI = i; //lambda wants final

            if (disabledWords == null || disabledWords.stream().noneMatch(num -> num == finalI)) {
                sb.append(decrementWordBy(currentWord, decrementBy));
            } else {
                sb.append(currentWord);
            }
        }

        return sb.toString().toUpperCase();
    }

    /**
     * Decrements word by a requested value (underflows)
     * @param hexWord
     * @param decrementBy
     * @return
     */
    private String decrementWordBy(String hexWord, long decrementBy) {
        if (hexWord.length() != 8) {
            throw new IllegalArgumentException("Hex word must be of exactly 8 characters");
        }

        if (decrementBy > BaitConstants.OVERFLOW_REFERENCE_8_HEX) {
            decrementBy = decrementBy % BaitConstants.OVERFLOW_REFERENCE_8_HEX;
        }

        long item = Long.parseLong(hexWord, 16);
        long finalNum = item - decrementBy; //0009 - 000A

        if (BaitConstants.ZERO_LONG < finalNum) {
            item = finalNum;
        } else if (BaitConstants.ZERO_LONG > finalNum) {
            item = finalNum + BaitConstants.OVERFLOW_REFERENCE_8_HEX;
        } else {
            item = BaitConstants.ZERO_LONG;
        }

        return helper.padToX(Long.toHexString(item), 8, uppercase);
    }

    @Override
    public String decrementPrivAbsolute(String address, List<Integer> disabledWords) {
        if (!BaitConstants.PATTERN_SIMPLE_64.matcher(address).matches()) {
            throw new IllegalArgumentException("Address passed was not of length 64 at DecrementModifier#decrementPrivAbsolute");
        }

        if (8 == Objects.requireNonNull(disabledWords).size()) {
            return address;
        }

        List<Integer> ignored = new ArrayList<>(disabledWords); //make a copy

        clearBuilder(sb);
        appendToBuilder(sb, address.toUpperCase(Locale.ROOT));

        String currentWord;
        int highestUnlockedIndex = 0;

        while (highestUnlockedIndex == 0) {
            highestUnlockedIndex = findHighestUnlockedIndex(ignored, highestUnlockedIndex);

            if (highestUnlockedIndex == 0) { //if all remaining words happen to get locked (edge case)
                break;
            }

            currentWord = decrementWord(extractCurrentWordFromAddress(address, highestUnlockedIndex)).toUpperCase();
            // System.out.println("Current priv (before decrement): " + sb);
            replaceBuilderWordAtIndex(sb, currentWord, highestUnlockedIndex);
            // System.out.println("New priv (after decrement): " + sb);

            if ("FFFFFFFF".equals(currentWord)) {
                ignored.add(highestUnlockedIndex);
                highestUnlockedIndex = 0; //repeat
            }
        }

        return sb.toString().toUpperCase();
    }

    /**
     * Decrements 64-hex string by a specified number (1-8 hex characters). Disabled words are left intact.
     * Requested decrement range is capped between 0x0 and 0xFFFFFFFF - higher or lower values are not allowed
     * @param seed
     * @param decrementBy
     * @param disabledWords
     * @return
     */
    @Override
    public String decrementPrivAbsoluteBy(String seed, long decrementBy, List<Integer> disabledWords) {
        if (!BaitConstants.PATTERN_SIMPLE_64.matcher(seed).matches()) {
            throw new IllegalArgumentException("Seed not valid at #decrementPrivAbsoluteBy (must be 64 hex characters), [received=" + seed + "]");
        }
        if (BaitConstants.OVERFLOW_REFERENCE_8_HEX <= decrementBy || decrementBy < 0) {
            throw new IllegalArgumentException("Method only accepts decrement requests for values from '0' to 'FFFFFFFF' [received=" + Long.toHexString(decrementBy) + "]");
        }

        if (8 == disabledWords.size()) {
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
            Pair<Long, String> decHelperResult = decrementWordByHelper(currentWord, Long.toHexString(decrementBy));
            if (0 == decHelperResult.getKey()) {
                //no overflow
                replaceBuilderWordAtIndex(sb, decHelperResult.getValue(), highestUnlockedIndex);
                break;
            }

            replaceBuilderWordAtIndex(sb, decHelperResult.getValue(), highestUnlockedIndex);
            decrementBy = decHelperResult.getKey();
            ignored.add(highestUnlockedIndex);
            highestUnlockedIndex = 0; //reset
        }

        return sb.toString().toUpperCase();
    }

    /**
     * Decrements a hex string (1-8 characters), underflows based on its own original length.
     * @param hexString
     * @param decrementBy
     * @return
     */
    @Override
    public String decrementHexStringBy(String hexString, long decrementBy) {
        int length = hexString.length();
        if (length > 8 || length < 1) {
            throw new IllegalArgumentException("Length cannot be more than 8 or less than 1 at #decrementHexStringBy");
        }

        String maxValue = Stream.generate(() -> "F").limit(length).collect(Collectors.joining());
        long maxValueLong = Long.parseLong(maxValue, 16) + 1;
        long overflowReference = 0;

        if (decrementBy > maxValueLong) {
            decrementBy = decrementBy % maxValueLong;
        }

        long initialResult = Long.parseLong(hexString, 16);
        long finalNum = initialResult - decrementBy;

        if (overflowReference < finalNum) {
            initialResult = finalNum;
        } else if (overflowReference > finalNum) {
            initialResult = maxValueLong + finalNum; //finalNum is negative
        } else {
            initialResult = BaitConstants.ZERO_LONG;
        }

        String result = Long.toHexString(initialResult);
        int newLength = result.length();

        if (newLength > length) {
            throw new IllegalStateException("Calculation was wrong at #decrementHexStringBy [oldLength: " + length + ", newLength: " + newLength);
        }

        StringBuilder sb = new StringBuilder();
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

    /**
     * Decrements a word. For input '00000000' returns 'FFFFFFFF'.
     * @param hexWord - 8-hex-character word
     * @return
     */
    private String decrementWord(String hexWord) {
        if (!BaitConstants.PATTERN_HEX_08.matcher(hexWord).matches()) {
            throw new IllegalArgumentException("Method only accepts 8-hex-character string at #decrementWord [received input: " + hexWord + "]");
        }

        long item = Long.parseLong(hexWord, 16);

        if (BaitConstants.ZERO_LONG > --item) {
            item = BaitConstants.OVERFLOW_REFERENCE_8_HEX - 1;
        }

        return helper.padToX(Long.toHexString(item), 8, uppercase);
    }

    private Pair<Long, String> decrementWordByHelper(String hexWord, String decrementBy) {
        if (!BaitConstants.PATTERN_HEX_08.matcher(hexWord).matches()) {
            throw new IllegalArgumentException("Input must be 8 hex word at #decrementWordByHelper");
        }

        if (!BaitConstants.PATTERN_HEX_01_TO_08.matcher(decrementBy).matches()) {
            throw new IllegalArgumentException("Increment request must be from '0' to 'FFFFFFFF' at #decrementWordByHelper");
        }

        long start = Long.parseLong(hexWord, 16);
        long decRequest = Long.parseLong(decrementBy, 16);

        long result = start - decRequest;


        if (result >= 0L) {
            return new Pair<>(0L, helper.padToX(Long.toHexString(result), 8, true));
        }

        long numOverflows = 0;
        while (result < 0L) {
            numOverflows++;
            result = result + BaitConstants.OVERFLOW_REFERENCE_8_HEX;
        }

        return new Pair<>(numOverflows, helper.padToX(Long.toHexString(result), 8, true));
    }
}
