package com.bearlycattable.bait.utility.addressModifiers.stringModifiers;

import java.util.List;

public interface FuzzingModifier {

    String fuzzSelectedWords(String address, List<Integer> disabledWords);

}
