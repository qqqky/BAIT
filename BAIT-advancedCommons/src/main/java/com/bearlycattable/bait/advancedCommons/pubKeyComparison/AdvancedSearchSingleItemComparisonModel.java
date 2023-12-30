package com.bearlycattable.bait.advancedCommons.pubKeyComparison;

import com.bearlycattable.bait.advancedCommons.contexts.P2PKHSingleResultData;
import com.bearlycattable.bait.advancedCommons.pubKeyComparison.AdvancedPubComparisonResultB;
import com.bearlycattable.bait.commons.enums.JsonResultScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.JsonResultTypeEnum;
import com.bearlycattable.bait.commons.enums.LogTextTypeEnum;
import com.bearlycattable.bait.commons.enums.AddressGenerationAndComparisonType;
import com.bearlycattable.bait.commons.functions.TriConsumer;
import com.bearlycattable.bait.commons.pubKeyComparison.PubComparisonResultSWrapper;

import javafx.scene.paint.Color;
import javafx.util.Pair;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
public class AdvancedSearchSingleItemComparisonModel {

    private final JsonResultScaleFactorEnum scaleFactor;
    private final int pointThresholdForNotify;
    private final TriConsumer<String, Color, LogTextTypeEnum> logConsumer;
    private final boolean verbose;
    private final AddressGenerationAndComparisonType addressGenerationAndComparisonType;
    @Setter
    private P2PKHSingleResultData resultContainer;
    @Setter
    private PubComparisonResultSWrapper newResult;
    @Setter
    private AdvancedPubComparisonResultB currentByteComparisonModel;
    @Setter
    private JsonResultTypeEnum type;
    @Setter
    private String currentPrivKey; //new result to compare to

    public boolean isPointMappingsCached() {
        return resultContainer != null && resultContainer.isGeneralPointsCachedForScaleFactor(scaleFactor, addressGenerationAndComparisonType);
    }

    public boolean isExistingPointsCached() {
        return resultContainer != null && resultContainer.isExistingPointsCachedForScaleFactor(scaleFactor);
    }

    public void updateExistingCachedPoints(int pointsNew) {
        resultContainer.getExistingPoints().put(type, pointsNew);
    }

    public int getExistingPoints() {
        return resultContainer.getExistingPoints().get(type);
    }

    public int getNewPoints() {
        return newResult != null ? newResult.getResultByType(type) : currentByteComparisonModel.getResultPointsByType(type);
    }

    public Pair<String, Integer> getCurrentBestPair() {
        return resultContainer.getPair(type, scaleFactor);
    }

    public void setNewBestPair(Pair<String, Integer> newBestPair) {
        resultContainer.setPair(newBestPair, type, scaleFactor);
    }
}
