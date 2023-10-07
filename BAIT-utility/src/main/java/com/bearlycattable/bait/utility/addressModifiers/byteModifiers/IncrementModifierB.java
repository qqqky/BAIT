package com.bearlycattable.bait.utility.addressModifiers.byteModifiers;

import java.util.List;

public interface IncrementModifierB {

    byte[] incrementPrivAbsoluteB(byte[] address, List<Integer> disabledWords);

    byte[] incrementPrivWordsB(byte[] address, List<Integer> disabledWords);
}
