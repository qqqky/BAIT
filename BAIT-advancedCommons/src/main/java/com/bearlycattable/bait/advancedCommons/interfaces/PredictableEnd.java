package com.bearlycattable.bait.advancedCommons.interfaces;

import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;

public interface PredictableEnd {

    @NonNull
    String predictLastItem(@NonNull String seed, int iterations, List<Integer> disabledWords);
}
