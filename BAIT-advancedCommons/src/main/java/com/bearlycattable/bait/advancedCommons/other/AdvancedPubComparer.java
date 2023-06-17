package com.bearlycattable.bait.advancedCommons.other;

import java.util.Optional;

import com.bearlycattable.bait.advancedCommons.contexts.P2PKHSingleResultData;
import com.bearlycattable.bait.commons.HeatVisualizerConstants;
import com.bearlycattable.bait.commons.enums.JsonResultScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.PubTypeEnum;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;
import com.bearlycattable.bait.commons.other.PubComparisonResult;
import com.bearlycattable.bait.commons.wrappers.PubComparisonResultWrapper;

public class AdvancedPubComparer {

    private Optional<PubComparisonResult> comparePubKeyHashesCached(PubTypeEnum type, String[] currentPKHArray, P2PKHSingleResultData data) {
        if (currentPKHArray == null || currentPKHArray.length == 0) {
            return Optional.empty();
        }

        if (currentPKHArray.length != 40) {
            throw new IllegalStateException("Passed array must be of length 40 at PubComparer#comparePubKeyHashesCached");
        }

        int pointCountPositive = 0;
        int pointCountNegative = 0;
        String currentChar;

        for (int i = 0; i < 40; i++) {
            currentChar = currentPKHArray[i];
            int pointsPositive = data.getCachedHeatComparisonPointsForPositive(currentChar, i);
            int pointsNegative = data.getCachedHeatComparisonPointsForNegative(currentChar, i);

            pointCountPositive = pointCountPositive + pointsPositive;
            pointCountNegative = pointCountNegative + pointsNegative;
        }

        return Optional.of(PubComparisonResult.builder()
                .positive(pointCountPositive)
                .negative(pointCountNegative)
                .type(type)
                .build());
    }

    public PubComparisonResultWrapper getCurrentResultCached(String currentPrivKey, String[] UPKHArray, String[] CPKHArray, P2PKHSingleResultData data, ScaleFactorEnum scaleFactor) {
        JsonResultScaleFactorEnum requestedScaleFactor = ScaleFactorEnum.toJsonScaleFactorEnum(scaleFactor);

        if (!data.isGeneralPointsCachedForScaleFactor(requestedScaleFactor)) {
            throw new IllegalStateException("Data collection is not cached for the requested scale factor [requested:" + requestedScaleFactor + ", actual: " + data.getCachedForScaleFactor() + "]");
        }

        PubComparisonResult resultUncompressed = getCurrentResultForUncompressedCached(currentPrivKey, UPKHArray, data).orElse(null);
        PubComparisonResult resultCompressed = getCurrentResultForCompressedCached(currentPrivKey, CPKHArray, data).orElse(null);

        if (resultUncompressed == null || resultCompressed == null) {
            return HeatVisualizerConstants.EMPTY_RESULT_WRAPPER;
        }

        return PubComparisonResultWrapper.builder()
                .resultForUncompressed(resultUncompressed)
                .resultForCompressed(resultCompressed)
                .build();
    }

    private Optional<PubComparisonResult> getCurrentResultForUncompressedCached(String currentPrivKey, String[] UPKHArray, P2PKHSingleResultData data) {
        Optional<PubComparisonResult> result = comparePubKeyHashesCached(PubTypeEnum.UNCOMPRESSED, UPKHArray, data);
        result.ifPresent(res -> res.setForPriv(currentPrivKey));
        return result;
    }

    private Optional<PubComparisonResult> getCurrentResultForCompressedCached(String currentPrivKey, String[] CPKHArray, P2PKHSingleResultData data) {
        Optional<PubComparisonResult> result = comparePubKeyHashesCached(PubTypeEnum.COMPRESSED, CPKHArray, data);
        result.ifPresent(res -> res.setForPriv(currentPrivKey));
        return result;
    }



}
