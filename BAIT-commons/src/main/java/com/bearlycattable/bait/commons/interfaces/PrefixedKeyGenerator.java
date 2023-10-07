package com.bearlycattable.bait.commons.interfaces;

import java.util.List;

import com.bearlycattable.bait.commons.enums.SearchModeEnum;

public interface PrefixedKeyGenerator {

    String buildNextPrivPrefixed(String current, List<Integer> disabledWords, String prefix);

    byte[] buildNextPrivPrefixedBytes(byte[] current, List<Integer> disabledWords, String prefix);
}
