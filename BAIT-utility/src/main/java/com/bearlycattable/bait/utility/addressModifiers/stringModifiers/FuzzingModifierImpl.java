package com.bearlycattable.bait.utility.addressModifiers.stringModifiers;

import java.util.List;
import java.util.logging.Logger;

import com.bearlycattable.bait.commons.HeatVisualizerConstants;
import com.bearlycattable.bait.commons.enums.OutputCaseEnum;

public class FuzzingModifierImpl extends AbstractModifier implements FuzzingModifier {

    private static final Logger LOG = Logger.getLogger(FuzzingModifierImpl.class.getName());
    private final StringBuilder sb = new StringBuilder();
    private final boolean uppercase;

    public FuzzingModifierImpl(OutputCaseEnum letterCase) {
        this.uppercase = OutputCaseEnum.UPPERCASE == letterCase;
    }

    @Override
    public String fuzzSelectedWords(String address, List<Integer> disabledWords) {
        if (!HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(address).matches()) {
            return null;
        }

        clearBuilder(sb);

        for (int i = 1; i <= 8; i++) {
            if (disabledWords.contains(i)) {
                sb.append(extractCurrentWordFromAddress(address, i));
            } else {
                sb.append(generator.generateHexString(8));
            }
        }
        return sb.toString().toUpperCase();
    }

}
