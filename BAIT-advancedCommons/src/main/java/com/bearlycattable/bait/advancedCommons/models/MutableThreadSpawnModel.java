package com.bearlycattable.bait.advancedCommons.models;

import java.util.List;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.bearlycattable.bait.advancedCommons.contexts.P2PKHSingleResultData;
import com.bearlycattable.bait.advancedCommons.interfaces.AdvancedSearchHelper;
import com.bearlycattable.bait.commons.enums.RandomWordPrefixMutationTypeEnum;
import com.bearlycattable.bait.commons.enums.SeedMutationTypeEnum;

import javafx.util.Pair;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MutableThreadSpawnModel {

    private AdvancedSearchHelper advancedSearchHelper;
    private boolean byteComparisonEnabled;
    private P2PKHSingleResultData[] deepDataCopy;
    private String saveLocation;
    private String seed;
    private List<Integer> disabledWords;
    private int totalLoopsRequested;
    private int remainingLoops;
    private int logSpacing;
    private int pointThresholdForNotify; //min points needed to play notification sound
    @Nullable
    private String parentThreadId;
    private Map<SeedMutationTypeEnum, Object> seedMutationConfigs;
    //only for random-related modes
    private String prefix;
    private Pair<RandomWordPrefixMutationTypeEnum, String> prefixMutationConfig;
}
