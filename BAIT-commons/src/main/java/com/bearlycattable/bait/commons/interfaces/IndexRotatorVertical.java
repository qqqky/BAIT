package com.bearlycattable.bait.commons.interfaces;

import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.commons.enums.SearchModeEnum;

public interface IndexRotatorVertical {

    @NonNull
    String rotateAtIndex(String current, List<Integer> disabledWords, int index);

    boolean isValidIndexForVerticalRotation(String address, List<Integer> disabledWords, int selectedIndex);
}
