package com.bearlycattable.bait.commons.interfaces;

import java.util.List;

public interface PrefixedKeyGenerator {

    String buildNextPriv(String current, List<Integer> disabledWords, String prefix);
}
