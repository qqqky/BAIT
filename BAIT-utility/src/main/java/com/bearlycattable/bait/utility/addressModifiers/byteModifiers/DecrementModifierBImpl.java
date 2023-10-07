package com.bearlycattable.bait.utility.addressModifiers.byteModifiers;

import java.util.List;

public class DecrementModifierBImpl extends AbstractModifierB implements DecrementModifierB {

    @Override
    public byte[] decrementPrivAbsoluteB(byte[] address, List<Integer> disabledWords) {
        throw new UnsupportedOperationException("Method not supported at " + this.getClass().getName() + "#decrementPrivAbsoluteB");
    }

    @Override
    public byte[] decrementPrivWordsB(byte[] address, List<Integer> disabledWords) {
        throw new UnsupportedOperationException("Method not supported at " + this.getClass().getName() + "#decrementPrivWordsB");
    }
}
