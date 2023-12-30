package com.bearlycattable.bait.advancedCommons.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.bearlycattable.bait.advancedCommons.contexts.P2PKHSingleResultData;
import com.bearlycattable.bait.advancedCommons.dataAccessors.SeedMutationConfigDataAccessor;
import com.bearlycattable.bait.advancedCommons.interfaces.AdvancedSearchHelper;
import com.bearlycattable.bait.advancedCommons.interfaces.PredictableEnd;
import com.bearlycattable.bait.commons.Config;
import com.bearlycattable.bait.commons.enums.AddressGenerationAndComparisonType;
import com.bearlycattable.bait.commons.enums.RandomWordPrefixMutationTypeEnum;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.enums.SeedMutationTypeEnum;
import com.bearlycattable.bait.commons.functions.TriFunction;

import javafx.util.Pair;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ThreadSpawnModel {

    private final AdvancedSearchHelper advancedSearchHelper;
    private final boolean byteComparisonEnabled;
    private P2PKHSingleResultData[] deepDataCopy;
    private final String saveLocation;
    private String seed;
    private final List<Integer> disabledWords;
    private final int totalLoopsRequested;
    private int remainingLoops;
    private final int logSpacing;
    private final int pointThresholdForNotify; //min points needed to play notification sound
    @Nullable private String parentThreadId;
    private Map<SeedMutationTypeEnum, Object> seedMutationConfigs;
    //only for random-related modes
    private String prefix;
    private Pair<RandomWordPrefixMutationTypeEnum, String> prefixMutationConfig;

    public Optional<Map<SeedMutationTypeEnum, Object>> getSeedMutationConfigsAsOptional() {
        return Optional.ofNullable(seedMutationConfigs);
    }

    public Optional<RandomWordPrefixMutationTypeEnum> getPrefixMutationType() {
        return prefixMutationConfig == null ? Optional.empty() : Optional.ofNullable(prefixMutationConfig.getKey());
    }

    public Optional<String> getPrefixMutationValue() {
        return prefixMutationConfig == null ? Optional.empty() : Optional.ofNullable(prefixMutationConfig.getValue());
    }

    public SearchModeEnum getMode() {
        return advancedSearchHelper == null ? null : advancedSearchHelper.getSearchMode();
    }

    public boolean isRandomRelatedMode() {
        return SearchModeEnum.isRandomRelatedMode(getMode());
    }

    @NonNull
    public List<String> makeLabelListForUserNotification(TriFunction<String, List<Integer>, SeedMutationConfigDataAccessor, String> buildMutatedSeedFunction) {
        int numOfPKHs = deepDataCopy.length;
        List<String> labelList = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        boolean finiteMode = SearchModeEnum.isFiniteMode(advancedSearchHelper.getSearchMode());

        labelList.add("Search mode: " + advancedSearchHelper.getSearchMode());
        if (finiteMode) {
            labelList.add("NOTE: the selected search mode is FINITE, so number of iterations might have been adjusted (unless they were already below max value for this search mode)");
        }
        labelList.add("Address generation and comparison mode: " + (advancedSearchHelper.isByteComparisonSupported() && byteComparisonEnabled ? AddressGenerationAndComparisonType.BYTE : AddressGenerationAndComparisonType.STRING));
        if (isRandomRelatedMode() && prefix != null && !prefix.isEmpty()) {
            labelList.add("");
            labelList.add("Selected prefix for every word: " + prefix);
            if (prefixMutationConfig != null && !prefixMutationConfig.getValue().isEmpty()) {
                labelList.add("Prefix mutation type and value: " + prefixMutationConfig.toString());
            }
        }

        labelList.add("");
        labelList.add("Scale factor: " + ScaleFactorEnum.toJsonScaleFactorEnum(advancedSearchHelper.getScaleFactor()));
        labelList.add("Number of iterations (for each loop): " + advancedSearchHelper.getIterations() + (finiteMode ? " [possibly adjusted]" : ""));
        labelList.add("Number of loops: " + remainingLoops);
        if (!SearchModeEnum.isRandomRelatedMode(advancedSearchHelper.getSearchMode())) {
            labelList.add("Starting seed (current loop): " + seed);
            labelList.add("Ending key (current loop): " + predictEndingKey(disabledWords, seed));
        }
        labelList.add("Disabled words: " + (disabledWords.isEmpty() ? "NONE" : disabledWords));
        labelList.add("Number of addresses in the template: " + numOfPKHs);
        if (numOfPKHs > Config.MAX_CACHEABLE_ADDRESSES_IN_TEMPLATE) {
            warnings.add("WARNING! Result template contains too many items to cache. Search will be much slower. Please consider using a template with fewer items (up to 5000)");
        }
        labelList.add("Save location for results: " + saveLocation);

        labelList.add("");
        boolean exactMatchCheckEnabled = advancedSearchHelper.isExactMatchCheckEnabled();
        labelList.add("Exact match check is " + (exactMatchCheckEnabled ? "ENABLED" : "DISABLED"));
        if (exactMatchCheckEnabled) {
            labelList.add("Total num of entries in the 'exact check' map: " + advancedSearchHelper.exactMatchCheckDataLength());
        } else {
            warnings.add("WARNING! Exact match check is disabled!");
        }

        labelList.add("");
        if (remainingLoops > 1) {
            insertSeedModificationInfo(buildMutatedSeedFunction, labelList, warnings);
        }

        labelList.add("Log options: ");
        labelList.add("Current key will be logged every " + logSpacing + " iterations");

        if (pointThresholdForNotify < 1) {
            warnings.add("WARNING! Notification sounds are disabled!");
        } else {
            labelList.add("Notification sound will be played when comparison points are more or equal to: " + pointThresholdForNotify);
        }

        if (!warnings.isEmpty()) {
            labelList.add("");
            labelList.add("#SPACER#" + "You have " + warnings.size() + " warning(s): ");
            labelList.addAll(warnings);
        }

        return labelList;
    }

    @NonNull
    private String predictEndingKey(List<Integer> disabledWords, @NonNull String seed) {
        if (advancedSearchHelper instanceof PredictableEnd) {
            return ((PredictableEnd) advancedSearchHelper).predictLastItem(seed, advancedSearchHelper.getIterations(), disabledWords);
        }

        return "UNKNOWN (cannot make predictions for current search mode)";
    }

    private void insertSeedModificationInfo(TriFunction<String, List<Integer>, SeedMutationConfigDataAccessor, String> buildMutatedSeedFunction, List<String> labelList, List<String> warnings) {
        if (isRandomRelatedMode()) {
            return;
        }

        if (seedMutationConfigs == null) {
            warnings.add("WARNING! Seed will not be modified after every loop! Are you sure it is intentional?");
            return;
        }

        SeedMutationConfigDataAccessor accessor = new SeedMutationConfigDataAccessor(seedMutationConfigs);
        accessor.validate();

        if (accessor.isEmpty()) {
            warnings.add("WARNING! Seed will not be modified after every loop! Are you sure it is intentional?");
            return;
        }

        labelList.add("The following modifications to the seed will be applied after every loop: ");
        accessor.getIncDecPair().ifPresent(incDecPair -> labelList.add(incDecPair.toString()));
        accessor.getHRotationPair().ifPresent(hRotPair -> labelList.add(hRotPair.toString()));
        accessor.getVRotationPair().ifPresent(vRotPair -> {
            accessor.getVRotationType().ifPresent(type -> {
                final int numIndexesInLine = 15;
                addVRotationInfoForUser(vRotPair, labelList, numIndexesInLine);
            });
        });

        labelList.add("Some examples for seed mutation (only showing up to 8th at max):");
        String seedExample = seed;
        for (int i = 1; i < Math.min(8, remainingLoops); i++) {
            seedExample = buildMutatedSeedFunction.apply(seedExample, disabledWords, accessor);
            labelList.add("For loop " + (i + 1) + " seed will be: " + seedExample);
        }
        labelList.add("");

    }

    private void addVRotationInfoForUser(Pair<String, List<Integer>> vRotData, List<String> labelList, int numIndexesInLine) {
        List<Integer> indexList = vRotData.getValue();
        if (indexList == null) {
            return;
        }
        if (indexList.size() <= numIndexesInLine) {
            labelList.add("The following indexes will be rotated vertically by " + vRotData.getKey() + ": " + vRotData.getValue());
            return;
        }

        List<Integer> subList;
        labelList.add("The following indexes will be rotated vertically by " + vRotData.getKey() + ": ");
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < indexList.size() / numIndexesInLine; i++) {
            if (i == 0) {
                sb.append("[");
            }
            subList = indexList.subList(i * numIndexesInLine, (i * numIndexesInLine) + numIndexesInLine);
            sb.append(subList.toString().replaceAll("[\\[\\]]", ""));

            if ((i + 1) == indexList.size() / numIndexesInLine) {
                subList = indexList.subList((i + 1) * numIndexesInLine, indexList.size());
                if (subList.isEmpty()) {
                    sb.append("]");
                } else {
                    sb.append(",");
                    labelList.add(sb.toString());
                    sb.delete(0, sb.length());
                    sb.append(subList.toString().replaceAll("\\[", ""));
                }
            } else {
                sb.append(",");
            }
            labelList.add(sb.toString());
            sb.delete(0, sb.length());
        }
    }
}
