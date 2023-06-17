package com.bearlycattable.bait.commons.contexts;

import com.bearlycattable.bait.commons.enums.QuickSearchResponseEnum;
import com.bearlycattable.bait.commons.wrappers.PubComparisonResultWrapper;

import lombok.Data;

@Data
public class QuickSearchResponseModel {
    private QuickSearchResponseEnum responseCommand;
    private PubComparisonResultWrapper highestResult;
}
