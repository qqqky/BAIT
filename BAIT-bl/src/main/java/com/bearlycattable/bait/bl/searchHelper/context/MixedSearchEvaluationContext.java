package com.bearlycattable.bait.bl.searchHelper.context;

import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.interfaces.SimpleSearchHelper;
import com.bearlycattable.bait.commons.wrappers.PubComparisonResultWrapper;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MixedSearchEvaluationContext {

    PubComparisonResultWrapper subResult;
    PubComparisonResultWrapper highest;
    String lockedPKHUncompressed;
    String lockedPKHCompressed;
    SimpleSearchHelper currentSearcher;
    SearchModeEnum searchMode;
    int iteration;
    int accuracy;
}
