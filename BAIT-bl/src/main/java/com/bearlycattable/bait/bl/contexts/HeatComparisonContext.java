package com.bearlycattable.bait.bl.contexts;

import com.bearlycattable.bait.commons.enums.QuickSearchComparisonType;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class HeatComparisonContext {

    private final String targetPK;
    private final String referenceKey;

    private final ScaleFactorEnum scaleFactor;

    private final QuickSearchComparisonType comparisonType;
}
