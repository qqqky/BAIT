package com.bearlycattable.bait.advancedCommons.pubKeyComparison;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.advancedCommons.contexts.P2PKHSingleResultData;
import com.bearlycattable.bait.commons.HeatVisualizerConstants;
import com.bearlycattable.bait.commons.enums.HeatOverflowTypeEnum;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;

public class AdvancedPubComparerBImpl implements AdvancedPubComparerB {

    private static final int IGNORED_HEAT_DIFFERENCE = 2;
    private static final int OVERFLOW_REFERENCE = HeatVisualizerConstants.OVERFLOW_REFERENCE_1_HEX;
    private static final Map<BigDecimal, Map<Integer, BigDecimal>> SCALED_MULTIPLIER_MAPPINGS = initScaledMultiplierMappings();
    private static final Map<Integer, Map<BigDecimal, Integer>> FINAL_POINTS_MAPPINGS = initFinalPointsMappings();

    private static Map<BigDecimal, Map<Integer, BigDecimal>> initScaledMultiplierMappings() {
        Map<BigDecimal, Map<Integer, BigDecimal>> map = new HashMap<>();
        for (ScaleFactorEnum scaleFactor : ScaleFactorEnum.values()) {
            BigDecimal mainKey = scaleFactor.getScaleFactor();
            map.put(mainKey, new HashMap<>());
            for (int basePoints = IGNORED_HEAT_DIFFERENCE; basePoints <= 0x10; basePoints++) {
                map.get(mainKey).put(basePoints, scaleFactor.getScaleFactor().pow(basePoints - IGNORED_HEAT_DIFFERENCE).setScale(5, RoundingMode.HALF_UP));
            }
        }
        return map;
    }

    // private static Map<BigDecimal, Map<Integer, BigDecimal>> initScaled2CharMultiplierMappings() {
    //     Map<BigDecimal, Map<Integer, BigDecimal>> map = new HashMap<>();
    //     for (ScaleFactorEnum scaleFactor : ScaleFactorEnum.values()) {
    //         BigDecimal mainKey = scaleFactor.getScaleFactor();
    //         map.put(mainKey, new HashMap<>());
    //         for (int basePoints = IGNORED_HEAT_DIFFERENCE; basePoints <= 0x100; basePoints++) {
    //             // AA               170
    //             // 10 - reference   16
    //             // 9 and 10 (for pos)  OR  7 and 6 (for neg)    -- 19 or 13 total (unscaled) points when summed
    //             // would be: 154 (for pos) OR -154 (for neg)    //but it wouldn't be based on each index anymore - can't use!
    //
    //             // rather we must calculate for EACH of 256 numbers and sum up:
    //
    //             // AA
    //             // 1A - reference
    //             // 9 and 16 (for pos) OR 7 and 16 (for neg)
    //             map.get(mainKey).put(basePoints, scaleFactor.getScaleFactor().pow(basePoints - IGNORED_HEAT_DIFFERENCE).setScale(5, RoundingMode.HALF_UP));
    //         }
    //     }
    //     return map;
    // }

    private static Map<Integer, Map<BigDecimal, Integer>> initFinalPointsMappings() {
        Map<Integer, Map<BigDecimal, Integer>> map = new HashMap<>();
        for (int basePoints = IGNORED_HEAT_DIFFERENCE; basePoints <= 0x10; basePoints++) { //up to 16
            map.put(basePoints, new HashMap<>());
            for (ScaleFactorEnum scaleFactor : ScaleFactorEnum.values()) {
                BigDecimal scaleFactorValue = scaleFactor.getScaleFactor();
                BigDecimal scaledMultiplier = SCALED_MULTIPLIER_MAPPINGS.get(scaleFactorValue).get(basePoints);
                BigDecimal finalPoints = new BigDecimal(basePoints).multiply(scaledMultiplier).setScale(0, RoundingMode.HALF_UP);

                //base points received -> on which scale factor -> how much points we get after applying the scale factor
                map.get(basePoints).put(scaleFactorValue, finalPoints.intValue());
            }
        }
        return map;
    }

    @Override
    public void comparePubKeyHashesB(@NonNull AdvancedPubComparisonResultB resultModel) {
        Objects.requireNonNull(resultModel).validateForBlindComparison(); //TODO: this could maybe be removed later

        //unpack
        byte[] referencePKH = resultModel.getReferencePKH();
        byte[] currentUPKH = resultModel.getCurrentUPKH();
        byte[] currentCPKH = resultModel.getCurrentCPKH();
        BigDecimal pointsMultiplier = resultModel.getScaleFactorOrDefault().getScaleFactor();

        int pointCountPosUPKH = 0;
        int pointCountNegUPKH = 0;
        int pointCountPosCPKH = 0;
        int pointCountNegCPKH = 0;

        int firstNibbleUPKH;
        int firstNibbleCPKH;
        int secondNibbleUPKH;
        int secondNibbleCPKH;
        int referenceNibbleFirst;
        int referenceNibbleSecond;

        int differenceFirstUPKH;
        int differenceFirstCPKH;
        int differenceSecondUPKH;
        int differenceSecondCPKH;

        for (int i = 0; i < referencePKH.length; i++) {
            referenceNibbleFirst = (referencePKH[i] & 0xF0) >>> 4;
            referenceNibbleSecond = (referencePKH[i] & 0x0F);

            firstNibbleUPKH = (currentUPKH[i] & 0xF0) >>> 4;
            firstNibbleCPKH = (currentCPKH[i] & 0xF0) >>> 4;

            secondNibbleUPKH = (currentUPKH[i] & 0x0F);
            secondNibbleCPKH = (currentCPKH[i] & 0x0F);

            differenceFirstUPKH = firstNibbleUPKH - referenceNibbleFirst;
            differenceFirstCPKH = firstNibbleCPKH - referenceNibbleFirst;
            differenceSecondUPKH = secondNibbleUPKH - referenceNibbleSecond;
            differenceSecondCPKH = secondNibbleCPKH - referenceNibbleSecond;

            pointCountPosUPKH += calculateCurrentPointsPositive(differenceFirstUPKH, differenceSecondUPKH, OVERFLOW_REFERENCE, pointsMultiplier);
            pointCountNegUPKH += calculateCurrentPointsNegative(differenceFirstUPKH, differenceSecondUPKH, OVERFLOW_REFERENCE, pointsMultiplier);
            pointCountPosCPKH += calculateCurrentPointsPositive(differenceFirstCPKH, differenceSecondCPKH, OVERFLOW_REFERENCE, pointsMultiplier);
            pointCountNegCPKH += calculateCurrentPointsNegative(differenceFirstCPKH, differenceSecondCPKH, OVERFLOW_REFERENCE, pointsMultiplier);
        }

        resultModel.setPointsPosUPKH(pointCountPosUPKH);
        resultModel.setPointsNegUPKH(pointCountNegUPKH);
        resultModel.setPointsPosCPKH(pointCountPosCPKH);
        resultModel.setPointsNegCPKH(pointCountNegCPKH);
    }

    @Override
    public void comparePubKeyHashesCachedB(@NonNull AdvancedSearchSingleItemComparisonModel fullModel) {
        AdvancedPubComparisonResultB resultModel = fullModel.getCurrentByteComparisonModel();
        P2PKHSingleResultData data = fullModel.getResultContainer();
        Objects.requireNonNull(resultModel).validateForBlindComparison(); //TODO: this could maybe be removed later

        //unpack
        byte[] currentUPKH = resultModel.getCurrentUPKH();
        byte[] currentCPKH = resultModel.getCurrentCPKH();

        int pointCountPosUPKH = 0;
        int pointCountNegUPKH = 0;
        int pointCountPosCPKH = 0;
        int pointCountNegCPKH = 0;

        for (int i = 0; i < 20; i++) {
            pointCountPosUPKH += data.getFromCache(i, currentUPKH[i], true);
            pointCountNegUPKH += data.getFromCache(i, currentUPKH[i], false);
            pointCountPosCPKH += data.getFromCache(i, currentCPKH[i], true);
            pointCountNegCPKH += data.getFromCache(i, currentCPKH[i], false);
        }

        resultModel.setPointsPosUPKH(pointCountPosUPKH);
        resultModel.setPointsNegUPKH(pointCountNegUPKH);
        resultModel.setPointsPosCPKH(pointCountPosCPKH);
        resultModel.setPointsNegCPKH(pointCountNegCPKH);
    }

    @Override
    public int cacheHelperB(int differenceFirstNibble, int differenceSecondNibble, int overflow_reference, BigDecimal pointsMultiplier, HeatOverflowTypeEnum heatType) {
        Objects.requireNonNull(heatType);

        switch (heatType) {
                case HEAT_POSITIVE:
                    return calculateCurrentPointsPositive(differenceFirstNibble, differenceSecondNibble, overflow_reference, pointsMultiplier);
                case HEAT_NEGATIVE:
                    return calculateCurrentPointsNegative(differenceFirstNibble, differenceSecondNibble, overflow_reference, pointsMultiplier);
                default:
                    throw new IllegalArgumentException("Heat type not supported at #cacheHelperB [type= " + heatType + "]");
        }
    }

    private int calculateCurrentPointsPositive(int differenceFirstNibble, int differenceSecondNibble, int overflow_reference, BigDecimal pointsMultiplier) {
        return fetchWithMultiplierApplied(countPointsPositiveHeat(differenceFirstNibble, overflow_reference), pointsMultiplier)
                + fetchWithMultiplierApplied(countPointsPositiveHeat(differenceSecondNibble, overflow_reference), pointsMultiplier);
    }

    private int calculateCurrentPointsNegative(int differenceFirstNibble, int differenceSecondNibble, int overflow_reference, BigDecimal pointsMultiplier) {
        return fetchWithMultiplierApplied(countPointsNegativeHeat(differenceFirstNibble, overflow_reference), pointsMultiplier)
                + fetchWithMultiplierApplied(countPointsNegativeHeat(differenceSecondNibble, overflow_reference), pointsMultiplier);
    }

    private int countPointsNegativeHeat(int difference, int overflow_reference) {
        if (difference > 0) {
            return difference;
        }

        return difference < 0 ? overflow_reference + difference : overflow_reference;
    }

    private int countPointsPositiveHeat(int difference, int overflow_reference) {
        if (difference > 0) {
            return overflow_reference - difference;
        }

        return difference < 0 ? -difference : overflow_reference;
    }

    public int fetchWithMultiplierApplied(int totalPoints, @NonNull BigDecimal pointsMultiplier) {
        assert totalPoints > 0 : "Points cannot be less than 1 at #calculateWithMultiplier";

        if (totalPoints < IGNORED_HEAT_DIFFERENCE) {
            return totalPoints;
        }

        return FINAL_POINTS_MAPPINGS.get(totalPoints).get(pointsMultiplier);
    }
}