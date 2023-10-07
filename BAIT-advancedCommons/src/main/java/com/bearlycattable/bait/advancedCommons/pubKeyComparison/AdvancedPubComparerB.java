package com.bearlycattable.bait.advancedCommons.pubKeyComparison;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.commons.HeatVisualizerConstants;
import com.bearlycattable.bait.commons.enums.HeatOverflowTypeEnum;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;

public class AdvancedPubComparerB {

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

    public void comparePubKeyHashesB(@NonNull AdvancedPubComparisonResultB model) {
        Objects.requireNonNull(model);
        model.validateForBlindComparison(); //TODO: this could maybe be removed later

        //unpack
        byte[] referencePKH = model.getReferencePKH();
        byte[] currentUPKH = model.getCurrentUPKH();
        byte[] currentCPKH = model.getCurrentCPKH();
        BigDecimal pointsMultiplier = model.getScaleFactorOrDefault().getScaleFactor();

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

        model.setPointsPosUPKH(pointCountPosUPKH);
        model.setPointsNegUPKH(pointCountNegUPKH);
        model.setPointsPosCPKH(pointCountPosCPKH);
        model.setPointsNegCPKH(pointCountNegCPKH);
    }

    public void comparePubKeyHashesCachedB(AdvancedPubComparisonResultB model) {
        throw new IllegalStateException("//dev");
    }

    // private int calculateFirstNibbleUPKH(byte valUPKH, int referenceNibbleFirst) {
    //     return ((valUPKH & 0xF0) >>> 4) - referenceNibbleFirst;
    // }

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
        return calculateWithMultiplier(countPointsPositive(differenceFirstNibble, overflow_reference), pointsMultiplier)
                + calculateWithMultiplier(countPointsPositive(differenceSecondNibble, overflow_reference), pointsMultiplier);
    }

    private int calculateCurrentPointsNegative(int differenceFirstNibble, int differenceSecondNibble, int overflow_reference, BigDecimal pointsMultiplier) {
        return calculateWithMultiplier(countPointsNegative(differenceFirstNibble, overflow_reference), pointsMultiplier)
                + calculateWithMultiplier(countPointsNegative(differenceSecondNibble, overflow_reference), pointsMultiplier);
    }

    private int countPointsNegative(int difference, int overflow_reference) {
        if (difference > 0) {
            return difference;
        }

        return difference < 0 ? overflow_reference + difference : overflow_reference;
    }

    private int countPointsPositive(int difference, int overflow_reference) {
        if (difference > 0) {
            return overflow_reference - difference;
        }

        return difference < 0 ? -difference : overflow_reference;
    }

    public int calculateWithMultiplier(int totalPoints, @NonNull BigDecimal pointsMultiplier) {
        assert totalPoints > 0 : "Points cannot be less than 1 at #calculateWithMultiplier";

        if (totalPoints < IGNORED_HEAT_DIFFERENCE) {
            return totalPoints;
        }

        return FINAL_POINTS_MAPPINGS.get(totalPoints).get(pointsMultiplier);
    }
}