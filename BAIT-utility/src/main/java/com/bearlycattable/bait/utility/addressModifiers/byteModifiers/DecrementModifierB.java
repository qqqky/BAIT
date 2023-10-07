package com.bearlycattable.bait.utility.addressModifiers.byteModifiers;

import java.util.List;

public interface DecrementModifierB {

    byte[] decrementPrivAbsoluteB(byte[] address, List<Integer> disabledWords);

    byte[] decrementPrivWordsB(byte[] address, List<Integer> disabledWords);

}
