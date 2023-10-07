package com.bearlycattable.bait.commons.contexts;

import com.bearlycattable.bait.commons.enums.QuickSearchResponseEnum;
import com.bearlycattable.bait.commons.pubKeyComparison.PubComparisonResultSWrapper;

import lombok.Data;

@Data
public class QuickSearchResponseModel {
    private QuickSearchResponseEnum responseCommand;
    private PubComparisonResultSWrapper highestResult;
}
