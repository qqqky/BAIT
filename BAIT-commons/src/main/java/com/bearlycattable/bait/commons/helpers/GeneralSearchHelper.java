package com.bearlycattable.bait.commons.helpers;

import java.math.BigDecimal;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.commons.contexts.AbstractSearchHelperCreationContext;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;
import com.bearlycattable.bait.commons.other.PubComparer;
import com.bearlycattable.bait.commons.wrappers.PubComparisonResultWrapper;
import com.bearlycattable.bait.commons.validators.PrivKeyValidator;

import lombok.Getter;

public abstract class GeneralSearchHelper {

    private final PubComparer pubComparer = new PubComparer();
    private final PrivKeyValidator privKeyValidator = PrivKeyValidator.newInstance();

    @Getter
    private final ScaleFactorEnum scaleFactor;
    @Getter
    private final int iterations;
    @Getter
    private final Map<Integer, BigDecimal> similarityMappings;

    protected GeneralSearchHelper() {
        throw new UnsupportedOperationException("Creation of AbstractAdvancedSearchHelper without context is not allowed");
    }

    protected GeneralSearchHelper(@NonNull AbstractSearchHelperCreationContext creationContext) {
        this.scaleFactor = creationContext.getScaleFactor();
        this.iterations = creationContext.getIterations();
        this.similarityMappings = creationContext.getSimilarityMappings();
    }

    protected boolean isValidKey(String key) {
        return privKeyValidator.isValidPrivateKey(key);
    }

    protected PubComparisonResultWrapper calculateCurrentResult(String currentPrivKey, String targetPKHUncompressed, String targetPKHCompressed, @NonNull ScaleFactorEnum scaleFactor) {
        return pubComparer.getCurrentResult(currentPrivKey, targetPKHUncompressed, targetPKHCompressed, scaleFactor);
    }


}
