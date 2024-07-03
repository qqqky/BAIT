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
        if (address == null || address.length != 32) {
            return null;
        }

        if (Objects.requireNonNull(disabledWords).containsAll(ALL_WORDS)) {
            return address;
        }

        int currentByte;

        WORD_LOOP: for (int i = 1 ; i < 9; i++) {  //words
            if (disabledWords.contains(i)) {
                continue;
            }

            for (int j = 3; j >= 0; j--) {   //indexes of each word
                currentByte = (i - 1) * 4 + j;

                //increment normally if value is not "FF"
                if (address[currentByte] != -1) {
                    address[currentByte] = (byte) (address[currentByte] + 1);
                    continue WORD_LOOP;
                }

                address[currentByte] = 0;
            }
        }

        return address;
    }


}
