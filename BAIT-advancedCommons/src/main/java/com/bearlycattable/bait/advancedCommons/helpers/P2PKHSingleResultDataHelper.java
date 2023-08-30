package com.bearlycattable.bait.advancedCommons.helpers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.bearlycattable.bait.advancedCommons.contexts.P2PKHSingleResultData;
import com.bearlycattable.bait.commons.HeatVisualizerConstants;
import com.bearlycattable.bait.commons.enums.HeatOverflowTypeEnum;
import com.bearlycattable.bait.commons.enums.JsonResultScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.JsonResultTypeEnum;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;
import com.bearlycattable.bait.commons.helpers.HeatVisualizerHelper;
import com.bearlycattable.bait.commons.other.PubComparer;
import com.bearlycattable.bait.commons.wrappers.PubComparisonResultWrapper;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.util.Pair;

public class P2PKHSingleResultDataHelper {

    private static final int OVERFLOW_REFERENCE = HeatVisualizerConstants.OVERFLOW_REFERENCE_1_HEX;
    private static final Map<Integer, BigDecimal> similarityMappings = initializeSimilarityMappings();
    private static final HeatVisualizerHelper helper = new HeatVisualizerHelper();
    private static final PubComparer pubComparer = new PubComparer();
    private static final Logger LOG = Logger.getLogger(P2PKHSingleResultDataHelper.class.getName());

    //this is used for merge function
    private static Map<Integer, BigDecimal> initializeSimilarityMappings() {
        Map<Integer, BigDecimal> mappings = new HashMap<>();
        int max = 16 * 40;
        for (int i = 0; i <= max; i++) {
            mappings.put(i, HeatVisualizerConstants.SINGLE_POINT_VALUE.multiply(new BigDecimal(i)));
        }
        return mappings;
    }

    private static int calculateScaledAccuracy(int points, ScaleFactorEnum scaleFactor) {
        return similarityMappings.get(helper.recalculateIndexForSimilarityMappings(points, scaleFactor)).setScale(0, RoundingMode.HALF_UP).intValue();
    }

    /**
     * Caches possible heat points of the whole dataCollection for the specified scale factor.
     * WARNING: this method should not be used if dataCollection.length() is ~5000 or more.
     * Reason: GC is not happy and won't allow it to be completed if the data set is too large.
     * @param dataCollection - array of search templates to be cached
     * @param scaleFactor - scale factor, which these templates should be cached for
     */
    public static void initializeCaches(P2PKHSingleResultData[] dataCollection, JsonResultScaleFactorEnum scaleFactor) {
        PubComparer comparer = new PubComparer();

        System.out.println("Started initializing caches [total: " + dataCollection.length + "]...");
        String lineRemoval = Stream.generate(() -> "\b").limit(40).collect(Collectors.joining());

        //report progress roughly every ~1%
        BigDecimal one = new BigDecimal(dataCollection.length).setScale(2, RoundingMode.HALF_UP).divide(new BigDecimal(100), RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);
        int onePercent = one.intValue() != 0 ? one.intValue() : 1;
        int count = 0;

        for (P2PKHSingleResultData item: dataCollection) {
            count++;
            initializeCache(item, scaleFactor, comparer);
            if (count % onePercent == 0) {
                System.out.print(lineRemoval);
                System.out.print((new BigDecimal(count).setScale(2, RoundingMode.HALF_UP).divide(one, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP) + "% of items cached so far... "));
            }
        }

        System.out.print(lineRemoval);
        System.out.println("All caches for result templates have been initialized.");
    }

    private static void initializeCache(P2PKHSingleResultData data, JsonResultScaleFactorEnum scaleFactor, PubComparer comparer) {
        List<String> charactersAsStrings = data.getHash().chars().mapToObj(num ->  {
            if (!isValidHexNum(num)) {
                throw new IllegalStateException("Bad hash... should be 40 hex characters");
            }
            return convertCharToString((char)num, true);
        }).collect(Collectors.toList());

        if (charactersAsStrings.size() != 40) {
            throw new IllegalStateException("Incorrect hash. Must be 40 hex characters");
        }

        data.clearPointMaps(); //deep clear

        BigDecimal pointsMultiplier = JsonResultScaleFactorEnum.toScaleFactorEnum(scaleFactor).getScaleFactor();

        for (int i = 0; i < 40; i++) {
            data.getPointMapForPositive().put(i, makeSubMap(i, charactersAsStrings, pointsMultiplier, comparer, HeatOverflowTypeEnum.HEAT_POSITIVE));
            data.getPointMapForNegative().put(i, makeSubMap(i, charactersAsStrings, pointsMultiplier, comparer, HeatOverflowTypeEnum.HEAT_NEGATIVE));
        }

        data.setCachedGeneralPointsForScaleFactor(scaleFactor);
    }

    private static Map<String, Integer> makeSubMap(int index, List<String> charactersAsStrings, BigDecimal pointsMultiplier, PubComparer comparer, HeatOverflowTypeEnum heatType) {
        Map<String, Integer> map = new HashMap<>();

        String referenceCharacter;
        int difference;

        for (int i = 0; i < 16; i++) {
            referenceCharacter = charactersAsStrings.get(index);
            difference = calculateDifferenceForHexChars(HeatVisualizerConstants.HEX_ALPHABET[i], referenceCharacter);

            map.put(HeatVisualizerConstants.HEX_ALPHABET[i].toLowerCase(Locale.ROOT),
                    comparer.calculateWithMultiplier(HeatOverflowTypeEnum.HEAT_POSITIVE == heatType ? countPointsPositive(difference, OVERFLOW_REFERENCE) : countPointsNegative(difference, OVERFLOW_REFERENCE), pointsMultiplier));
        }

        return map;
    }

    private static int countPointsNegative(int difference, int overflow_reference) {
        if (difference > 0) {
            return difference;
        }

        return difference < 0 ? overflow_reference + difference : overflow_reference;
    }

    private static int countPointsPositive(int difference, int overflow_reference) {
        if (difference > 0) {
            return overflow_reference - difference;
        }

        return difference < 0 ? -difference : overflow_reference;
    }

    private static int calculateDifferenceForHexChars(String currentCharacter, String lockedCharacter) {
        return Integer.parseInt(currentCharacter, 16) - Integer.parseInt(lockedCharacter, 16);
    }

    private static boolean isValidHexNum(int num) {
        return (num > 47 && num < 58) || (num > 64 && num < 71) || (num > 96 && num < 103);
    }

    private static String convertCharToString(char character, boolean uppercase) {
        return uppercase ? Character.toString(character).toUpperCase() : Character.toString(character).toLowerCase();
    }

    /**
     * Revalidates and caches the existing points in the search template (data array)
     * @param dataArray
     * @param scaleFactor
     */
    public static synchronized void revalidateAndInitCacheForExistingPoints(P2PKHSingleResultData[] dataArray, JsonResultScaleFactorEnum scaleFactor) {
        PubComparer comparer = new PubComparer();
        System.out.println("Started caching the existing points for scaleFactor: " + scaleFactor);

        for (P2PKHSingleResultData item : dataArray) {
            String hash = item.getHash();

            for (JsonResultTypeEnum type: JsonResultTypeEnum.values()) {
                String currentBestKey = item.getPair(type, scaleFactor).getKey();
                if (currentBestKey.isEmpty()) {
                    item.getExistingPoints().put(type, 0);
                } else {
                    PubComparisonResultWrapper recalculatedResult = comparer.getCurrentResult(currentBestKey, hash, hash, JsonResultScaleFactorEnum.toScaleFactorEnum(scaleFactor));
                    item.getExistingPoints().put(type, recalculatedResult.getResultByType(type));
                }
            }
            item.setCachedExistingPointsForScaleFactor(scaleFactor);
        }

        System.out.println("Ended caching the existing points for scale factor: " + scaleFactor);
    }

    /**
     * Makes a deep copy of user's search result template (used before starting a new search)
     * @param searchData
     * @return
     */
    public static P2PKHSingleResultData[] deepCopy(P2PKHSingleResultData[] searchData) {
        if (Objects.requireNonNull(searchData).length == 0) {
            return new P2PKHSingleResultData[0];
        }

        List<P2PKHSingleResultData> list = new ArrayList<>();

        for (P2PKHSingleResultData item : searchData) {
            list.add(P2PKHSingleResultData.newInstance(item));
        }

        return list.toArray(new P2PKHSingleResultData[0]);
    }

    /**
     * General method for saving a search template once the search is done (.json format)
     * @param saveLocation
     * @param data
     * @return
     */
    public static synchronized boolean serializeAndSave(String saveLocation, P2PKHSingleResultData[] data) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.getFactory().disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET); //if this was enabled - program would quit after calling ".writeValue()"

        Path path = Paths.get(saveLocation);
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE, StandardOpenOption.SYNC)) {
            mapper.writeValue(writer, data);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Creates a map with current lowest result points for every item(target key) in the template array.
     * Having such map becomes exponentially more efficient as we expect any new finds will become less and
     * less common as search goes on.
     * @param dataArray
     * @param scaleFactor
     * @return Map<targetKey, minPointsAcrossAllResultTypes>
     */
    public static synchronized Map<String, Integer> createCurrentMinPointsMap(P2PKHSingleResultData[] dataArray, JsonResultScaleFactorEnum scaleFactor) {
        PubComparer comparer = new PubComparer(); //make our own pubComparer
        Map<String, Integer> result = new HashMap<>();
        Arrays.stream(dataArray).forEach(item -> {
            result.put(item.getHash(), findCurrentMinPoints(Collections.singletonList(item).toArray(new P2PKHSingleResultData[0]), scaleFactor, comparer));
        });
        return result;
    }

    private static synchronized int findCurrentMinPoints(P2PKHSingleResultData[] dataArray, JsonResultScaleFactorEnum scaleFactor, PubComparer pubComparer) {
        int min = Integer.MAX_VALUE;
        ScaleFactorEnum sf = JsonResultScaleFactorEnum.toScaleFactorEnum(scaleFactor);

        for (P2PKHSingleResultData item : dataArray) {
            if (item.isGeneralPointsCachedForScaleFactor(scaleFactor)) {
                for (JsonResultTypeEnum resultType : JsonResultTypeEnum.values()) {
                    min = Math.min(min, item.getExistingPoints().get(resultType));
                }
            } else {
                PubComparisonResultWrapper oldResultFromData;
                String unknownP2PKH = item.getHash();
                for (JsonResultTypeEnum resultType : JsonResultTypeEnum.values()) {
                    String currentPriv = item.getPair(resultType, scaleFactor).getKey();
                    if (!currentPriv.isEmpty()) {
                        oldResultFromData = pubComparer.getCurrentResult(currentPriv, unknownP2PKH, unknownP2PKH, sf);
                        min = Math.min(min, oldResultFromData.getResultByType(resultType));
                    }
                }
            }
        }

        return min;
    }

    /**
     * This method merges 2 valid data arrays. If collisions are found, the points are recalculated.
     * That means for non-colliding elements a blind merge will be performed.
     * @param one - data array which will be taken as base to merge @param 'two' into
     * @param two - data array which will be merged into the @param 'one'
     * @return
     */
    @Nullable
    public static P2PKHSingleResultData[] merge(P2PKHSingleResultData[] one, P2PKHSingleResultData[] two) {
        if (one == null || one.length == 0) {
            return two != null && two.length != 0 ? two : null;
        }

        if (two == null || two.length == 0) {
            return one;
        }

        Map<String, Map<JsonResultTypeEnum, Map<JsonResultScaleFactorEnum, Pair<String, Integer>>>> dataMapOne = toDataMap(one);
        Map<String, Map<JsonResultTypeEnum, Map<JsonResultScaleFactorEnum, Pair<String, Integer>>>> dataMapTwo = toDataMap(two);

        //merge
        dataMapTwo.keySet().stream().forEach(key -> {
            dataMapOne.merge(key, dataMapTwo.get(key), (v1,v2) -> resolveValueCollision(key, v1, v2));
        });

        return toArray(dataMapOne);
    }

    private static Map<JsonResultTypeEnum, Map<JsonResultScaleFactorEnum, Pair<String, Integer>>> resolveValueCollision(String hash, Map<JsonResultTypeEnum, Map<JsonResultScaleFactorEnum, Pair<String, Integer>>> v1, Map<JsonResultTypeEnum, Map<JsonResultScaleFactorEnum, Pair<String, Integer>>> v2) {
        Map<JsonResultTypeEnum, Map<JsonResultScaleFactorEnum, Pair<String, Integer>>> finalResult = new LinkedHashMap<>();

        Arrays.stream(JsonResultTypeEnum.values()).forEach(resultType -> {
            finalResult.put(resultType, new LinkedHashMap<>());

            Arrays.stream(JsonResultScaleFactorEnum.values()).forEach(scaleFactor -> {
                Pair<String, Integer> pairOne = v1.get(resultType).get(scaleFactor);
                Pair<String, Integer> pairTwo = v2.get(resultType).get(scaleFactor);

                String keyOne = pairOne.getKey();
                String keyTwo = pairOne.getKey();

                //if one or both empty
                if (keyOne.isEmpty() && (!keyTwo.isEmpty())) { //0,1
                    finalResult.get(resultType).put(scaleFactor, pairTwo);
                    return;
                } else if (!keyOne.isEmpty() && keyTwo.isEmpty()) { //1,0
                    finalResult.get(resultType).put(scaleFactor, pairOne);
                    return;
                } else if (keyOne.isEmpty()) { //0,0 - both empty
                    finalResult.get(resultType).put(scaleFactor, new Pair<>(HeatVisualizerConstants.EMPTY_STRING, 0));
                    return;
                }

                //1,1 - both not empty (we recalculate using non-cached version)
                String unknownP2PKH = hash;
                PubComparisonResultWrapper resultOne = pubComparer.getCurrentResult(pairOne.getKey(), unknownP2PKH, unknownP2PKH, JsonResultScaleFactorEnum.toScaleFactorEnum(scaleFactor));
                PubComparisonResultWrapper resultTwo = pubComparer.getCurrentResult(pairTwo.getKey(), unknownP2PKH, unknownP2PKH, JsonResultScaleFactorEnum.toScaleFactorEnum(scaleFactor));

                int pointsOne = resultOne.getResultByType(resultType);
                int pointsTwo = resultTwo.getResultByType(resultType);

                if (pointsOne >= pointsTwo) {
                    finalResult.get(resultType).put(scaleFactor, new Pair<>(pairOne.getKey(), calculateScaledAccuracy(pointsOne, JsonResultScaleFactorEnum.toScaleFactorEnum(scaleFactor))));
                } else {
                    finalResult.get(resultType).put(scaleFactor, new Pair<>(pairTwo.getKey(), calculateScaledAccuracy(pointsTwo, JsonResultScaleFactorEnum.toScaleFactorEnum(scaleFactor))));
                }
            });
        });

        return finalResult;
    }

    public static P2PKHSingleResultData[] toArray(Map<String, Map<JsonResultTypeEnum, Map<JsonResultScaleFactorEnum, Pair<String, Integer>>>> searchDataMap) {
        Objects.requireNonNull(searchDataMap);

        return searchDataMap.keySet().stream()
                .map(key -> new P2PKHSingleResultData(key, searchDataMap.get(key)))
                .toArray(P2PKHSingleResultData[]::new);
    }

    public static Map<String, Map<JsonResultTypeEnum, Map<JsonResultScaleFactorEnum, Pair<String, Integer>>>> toDataMap(P2PKHSingleResultData[] dataArray) {
        Objects.requireNonNull(dataArray);

        return Arrays.stream(dataArray)
                .collect(Collectors.toMap(P2PKHSingleResultData::getHash, P2PKHSingleResultData::getResults, (v1,v2) -> v1, LinkedHashMap::new));
    }
}
