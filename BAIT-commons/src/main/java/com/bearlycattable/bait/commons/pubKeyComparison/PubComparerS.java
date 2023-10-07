package com.bearlycattable.bait.commons.pubKeyComparison;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.bearlycattable.bait.commons.Config;
import com.bearlycattable.bait.commons.HeatVisualizerConstants;
import com.bearlycattable.bait.commons.enums.HeatOverflowTypeEnum;
import com.bearlycattable.bait.commons.enums.PubTypeEnum;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;
import com.bearlycattable.bait.commons.helpers.HeatVisualizerHelper;

public class PubComparerS {

    private static final Logger LOG = Logger.getLogger(PubComparerS.class.getName());
    private final HeatVisualizerHelper helper = new HeatVisualizerHelper();
    private static final Map<BigDecimal, Map<Integer, BigDecimal>> SCALED_MULTIPLIER_MAPPINGS = initScaledMultiplierMappings();
    private static final Map<Integer, Map<BigDecimal, Integer>> FINAL_POINTS_MAPPINGS = initFinalPointsMappings();
    private static final int IGNORED_HEAT_DIFFERENCE = 2; //eg.: '2' means that when heat difference <2, scale factor will be ignored

    private static Map<Integer, Map<BigDecimal, Integer>> initFinalPointsMappings() {
        Map<Integer, Map<BigDecimal, Integer>> map = new HashMap<>();
        for (int i = IGNORED_HEAT_DIFFERENCE; i <= 0x10; i++) { //up to 16
            map.put(i, new HashMap<>());
            for (ScaleFactorEnum scaleFactor : ScaleFactorEnum.values()) {
                BigDecimal scaleFactorValue = scaleFactor.getScaleFactor();
                BigDecimal scaledMultiplier = SCALED_MULTIPLIER_MAPPINGS.get(scaleFactorValue).get(i);
                BigDecimal finalPoints = new BigDecimal(i).multiply(scaledMultiplier).setScale(0, RoundingMode.HALF_UP);

                map.get(i).put(scaleFactorValue, finalPoints.intValue());
            }
        }
        return map;
    }

    private static Map<Integer, BigDecimal> initBigDecimalMappings() {
        Map<Integer, BigDecimal> map = new HashMap<>();
        for (int i = 0; i <= 0x10; i++) {
            map.put(i, new BigDecimal(i));
        }
        return map;
    }

    private static Map<BigDecimal, Map<Integer, BigDecimal>> initScaledMultiplierMappings() {
        Map<BigDecimal, Map<Integer, BigDecimal>> map = new HashMap<>();
        for (ScaleFactorEnum scaleFactor : ScaleFactorEnum.values()) {
            BigDecimal mainKey = scaleFactor.getScaleFactor();
            map.put(mainKey, new HashMap<>());
            for (int i = IGNORED_HEAT_DIFFERENCE; i <= 0x10; i++) {
                map.get(mainKey).put(i, scaleFactor.getScaleFactor().pow(i - IGNORED_HEAT_DIFFERENCE).setScale(5, RoundingMode.HALF_UP));
            }
        }
        return map;
    }

    public PubComparisonResultSWrapper selectBest(PubComparisonResultSWrapper old, PubComparisonResultSWrapper current) {
       List<Integer> rankingsOld = old.resultStream()
               .map(PubComparisonResultS::getValueList)
               .flatMap(Collection::stream)
               .sorted(Comparator.reverseOrder())
               .collect(Collectors.toList());

        List<Integer> rankingsCurrent = current.resultStream()
                .map(PubComparisonResultS::getValueList)
                .flatMap(Collection::stream)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        boolean oldIsHighest = true;
        for (int i = 0; i < rankingsOld.size(); i++) {
            if (rankingsOld.get(i) > rankingsCurrent.get(i)) {
                break;
            }
            if (rankingsOld.get(i) < rankingsCurrent.get(i)) {
                oldIsHighest = false;
                break;
            }
            //continue if equal
        }

        return oldIsHighest ? old : current;
    }

    public Optional<PubComparisonResultS> comparePubKeyHashes(PubTypeEnum type, String referencePKH, String currentPKH, ScaleFactorEnum scaleFactor) {
        if (referencePKH == null || referencePKH.isEmpty()) {
            return Optional.empty();
        }

        if (currentPKH == null || currentPKH.isEmpty()) {
            return Optional.empty();
        }

        ScaleFactorEnum forScaleFactor = scaleFactor == null ? Config.DEFAULT_SCALE_FACTOR : scaleFactor;
        BigDecimal pointsMultiplier = forScaleFactor.getScaleFactor();
        int pointCountPositive = 0;
        int pointCountNegative = 0;

        String currentItem;
        String referenceItem;
        int difference;
        int overflow_reference = HeatVisualizerConstants.OVERFLOW_REFERENCE_1_HEX;

        for (int i = 0; i < referencePKH.length(); i++) {
            currentItem = String.valueOf(currentPKH.charAt(i));
            referenceItem = String.valueOf(referencePKH.charAt(i));
            difference = calculateDifferenceForHexChars(currentItem, referenceItem);

            int pointsPositive = calculateWithMultiplier(countPointsPositive(difference, overflow_reference), pointsMultiplier);
            int pointsNegative = calculateWithMultiplier(countPointsNegative(difference, overflow_reference), pointsMultiplier);

            pointCountPositive = pointCountPositive + pointsPositive;
            pointCountNegative = pointCountNegative + pointsNegative;
        }

        return Optional.of(PubComparisonResultS.builder()
                                   .positive(pointCountPositive)
                                   .negative(pointCountNegative)
                                   .forScaleFactor(forScaleFactor)
                                   .type(type)
                                   .build());
    }

    private int calculateDifferenceForHexChars(String currentCharacter, String lockedCharacter) {
        return Integer.parseInt(currentCharacter, 16) - Integer.parseInt(lockedCharacter, 16);
    }

    /**
     * Difference > 0 always means negative overflow is lower(better result) than positive
     * Difference < 0 always means positive overflow is lower(better result) than negative
     * For a single index: min points = 1, max points = 16
     * @param difference - (current num minus locked num)
     * @param overflow_reference
     * @return
     */
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

    public int cacheHelperS(int difference, int overflow_reference, BigDecimal pointsMultiplier, HeatOverflowTypeEnum heatType) {
        int points;

        switch (heatType) {
            case HEAT_POSITIVE:
                points = countPointsPositive(difference, overflow_reference);
                break;
            case HEAT_NEGATIVE:
                points = countPointsNegative(difference, overflow_reference);
                break;
            default:
                throw new IllegalArgumentException("Heat type not supported at #cacheHelperS [type= " + heatType + "]");
        }

        return calculateWithMultiplier(points, pointsMultiplier);
    }

    public int calculateWithMultiplier(int totalPoints, BigDecimal pointsMultiplier) {
        if (totalPoints < 1) {
            throw new IllegalArgumentException("Points cannot be less than 1");
        }
        if (totalPoints < IGNORED_HEAT_DIFFERENCE) {
            return totalPoints;
        }

        return FINAL_POINTS_MAPPINGS.get(totalPoints).get(pointsMultiplier);
    }

    public PubComparisonResultSWrapper getCurrentResult(String currentPrivKey, String referencePKHUncompressed, String referencePKHCompressed, ScaleFactorEnum scaleFactor) {
        PubComparisonResultS resultUncompressed = getCurrentResultForUncompressed(currentPrivKey, referencePKHUncompressed, scaleFactor).orElse(null);
        PubComparisonResultS resultCompressed = getCurrentResultForCompressed(currentPrivKey, referencePKHCompressed, scaleFactor).orElse(null);

        if (resultUncompressed == null || resultCompressed == null) {
            return HeatVisualizerConstants.EMPTY_RESULT_WRAPPER;
        }

        return PubComparisonResultSWrapper.builder()
                .resultForUncompressed(resultUncompressed)
                .resultForCompressed(resultCompressed)
                .build();
    }

    private Optional<PubComparisonResultS> getCurrentResultForUncompressed(String currentPrivKey, String lockedPKHUncompressed, ScaleFactorEnum scaleFactor) {
        Optional<PubComparisonResultS> result = comparePubKeyHashes(PubTypeEnum.UNCOMPRESSED, lockedPKHUncompressed, helper.getPubKeyHashUncompressed(currentPrivKey, true), scaleFactor);
        result.ifPresent(res -> res.setForPriv(currentPrivKey));
        return result;
    }

    private Optional<PubComparisonResultS> getCurrentResultForCompressed(String currentPrivKey, String lockedPKHCompressed, ScaleFactorEnum scaleFactor) {
        Optional<PubComparisonResultS> result = comparePubKeyHashes(PubTypeEnum.COMPRESSED, lockedPKHCompressed, helper.getPubKeyHashCompressed(currentPrivKey, true), scaleFactor);
        result.ifPresent(res -> res.setForPriv(currentPrivKey));
        return result;
    }
}
