package com.bearlycattable.bait.advancedCommons.pubKeyComparison;

import java.util.Optional;

import com.bearlycattable.bait.advancedCommons.contexts.P2PKHSingleResultData;
import com.bearlycattable.bait.commons.HeatVisualizerConstants;
import com.bearlycattable.bait.commons.enums.JsonResultScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.AddressGenerationAndComparisonType;
import com.bearlycattable.bait.commons.enums.PubTypeEnum;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;
import com.bearlycattable.bait.commons.pubKeyComparison.PubComparisonResultS;
import com.bearlycattable.bait.commons.pubKeyComparison.PubComparisonResultSWrapper;

public class AdvancedPubComparerSImpl implements AdvancedPubComparerS {

    private Optional<PubComparisonResultS> comparePubKeyHashesCachedS(PubTypeEnum type, String[] currentPKHArray, P2PKHSingleResultData data) {
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

        return Optional.of(PubComparisonResultS.builder()
                .positive(pointCountPositive)
                .negative(pointCountNegative)
                .type(type)
                .build());
    }

    @Override
    public PubComparisonResultSWrapper calculateCurrentResultCachedS(String currentPrivKey, String[] UPKHArray, String[] CPKHArray, P2PKHSingleResultData data, ScaleFactorEnum scaleFactor) {
        JsonResultScaleFactorEnum requestedScaleFactor = ScaleFactorEnum.toJsonScaleFactorEnum(scaleFactor);

        if (!data.isGeneralPointsCachedForScaleFactor(requestedScaleFactor, AddressGenerationAndComparisonType.STRING)) {
            throw new IllegalStateException("Data collection is not cached for the requested scale factor [requested:" + requestedScaleFactor + ", actual: " + data.getCachedForScaleFactor() + "]");
        }

        PubComparisonResultS resultUncompressed = getCurrentResultForUncompressedCached(currentPrivKey, UPKHArray, data).orElse(null);
        PubComparisonResultS resultCompressed = getCurrentResultForCompressedCached(currentPrivKey, CPKHArray, data).orElse(null);

        if (resultUncompressed == null || resultCompressed == null) {
            return HeatVisualizerConstants.EMPTY_RESULT_WRAPPER;
        }

        return PubComparisonResultSWrapper.builder()
                .resultForUncompressed(resultUncompressed)
                .resultForCompressed(resultCompressed)
                .build();
    }

    private Optional<PubComparisonResultS> getCurrentResultForUncompressedCached(String currentPrivKey, String[] UPKHArray, P2PKHSingleResultData data) {
        Optional<PubComparisonResultS> result = comparePubKeyHashesCachedS(PubTypeEnum.UNCOMPRESSED, UPKHArray, data);
        result.ifPresent(res -> res.setForPriv(currentPrivKey));
        return result;
    }

    private Optional<PubComparisonResultS> getCurrentResultForCompressedCached(String currentPrivKey, String[] CPKHArray, P2PKHSingleResultData data) {
        Optional<PubComparisonResultS> result = comparePubKeyHashesCachedS(PubTypeEnum.COMPRESSED, CPKHArray, data);
        result.ifPresent(res -> res.setForPriv(currentPrivKey));
        return result;
    }
}
