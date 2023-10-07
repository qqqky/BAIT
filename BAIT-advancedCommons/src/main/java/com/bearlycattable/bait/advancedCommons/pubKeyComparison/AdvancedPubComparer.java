package com.bearlycattable.bait.advancedCommons.pubKeyComparison;

import org.checkerframework.checker.nullness.qual.NonNull;

public interface AdvancedPubComparer {

    void comparePubKeyHashes(@NonNull AdvancedPubComparisonResultB model);

    void comparePubKeyHashesCached(@NonNull AdvancedPubComparisonResultB model);
}
