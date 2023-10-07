package com.bearlycattable.bait.utility.addressModifiers.stringModifiers;

import java.util.List;
import java.util.logging.Logger;

import com.bearlycattable.bait.commons.HeatVisualizerConstants;
import com.bearlycattable.bait.commons.enums.OutputCaseEnum;

public class HRotatorModifierImpl extends AbstractModifier implements HRotatorModifier {

    private static final Logger LOG = Logger.getLogger(HRotatorModifierImpl.class.getName());
    private final StringBuilder sb = new StringBuilder();
    private final boolean uppercase;

    public HRotatorModifierImpl(OutputCaseEnum letterCase) {
        this.uppercase = OutputCaseEnum.UPPERCASE == letterCase;
    }

    @Override
    public String rotateAddressLeftBy(String address, int rotateBy, boolean withInternalHeader) {
        return withInternalHeader ? rotateLeftBy((HeatVisualizerConstants.PRIV_HEADER + address), rotateBy > 128 ? rotateBy % 128 : rotateBy).substring(64) : rotateLeftBy(address, rotateBy > 64 ? rotateBy % 64 : rotateBy);
    }

    @Override
    public String rotateAddressRightBy(String address, int rotateBy, boolean withInternalHeader) {
        return withInternalHeader ? rotateRightBy((HeatVisualizerConstants.PRIV_HEADER + address), rotateBy > 128 ? rotateBy % 128 : rotateBy).substring(64) : rotateRightBy(address, rotateBy > 64 ? rotateBy % 64 : rotateBy);
    }

    /**
     * Rotates selected words of a 64-hex address by specified number (left circular rotation)
     * After 8 rotations address is back to original.
     * @param address
     * @param rotateBy
     * @param disabledWords
     * @return resulting 64-hex address after word rotation
     */
    @Override
    public String rotateAllWordsBy(String address, int rotateBy, List<Integer> disabledWords) {
        if (!HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(address).matches()) {
            return null;
        }

        if (rotateBy > 8) {
            rotateBy = rotateBy % 8;
        }

        clearBuilder(sb);

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
}
