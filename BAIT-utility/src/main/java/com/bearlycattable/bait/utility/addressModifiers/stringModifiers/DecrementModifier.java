package com.bearlycattable.bait.utility.addressModifiers.stringModifiers;

import java.util.List;

public interface DecrementModifier {

    String decrementAllWords(String address, List<Integer> disabledWords);

    String decrementWordsBy(String address, long decrementBy, List<Integer> disabledWords);

    String decrementPrivAbsolute(String address, List<Integer> disabledWords);

    String decrementPrivAbsoluteBy(String seed, long decrementBy, List<Integer> disabledWords);

    String decrementHexStringBy(String hexString, long decrementBy);
}
