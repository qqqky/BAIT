package com.bearlycattable.bait.utility.addressModifiers.stringModifiers;

import java.util.List;

public interface HRotatorModifier {

    String rotateAddressLeftBy(String address, int rotateBy, boolean withInternalPrefix);

    String rotateAddressRightBy(String address, int rotateBy, boolean withInternalPrefix);

    String rotateAllWordsBy(String address, int rotateBy, List<Integer> disabledWords);


}
