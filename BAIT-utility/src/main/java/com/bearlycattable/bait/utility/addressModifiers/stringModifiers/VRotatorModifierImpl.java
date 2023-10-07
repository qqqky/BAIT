package com.bearlycattable.bait.utility.addressModifiers.stringModifiers;

import java.util.List;
import java.util.logging.Logger;

import com.bearlycattable.bait.commons.HeatVisualizerConstants;
import com.bearlycattable.bait.commons.enums.OutputCaseEnum;

public class VRotatorModifierImpl extends AbstractModifier implements VRotatorModifier {

    private static final Logger LOG = Logger.getLogger(VRotatorModifierImpl.class.getName());
    private final StringBuilder sb = new StringBuilder();
    private final boolean uppercase;

    public VRotatorModifierImpl(OutputCaseEnum letterCase) {
        this.uppercase = OutputCaseEnum.UPPERCASE == letterCase;
    }

    @Override
    public String rotateSelectedIndexVertically(String address, List<Integer> disabledWords, int selectedIndex) {
        if (!isValidIndexForVerticalRotation(address, disabledWords, selectedIndex)) {
            return null;
        }

        clearBuilder(sb);
        sb.append(address);

        String incrementedChar = incrementHexCharacter(String.valueOf(address.charAt(selectedIndex))).toUpperCase();
        if (!HeatVisualizerConstants.PATTERN_HEX_01.matcher(incrementedChar).matches()) {
            throw new IllegalStateException("at VRotatorModifier#rotateSelectedIndexVertically. Incremented result was: " + incrementedChar);
        }
        sb.replace(selectedIndex, selectedIndex + 1, incrementedChar);

        return sb.toString();
    }

    //vertical rotation is capped at 0x10, so we take the easy way out
    @Override
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

    @Override
    public boolean isValidIndexForVerticalRotation(String address, List<Integer> disabledWords, int selectedIndex) {
        if (address == null || address.length() <= selectedIndex || address.length() != 64) {
            return false;
        }

        return disabledWords.stream()
                .noneMatch(disabledWord ->
                        (selectedIndex >= (disabledWord - 1) * 8)
                                && (selectedIndex < ((disabledWord - 1) * 8) + 8));
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
}
