package com.bearlycattable.bait.commons.interfaces;

import java.util.List;;import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.wrappers.PubComparisonResultWrapper;

public interface SubSearchSupport {

    PubComparisonResultWrapper subSearchInSelectedMode(SearchModeEnum mode, String originalLockedPriv, PubComparisonResultWrapper currentHighestResult, List<Integer> disabledWords);
}
