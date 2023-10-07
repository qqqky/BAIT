package com.bearlycattable.bait.commons.helpers;

import java.math.BigDecimal;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.commons.contexts.AbstractSearchHelperCreationContext;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;
import com.bearlycattable.bait.commons.pubKeyComparison.PubComparerS;
import com.bearlycattable.bait.commons.pubKeyComparison.PubComparisonResultSWrapper;
import com.bearlycattable.bait.commons.validators.PrivKeyValidator;

import lombok.Getter;

public abstract class AbstractGeneralSearchHelper {

    private final PubComparerS pubComparer = new PubComparerS();
    private final PrivKeyValidator privKeyValidator = PrivKeyValidator.newInstance();

    @Getter
    private final ScaleFactorEnum scaleFactor;
    @Getter
    private final int iterations;
    @Getter
    private final Map<Integer, BigDecimal> similarityMappings;

    protected AbstractGeneralSearchHelper() {
        throw new UnsupportedOperationException("Creation of AbstractAdvancedSearchHelper without context is not allowed");
    }

    protected AbstractGeneralSearchHelper(@NonNull AbstractSearchHelperCreationContext creationContext) {
        this.scaleFactor = creationContext.getScaleFactor();
        this.iterations = creationContext.getIterations();
        this.similarityMappings = creationContext.getSimilarityMappings();
    }

    protected boolean isValidKey(String key) {
        return privKeyValidator.isValidPrivateKey(key);
    }

    protected boolean isValidKey(byte[] key) {
        return privKeyValidator.isValidPrivateKey(key);
    }

    protected PubComparisonResultSWrapper calculateCurrentResult(String currentPrivKey, String targetPKHUncompressed, String targetPKHCompressed, @NonNull ScaleFactorEnum scaleFactor) {
        return pubComparer.getCurrentResult(currentPrivKey, targetPKHUncompressed, targetPKHCompressed, scaleFactor);
    }
}
