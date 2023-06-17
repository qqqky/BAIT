package com.bearlycattable.bait.advancedCommons.contexts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.bearlycattable.bait.commons.HeatVisualizerConstants;
import com.bearlycattable.bait.commons.enums.JsonResultScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.JsonResultTypeEnum;
import com.bearlycattable.bait.advancedCommons.helpers.P2PKHSingleResultDataHelper;
import com.bearlycattable.bait.commons.other.PubComparer;
import com.bearlycattable.bait.commons.wrappers.PubComparisonResultWrapper;
import com.bearlycattable.bait.advancedCommons.serialization.P2PKHSingleResultDataDeserializerCustom;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import javafx.util.Pair;
import lombok.Getter;

@JsonInclude
@JsonDeserialize(using = P2PKHSingleResultDataDeserializerCustom.class)
public class P2PKHSingleResultData {

    @JsonIgnore
    @Getter
    private transient Map<Integer, Map<String, Integer>> pointMapForPositive = new HashMap<>(); //index, value, result(int)
    @JsonIgnore
    @Getter
    private transient Map<Integer, Map<String, Integer>> pointMapForNegative = new HashMap<>(); //index, value, result(int)
    @JsonIgnore
    @Getter
    private transient Map<JsonResultTypeEnum, Integer> existingPoints = new HashMap<>();
    @JsonIgnore
    private static final PubComparer pubComparer = new PubComparer();
    @JsonIgnore
    private transient JsonResultScaleFactorEnum cachedGeneralPointsForScaleFactor;
    @JsonIgnore
    private transient JsonResultScaleFactorEnum cachedExistingPointsForScaleFactor;

    private String hash; //unencoded public key
    private Map<JsonResultTypeEnum, Map<JsonResultScaleFactorEnum, Pair<String, Integer>>> results; //4 types of results

    public P2PKHSingleResultData(String hash, Map<JsonResultTypeEnum, Map<JsonResultScaleFactorEnum, Pair<String, Integer>>> results) {
        this.hash = Objects.requireNonNull(hash);
        this.results = Objects.requireNonNull(results);	//use LinkedHashMap, because Jackson traverses with Iterator...
    }

    public static P2PKHSingleResultData newInstance(P2PKHSingleResultData data) {
        Map<JsonResultTypeEnum, Map<JsonResultScaleFactorEnum, Pair<String, Integer>>> newMap = new LinkedHashMap<>();
        Map<JsonResultTypeEnum, Map<JsonResultScaleFactorEnum, Pair<String, Integer>>> oldMap = data.getResults();
        oldMap.keySet().stream().forEach(key -> {
            newMap.put(key, new LinkedHashMap<>());
            oldMap.get(key).keySet().stream().forEach(subKey -> {
                Pair<String, Integer> oldPair = oldMap.get(key).get(subKey);
                Pair<String, Integer> newPair = new Pair<>(oldPair.getKey(), oldPair.getValue());
                newMap.get(key).put(subKey, newPair);
            });
        });
        return new P2PKHSingleResultData(data.getHash(), newMap);
    }

    public static P2PKHSingleResultData createEmptyBackbone(String hash) {
        Map<JsonResultTypeEnum, Map<JsonResultScaleFactorEnum, Pair<String, Integer>>> dataMap = new LinkedHashMap<>();
        Arrays.stream(JsonResultTypeEnum.values()).sequential().forEach(resultType -> {
            Map<JsonResultScaleFactorEnum, Pair<String, Integer>> internalMap = new LinkedHashMap<>();
            Arrays.stream(JsonResultScaleFactorEnum.values()).sequential().forEach(scaleFactor -> {
                internalMap.put(scaleFactor, new Pair<>(HeatVisualizerConstants.EMPTY_STRING, 0));
            });
            dataMap.put(resultType, internalMap);
        });

        return new P2PKHSingleResultData(hash, dataMap);
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

    @JsonIgnore
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
                    finalResult.get(resultType).put(scaleFactor, new Pair<>(pairOne.getKey(), P2PKHSingleResultDataHelper.calculateScaledAccuracy(pointsOne, JsonResultScaleFactorEnum.toScaleFactorEnum(scaleFactor))));
                } else {
                    finalResult.get(resultType).put(scaleFactor, new Pair<>(pairTwo.getKey(), P2PKHSingleResultDataHelper.calculateScaledAccuracy(pointsTwo, JsonResultScaleFactorEnum.toScaleFactorEnum(scaleFactor))));
                }
            });
        });

        return finalResult;
    }

    @JsonIgnore
    public int getCachedHeatComparisonPointsForPositive(String hexCharToCompare, int index) {
        return pointMapForPositive.get(index).get(hexCharToCompare);
    }

    @JsonIgnore
    public int getCachedHeatComparisonPointsForNegative(String hexCharToCompare, int index) {
        return pointMapForNegative.get(index).get(hexCharToCompare);
    }

    @JsonIgnore
    public void clearPointMaps() {
        List<Integer> keysForPositive = new ArrayList<>(pointMapForPositive.keySet());
        List<Integer> keysForNegative = new ArrayList<>(pointMapForNegative.keySet());

        keysForPositive.forEach(key -> pointMapForPositive.get(key).clear());
        keysForNegative.forEach(key -> pointMapForNegative.get(key).clear());

        pointMapForPositive.clear();
        pointMapForNegative.clear();
    }

    public String getHash() {
        return hash;
    }

    public Map<JsonResultTypeEnum, Map<JsonResultScaleFactorEnum, Pair<String, Integer>>> getResults() {
        return results;
    }

    @JsonIgnore
    public void setPair(Pair<String, Integer> resultPair, JsonResultTypeEnum resultType, JsonResultScaleFactorEnum scaleFactor) {
        Pair<String, Integer> copy = new Pair<>(resultPair.getKey(), resultPair.getValue());
        results.get(resultType).put(scaleFactor, copy);
    }

    @JsonIgnore
    public Pair<String, Integer> getPair(JsonResultTypeEnum resultType, JsonResultScaleFactorEnum scaleFactor) {
        return results.get(resultType).get(scaleFactor);
    }

    @JsonIgnore
    public void setCachedGeneralPointsForScaleFactor(JsonResultScaleFactorEnum scaleFactor) {
        this.cachedGeneralPointsForScaleFactor = scaleFactor;
    }

    @JsonIgnore
    public void setCachedExistingPointsForScaleFactor(JsonResultScaleFactorEnum scaleFactor) {
        this.cachedExistingPointsForScaleFactor = scaleFactor;
    }

    /**
     * Checks if P2PKH points (points of this entity's hash field) have been cached for the specified scale factor
     * (for faster heat comparison)
     * @param scaleFactor
     * @return
     */
    @JsonIgnore
    public boolean isGeneralPointsCachedForScaleFactor(JsonResultScaleFactorEnum scaleFactor) {
        return cachedGeneralPointsForScaleFactor == scaleFactor;
    }

    @JsonIgnore
    public JsonResultScaleFactorEnum getCachedForScaleFactor() {
        return cachedGeneralPointsForScaleFactor;
    }

    /**
     * Checks if existing data (points) of this entity has been cached for the specified scale factor (used to avoid
     * recalculating the points every time we have to make a check).
     *
     * NOTE: existing points should be cached before any lengthy search
     * @param scaleFactor
     * @return
     */
    @JsonIgnore
    public boolean isExistingPointsCachedForScaleFactor(JsonResultScaleFactorEnum scaleFactor) {
        return cachedExistingPointsForScaleFactor == scaleFactor;
    }

    @JsonIgnore
    public static synchronized String toStringPretty(P2PKHSingleResultData[] array) {
        if (array == null || array.length == 0) {
           return null;
        }

        StringBuilder sb = new StringBuilder();
        int length = array.length;
        String newLine = System.lineSeparator();

        sb.append("[").append(newLine);
        if (array.length == 1) {
            sb.append(array[0].toStringPretty()).append(newLine).append("]");
            return sb.toString();
        }

        for (int i = 0; i < length; i++) {
            sb.append(array[i].toStringPretty());
            if (i < (length - 1)) {
                sb.append(",");
            }
            sb.append(newLine);
        }

        sb.append("]");
        return sb.toString();
    }

    @JsonIgnore
    private String toStringPretty() {

        StringBuilder sb = new StringBuilder();
        String newLine = System.lineSeparator();
        String tab = "\t";

        sb.append(tab).append("{ \"hash\" : \"").append(hash).append("\",").append(newLine)
                .append(tab).append("  \"results\" : {").append(newLine);

        int countTypes = 0;
        int countScaleFactors = 0;

        for (JsonResultTypeEnum type : JsonResultTypeEnum.values()) {
            countTypes++;
            sb.append(tab).append(tab).append("\"").append(type).append("\" : {").append(newLine);
            for (JsonResultScaleFactorEnum scaleFactor : JsonResultScaleFactorEnum.values()) {
                countScaleFactors++;
                        sb.append(tab).append(tab).append(tab).append("\"").append(scaleFactor).append("\" : {")
                                .append("\"key\" : \"").append(results.get(type).get(scaleFactor).getKey()).append("\", ")
                                .append("\"value\" : ").append(results.get(type).get(scaleFactor).getValue()).append("}");
                if (countScaleFactors != JsonResultTypeEnum.values().length) {
                    sb.append(",").append(newLine);
                } else {
                    sb.append(newLine);
                }
            }

            countScaleFactors = 0; //reset counter
            sb.append(tab).append(tab).append("}");

            if (countTypes != JsonResultTypeEnum.values().length) {
                sb.append(",");
            }

            sb.append(newLine);
        }

        sb.append(tab).append("}");

        return sb.toString();
    }
}
