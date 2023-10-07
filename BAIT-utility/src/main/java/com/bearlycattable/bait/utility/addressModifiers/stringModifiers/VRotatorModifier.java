package com.bearlycattable.bait.utility.addressModifiers.stringModifiers;

import java.util.List;

public interface VRotatorModifier {

    String rotateSelectedIndexVertically(String address, List<Integer> disabledWords, int selectedIndex);

    String rotateSelectedIndexVerticallyBy(String result, List<Integer> disabledWords, Integer index, String value);

    boolean isValidIndexForVerticalRotation(String address, List<Integer> disabledWords, int selectedIndex);


}
