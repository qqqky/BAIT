package com.bearlycattable.bait.commons.interfaces;

import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.commons.enums.SearchModeEnum;

public interface CustomKeyGenerator {

    /**
     * Build next private key in the sequence
     * @param current
     * @param disabledWords
     * @return
     */
    @NonNull
    String buildNextPriv(String current, List<Integer> disabledWords);

    byte[] buildNextPrivBytes(byte[] current, List<Integer> disabledWords);
}
