package com.bearlycattable.bait.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.bearlycattable.bait.commons.HeatVisualizerConstants;
import com.bearlycattable.bait.commons.enums.OutputCaseEnum;
import com.bearlycattable.bait.commons.helpers.HeatVisualizerHelper;

import javafx.util.Pair;

public class AddressModifier {

    private static final Logger LOG = Logger.getLogger(AddressModifier.class.getName());

    private final StringBuilder sb = new StringBuilder();
    private final RandomAddressGenerator generator = RandomAddressGenerator.getSecureGenerator(64);

    private final HeatVisualizerHelper helper = new HeatVisualizerHelper();
    private final boolean uppercase;

    public AddressModifier(OutputCaseEnum letterCase) {
        this.uppercase = OutputCaseEnum.UPPERCASE == letterCase;
    }

    private String rotateLeftBy(String address, int rotateBy) {
        if (address == null || address.isEmpty()) {
            return null;
        }

        if (!isValidIndex(address.length(), rotateBy)) {
            return null;
        }

        return address.substring(rotateBy) + address.substring(0, rotateBy);
    }

    private String rotateRightBy(String address, int index) {
        if (address == null || address.isEmpty()) {
            return null;
        }

        if (!isValidIndex(address.length(), index)) {
            return null;
        }

        return address.substring(address.length() - index) + address.substring(0, address.length() - index);
    }

    public String rotateAddressLeftBy(String address, int rotateBy, boolean withInternalPrefix) {
        return withInternalPrefix ? rotateLeftBy((HeatVisualizerConstants.PRIV_HEADER + address), rotateBy > 128 ? rotateBy % 128 : rotateBy).substring(64) : rotateLeftBy(address, rotateBy > 64 ? rotateBy % 64 : rotateBy);
    }

    public String rotateAddressRightBy(String address, int rotateBy, boolean withInternalPrefix) {
        return withInternalPrefix ? rotateRightBy((HeatVisualizerConstants.PRIV_HEADER + address), rotateBy > 128 ? rotateBy % 128 : rotateBy).substring(64) : rotateRightBy(address, rotateBy > 64 ? rotateBy % 64 : rotateBy);
    }

    /**
     * Rotates selected words of a 64-hex address by specified number (left circular rotation)
     * After 8 rotations address is back to original.
     * @param address
     * @param rotateBy
     * @param disabledWords
     * @return resulting 64-hex address after word rotation
     */
    public String rotateAllWordsBy(String address, int rotateBy, List<Integer> disabledWords) {
        if (!HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(address).matches()) {
            return null;
        }

        if (rotateBy > 8) {
            rotateBy = rotateBy % 8;
        }

        clearBuilder();

        for (int i = 1; i <= 8; i++) {
            String currentWord = extractCurrentWordFromAddress(address, i);
            int finalI = i; //lambda wants final

            if (disabledWords.stream().anyMatch(wordNum -> wordNum == finalI)) {
                sb.append(currentWord);
            } else {
                sb.append(rotateLeftBy(currentWord, rotateBy));
            }
        }
        return sb.toString();
    }

    private boolean isValidIndex(int inputLength, int index) {
        return index >= 0 && index <= inputLength;
    }

    /**
     * Increments all words equally (disabled words are not incremented)
     * @param address
     * @param disabledWords
     * @return
     */
    public String incrementAllWords(String address, List<Integer> disabledWords) {
        if (!HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(address).matches()) {
            return null;
        }

        clearBuilder();

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

    public String incrementWordsBy(String address, long incrementBy, List<Integer> disabledWords) {
        if (!HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(address).matches()) {
            return null;
        }

        clearBuilder();

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

    public String decrementWordsBy(String address, long decrementBy, List<Integer> disabledWords) {
        if (!HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(address).matches()) {
            return null;
        }

        clearBuilder();

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
     * Decrements all (non-disabled) words of a 64-hex address by 1. Can underflow
     * @param address
     * @param disabledWords
     * @return
     */
    public String decrementAllWords(String address, List<Integer> disabledWords) {
        if (!HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(address).matches()) {
            return null;
        }

        if (8 == Objects.requireNonNull(disabledWords).size()) {
            return address;
        }

        clearBuilder();

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

    public String incrementPrivAbsolute(String address, List<Integer> disabledWords) {
        if (!HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(address).matches()) {
            return null;
        }

        if (8 == Objects.requireNonNull(disabledWords).size()) {
            return address;
        }

        List<Integer> ignored = new ArrayList<>(disabledWords); //make a copy

        clearBuilder();
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
            replaceBuilderWordAtIndex(currentWord, highestUnlockedIndex);
            // System.out.println("New priv (after increment): " + sb);

            if ("00000000".equals(currentWord)) {
                ignored.add(highestUnlockedIndex);
                highestUnlockedIndex = 0;
            }
        }

        return sb.toString().toUpperCase();
    }

    private String extractCurrentWordFromAddress(String address, int wordNumber) {
        return address.substring((wordNumber - 1) * 8, ((wordNumber - 1) * 8) + 8);
    }

    public String decrementPrivAbsolute(String address, List<Integer> disabledWords) {
        if (!HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(address).matches()) {
            throw new IllegalArgumentException("Address passed was not of length 64 at AddressModifier#decrementPrivAbsolute");
        }

        if (8 == Objects.requireNonNull(disabledWords).size()) {
            return address;
        }

        List<Integer> ignored = new ArrayList<>(disabledWords); //make a copy

        clearBuilder();
        appendToBuilder(address.toUpperCase(Locale.ROOT));

        String currentWord;
        int highestUnlockedIndex = 0;

        while (highestUnlockedIndex == 0) {
            highestUnlockedIndex = findHighestUnlockedIndex(ignored, highestUnlockedIndex);

            if (highestUnlockedIndex == 0) { //if all remaining words happen to get locked (edge case)
                break;
            }

            currentWord = decrementWord(extractCurrentWordFromAddress(address, highestUnlockedIndex)).toUpperCase();
            // System.out.println("Current priv (before decrement): " + sb);
            replaceBuilderWordAtIndex(currentWord, highestUnlockedIndex);
            // System.out.println("New priv (after decrement): " + sb);

            if ("FFFFFFFF".equals(currentWord)) {
                ignored.add(highestUnlockedIndex);
                highestUnlockedIndex = 0; //repeat
            }
        }

        return sb.toString().toUpperCase();
    }

    private int findHighestUnlockedIndex(List<Integer> ignored, int highestUnlockedIndex) {
        return Stream.iterate(1, i -> ++i)
                .map(num -> ignored.contains(num) ? null : num)
                .limit(8)
                .filter(Objects::nonNull)
                .reduce(highestUnlockedIndex, (result, currentNum) -> currentNum > result ? currentNum : result);
    }

    private void appendToBuilder(String address) {
        sb.append(address);
    }

    private void replaceBuilderWordAtIndex(String currentWord, int highestWordNum) {
        sb.replace((highestWordNum - 1) * 8, ((highestWordNum - 1) * 8) + 8, currentWord);
    }

    public String rotateSelectedIndexVertically(String address, List<Integer> disabledWords, int selectedIndex) {
        if (!isValidIndexForVerticalRotation(address, disabledWords, selectedIndex)) {
            return null;
        }

        clearBuilder();
        sb.append(address);

        String incrementedChar = incrementHexCharacter(String.valueOf(address.charAt(selectedIndex))).toUpperCase();
        if (!HeatVisualizerConstants.PATTERN_HEX_01.matcher(incrementedChar).matches()) {
            throw new IllegalStateException("at AddressModifier#rotateSelectedIndexVertically. Incremented result was: " + incrementedChar);
        }
        sb.replace(selectedIndex, selectedIndex + 1, incrementedChar);

        return sb.toString();
    }

    public boolean isValidIndexForVerticalRotation(String address, List<Integer> disabledWords, int selectedIndex) {
        if (address == null || address.length() <= selectedIndex || address.length() != 64) {
            return false;
        }

        return disabledWords.stream()
                .noneMatch(disabledWord ->
                   (selectedIndex >= (disabledWord - 1) * 8)
                   && (selectedIndex < ((disabledWord - 1) * 8) + 8));
    }

    public String fuzzSelectedWords(String address, List<Integer> disabledWords) {
        if (!HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(address).matches()) {
            return null;
        }

        clearBuilder();

        for (int i = 1; i <= 8; i++) {
            if (disabledWords.contains(i)) {
                sb.append(extractCurrentWordFromAddress(address, i));
            } else {
                sb.append(generator.generateHexString(8));
            }
        }
        return sb.toString().toUpperCase();
    }

    /**
     * Increments a word by 1. For input 'FFFFFFFF' returns '00000000'.
     * @param hexWord
     * @return
     */
    private String incrementWord(String hexWord) {
        if (!HeatVisualizerConstants.PATTERN_SIMPLE_08.matcher(hexWord).matches()) {
            throw new IllegalArgumentException("Method only accepts 8-hex-character string at #incrementWord [received input: " + hexWord + "]");
        }

        long item = Long.parseLong(hexWord, 16);

        if (HeatVisualizerConstants.OVERFLOW_REFERENCE_8_HEX <= ++item) {
            item = HeatVisualizerConstants.ZERO_LONG;
        }

        return helper.padTo8(Long.toHexString(item), uppercase);
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

        return helper.padTo8(Long.toHexString(item), uppercase);
    }

    /**
     * Decrements word by a requested value (underflows)
     * @param hexWord
     * @param decrementBy
     * @return
     */
    private String decrementWordBy(String hexWord, long decrementBy) {
        if (hexWord.length() > 8) {
            throw new IllegalArgumentException("Hex word cannot be longer than 8 characters");
        }

        long maxValueLong = Long.parseLong(Stream.generate(() -> "F").limit(8).collect(Collectors.joining())) + 1;

        if (decrementBy > maxValueLong) {
            decrementBy = decrementBy % maxValueLong;
        }

        long item = Long.parseLong(hexWord, 16);
        long finalNum = item - decrementBy; //0009 - 000A

        if (HeatVisualizerConstants.ZERO_LONG > finalNum) {
            item = HeatVisualizerConstants.OVERFLOW_REFERENCE_8_HEX - finalNum;
        } else {
            item = finalNum;
        }

        return helper.padTo8(Long.toHexString(item), uppercase);
    }

    /**
     * Increments a hex string (1-8 characters), overflows based on its own original length.
     * @param hexString
     * @param incrementBy
     * @return
     */
    public String incrementHexStringBy(String hexString, long incrementBy) {
        int length = hexString.length();

        String maxValue = Stream.generate(() -> "F").limit(length).collect(Collectors.joining());
        long overflowReference = Long.parseLong(maxValue, 16) + 1;

        if (incrementBy > overflowReference) {
            incrementBy = incrementBy % overflowReference;
        }

        long initialResult = Long.parseLong(hexString, 16);
        long finalNum = initialResult + incrementBy;

        if (overflowReference > finalNum) {
            initialResult = finalNum;
        } else if (overflowReference < finalNum) {
            initialResult = finalNum - overflowReference;
        } else {
            initialResult = HeatVisualizerConstants.ZERO_LONG;
        }

        String result = Long.toHexString(initialResult);
        int newLength = result.length();

        if (newLength > length) {
            throw new IllegalStateException("Calculation was wrong at #incrementHexStringBy [oldLength: " + length + ", newLength: " + newLength);
        }

        clearBuilder();

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

    public String incrementHexStringBy(String hexString, String incrementBy) {
        int length = hexString.length();

        String maxValue = Stream.generate(() -> "F").limit(length).collect(Collectors.joining());
        long overflowReference = Long.parseLong(maxValue, 16) + 1;
        long incAsLong = Long.parseLong(incrementBy, 16);

        if (incAsLong > overflowReference) {
            incAsLong = incAsLong % overflowReference;
        }

        long initialResult = Long.parseLong(hexString, 16);
        long finalNum = initialResult + incAsLong;

        if (overflowReference > finalNum) {
            initialResult = finalNum;
        } else if (overflowReference < finalNum) {
            initialResult = finalNum - overflowReference;
        } else {
            initialResult = HeatVisualizerConstants.ZERO_LONG;
        }

        String result = Long.toHexString(initialResult);
        int newLength = result.length();

        if (newLength > length) {
            throw new IllegalStateException("Calculation was wrong at #incrementHexStringBy [oldLength: " + length + ", newLength: " + newLength);
        }

        clearBuilder();

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

    public String incrementIntegerStringBy(String integerString, long incrementBy) {
        int length = integerString.length();

        String maxValue = Stream.generate(() -> "9").limit(length).collect(Collectors.joining());
        long overflowReference = Long.parseLong(maxValue) + 1;

        if (incrementBy > overflowReference) {
            incrementBy = incrementBy % overflowReference;
        }

        long initialResult = Long.parseLong(integerString);
        long finalNum = initialResult + incrementBy;

        if (overflowReference > finalNum) {
            initialResult = finalNum;
        } else if (overflowReference < finalNum) {
            initialResult = finalNum - overflowReference;
        } else {
            initialResult = HeatVisualizerConstants.ZERO_LONG;
        }

        String result = Long.toString(initialResult);
        int newLength = result.length();

        if (newLength > length) {
            throw new IllegalStateException("Calculation was wrong at #incrementHexStringBy [oldLength: " + length + ", newLength: " + newLength);
        }

        clearBuilder();

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
     * Increments 64-hex string by a specified number (1-8 hex characters). Disabled words are left intact.
     * Requested increment range is capped between 0x0 and 0xFFFFFFFF - higher or lower values are not allowed
     * @param seed
     * @param incrementBy
     * @param disabledWords
     * @return
     */
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

        clearBuilder();
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
                replaceBuilderWordAtIndex(incHelperResult.getValue(), highestUnlockedIndex);
                break;
            }

            replaceBuilderWordAtIndex(incHelperResult.getValue(), highestUnlockedIndex);
            incrementBy = incHelperResult.getKey();
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
            initialResult = HeatVisualizerConstants.ZERO_LONG;
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
        if (!HeatVisualizerConstants.PATTERN_SIMPLE_08.matcher(hexWord).matches()) {
            throw new IllegalArgumentException("Method only accepts 8-hex-character string at #decrementWord [received input: " + hexWord + "]");
        }

        long item = Long.parseLong(hexWord, 16);

        if (HeatVisualizerConstants.ZERO_LONG > --item) {
            item = HeatVisualizerConstants.OVERFLOW_REFERENCE_8_HEX - 1;
        }

        return helper.padTo8(Long.toHexString(item), uppercase);
    }

    /**
     * Helper increments a hex word and also returns num of times it overflowed.
     * Since input must be 8 hex words and increment is limited to max 8 hex characters, we can only overflow once.
     * @param hexWord
     * @param incrementBy
     * @return - num of overflows and result
     */
    private Pair<Long, String> incrementWordByHelper(String hexWord, String incrementBy) {
        if (!HeatVisualizerConstants.PATTERN_SIMPLE_08.matcher(hexWord).matches()) {
            throw new IllegalArgumentException("Input must be 8 hex word at #incrementWordByHelper");
        }

        if (!HeatVisualizerConstants.PATTERN_SIMPLE_08_OR_LESS.matcher(incrementBy).matches()) {
            throw new IllegalArgumentException("Increment request must be from '0' to 'FFFFFFFF' at #incrementWordByHelper");
        }

        long start = Long.parseLong(hexWord, 16);
        long incRequest = Long.parseLong(incrementBy, 16);

        long result = start + incRequest;

        if (result < HeatVisualizerConstants.OVERFLOW_REFERENCE_8_HEX) {
            return new Pair<>(0L, helper.padTo8(Long.toHexString(result), true));
        }

        if (result > HeatVisualizerConstants.OVERFLOW_REFERENCE_8_HEX) {
            return new Pair<>(1L, helper.padTo8(Long.toHexString(result - HeatVisualizerConstants.OVERFLOW_REFERENCE_8_HEX), true));
        }

        return new Pair<>(1L, "00000000");
    }

    /**
     * Increments a single hex character. Overflows
     * @param hexChar
     * @return
     */
    private String incrementHexCharacter(String hexChar) {
        if (hexChar.length() != 1) {
            throw new IllegalArgumentException("Method only accepts a string of length 1 at #incrementHexCharacter [received input: " + hexChar + "]");
        }

        if ("f".equals(hexChar) || "F".equals(hexChar)) {
            return HeatVisualizerConstants.ZERO_STRING;
        }
        int num = Integer.parseInt(hexChar, 16);
        return Integer.toHexString(++num);
    }

    /**
     * Decrements a single hex character. Underflows
     * @param hexChar
     * @return
     */
    private String decrementHexCharacter(String hexChar) {
        if (hexChar.length() != 1) {
            throw new IllegalArgumentException("Method only accepts a string of length 1 at #decrementHexCharacter [received input: " + hexChar + "]");
        }

        if (HeatVisualizerConstants.ZERO_STRING.equals(hexChar)) {
            return HeatVisualizerConstants.F_STRING;
        }
        int num = Integer.parseInt(hexChar, 16);
        return Integer.toHexString(--num);
    }

    private void clearBuilder() {
        sb.delete(0, sb.length());
    }

    //vertical rotation is capped at 0x10, so we take the easy way out
    public String rotateSelectedIndexVerticallyBy(String result, List<Integer> disabledWords, Integer index, String value) {
        //if index is disabled - do not engage
        if (disabledWords.contains((index / 8) + 1)) {
            return result;
        }

        int times = Integer.parseInt(value, 16);
        for (int i = 0; i < times; i++) {
            result = rotateSelectedIndexVertically(result, disabledWords, index);
        }
        return result;
    }

    /**
     * Decrements 64-hex string by a specified number (1-8 hex characters). Disabled words are left intact.
     * Requested decrement range is capped between 0x0 and 0xFFFFFFFF - higher or lower values are not allowed
     * @param seed
     * @param decrementBy
     * @param disabledWords
     * @return
     */
    public String decrementPrivAbsoluteBy(String seed, long decrementBy, List<Integer> disabledWords) {
        if (!HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(seed).matches()) {
            throw new IllegalArgumentException("Seed not valid at #decrementPrivAbsoluteBy (must be 64 hex characters), [received=" + seed + "]");
        }
        if (HeatVisualizerConstants.OVERFLOW_REFERENCE_8_HEX <= decrementBy || decrementBy < 0) {
            throw new IllegalArgumentException("Method only accepts decrement requests for values from '0' to 'FFFFFFFF' [received=" + Long.toHexString(decrementBy) + "]");
        }

        if (8 == disabledWords.size()) {
            return seed;
        }

        List<Integer> ignored = new ArrayList<>(disabledWords); //make a copy

        clearBuilder();
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
                replaceBuilderWordAtIndex(decHelperResult.getValue(), highestUnlockedIndex);
                break;
            }

            replaceBuilderWordAtIndex(decHelperResult.getValue(), highestUnlockedIndex);
            decrementBy = decHelperResult.getKey();
            ignored.add(highestUnlockedIndex);
            highestUnlockedIndex = 0; //reset
        }

        return sb.toString().toUpperCase();
    }

    private Pair<Long, String> decrementWordByHelper(String hexWord, String decrementBy) {
        if (!HeatVisualizerConstants.PATTERN_SIMPLE_08.matcher(hexWord).matches()) {
            throw new IllegalArgumentException("Input must be 8 hex word at #decrementWordByHelper");
        }

        if (!HeatVisualizerConstants.PATTERN_SIMPLE_08_OR_LESS.matcher(decrementBy).matches()) {
            throw new IllegalArgumentException("Increment request must be from '0' to 'FFFFFFFF' at #decrementWordByHelper");
        }

        long start = Long.parseLong(hexWord, 16);
        long decRequest = Long.parseLong(decrementBy, 16);

        long result = start - decRequest;


        if (result >= 0L) {
            return new Pair<>(0L, helper.padTo8(Long.toHexString(result), true));
        }

        long numOverflows = 0;
        while (result < 0L) {
            numOverflows++;
            result = result + HeatVisualizerConstants.OVERFLOW_REFERENCE_8_HEX;
        }

        return new Pair<>(numOverflows, helper.padTo8(Long.toHexString(result), true));
    }
}
