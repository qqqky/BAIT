package com.bearlycattable.bait.advancedCommons.pubKeyComparison;

import com.bearlycattable.bait.advancedCommons.contexts.P2PKHSingleResultData;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;
import com.bearlycattable.bait.commons.pubKeyComparison.PubComparisonResultSWrapper;

public interface AdvancedPubComparerS {

    PubComparisonResultSWrapper calculateCurrentResultCachedS(String currentPrivKey, String[] UPKHArray, String[] CPKHArray, P2PKHSingleResultData data, ScaleFactorEnum scaleFactor);
}
