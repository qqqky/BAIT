package com.bearlycattable.bait.commons.interfaces;

import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.commons.enums.SearchModeEnum;

public interface IndexRotatorHorizontal {

    @NonNull
    String rotateLeft(String current);

    @NonNull
    String rotateLeftBy(String current, int rotateBy);

    @NonNull
    String rotateLeft(String current, List<Integer> disabledWords);

    @NonNull
    String rotateLeftBy(String current, int rotateBy, List<Integer> disabledWords);
}
