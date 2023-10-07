package com.bearlycattable.bait.utility.addressModifiers.byteModifiers;

import java.util.List;
import java.util.Objects;

public class IncrementModifierBImpl extends AbstractModifierB implements IncrementModifierB {

    @Override
    public byte[] incrementPrivAbsoluteB(byte[] address, List<Integer> disabledWords) {
        if (address == null || address.length != 32) {
            return null;
        }

        if (Objects.requireNonNull(disabledWords).containsAll(ALL_WORDS)) {
            return address;
        }

        int currentWord;

        for (int i = address.length - 1 ; i >= 0; i--) {
            currentWord = ((i + 1) * 2 + 7) / 8;    //from 1 to 8

            if (disabledWords.contains(currentWord)) {
                continue;
            }

            //increment normally if value is not "FF"
            if (address[i] != -1) {
                address[i] = (byte) (address[i] + 1);
                break;
            }

            address[i] = 0;
        }

        return address;
    }

    @Override
    public byte[] incrementPrivWordsB(byte[] address, List<Integer> disabledWords) {
        throw new UnsupportedOperationException("Method not supported at " + this.getClass().getName() + "#incrementPrivWordsB");
    }
}
