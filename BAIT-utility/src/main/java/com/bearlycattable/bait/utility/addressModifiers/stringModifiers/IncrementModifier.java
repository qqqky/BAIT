package com.bearlycattable.bait.utility.addressModifiers.stringModifiers;

import java.util.List;

public interface IncrementModifier {

    String incrementAllWords(String address, List<Integer> disabledWords);

    String incrementWordsBy(String address, long incrementBy, List<Integer> disabledWords);

    String incrementHexStringBy(String hexString, long incrementBy);

    String incrementHexStringBy(String hexString, String incrementBy);

    String incrementIntegerStringBy(String integerString, long incrementBy);

    String incrementPrivAbsolute(String address, List<Integer> disabledWords);

    String incrementPrivAbsoluteBy(String seed, long incrementBy, List<Integer> disabledWords);




}
