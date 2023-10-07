package com.bearlycattable.bait.commons.interfaces;

import java.util.List;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.pubKeyComparison.PubComparisonResultSWrapper;

public interface SubSearchSupport {

    PubComparisonResultSWrapper subSearchInSelectedMode(SearchModeEnum mode, String originalLockedPriv, PubComparisonResultSWrapper currentHighestResult, List<Integer> disabledWords);
}
