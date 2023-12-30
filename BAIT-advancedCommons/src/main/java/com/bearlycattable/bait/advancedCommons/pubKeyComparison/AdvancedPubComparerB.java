package com.bearlycattable.bait.advancedCommons.pubKeyComparison;

import java.math.BigDecimal;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.commons.enums.HeatOverflowTypeEnum;

public interface AdvancedPubComparerB {

    void comparePubKeyHashesB(@NonNull AdvancedPubComparisonResultB model);

    void comparePubKeyHashesCachedB(@NonNull AdvancedSearchSingleItemComparisonModel fullModel);

    int cacheHelperB(int differenceFirstNibble, int differenceSecondNibble, int overflow_reference, BigDecimal pointsMultiplier, HeatOverflowTypeEnum heatType);
}
