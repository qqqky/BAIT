package com.bearlycattable.bait.bl.contexts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.advancedCommons.interfaces.AdvancedSearchHelper;
import com.bearlycattable.bait.advancedCommons.interfaces.PredictableEnd;
import com.bearlycattable.bait.bl.controllers.advancedTab.AdvancedSubTabSearchController;
import com.bearlycattable.bait.advancedCommons.contexts.P2PKHSingleResultData;
import com.bearlycattable.bait.commons.contexts.TaskDiagnosticsModel;
import com.bearlycattable.bait.advancedCommons.models.ThreadSpawnModel;
import com.bearlycattable.bait.commons.dataAccessors.SeedMutationConfigDataAccessor;
import com.bearlycattable.bait.commons.dataAccessors.ThreadComponentDataAccessor;
import com.bearlycattable.bait.commons.enums.RandomWordPrefixMutationTypeEnum;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;

import javafx.beans.value.ObservableStringValue;
import javafx.concurrent.Task;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TaskPreparationContext {

    private final Task<P2PKHSingleResultData[]> searchTask;
    private final ThreadComponentDataAccessor accessor;
    private final Map<String, Task<P2PKHSingleResultData[]>> taskMap;
    private final Map<String,P2PKHSingleResultData[]> taskResultsMap;
    private final String saveLocation;
    private final TaskDiagnosticsModel taskDiagnosticsModel;
    private final ObservableStringValue observableProgressLabelValue;
    private final boolean firstLoop;
    private final boolean verboseMode;

    //variables below are used for loops
    private final AdvancedSubTabSearchController advancedSubTabSearchController;
    private final ThreadSpawnModel threadSpawnModel;

    //Note: this method is very similar to ThreadSpawnModel#makeLabelListForUserNotification(),
    //      but that one builds a more complete info summary.
    @NonNull
    public List<String> buildDetailedRunInfoForUi() {
        int loopsRemaining = threadSpawnModel.getRemainingLoops();
        int notifyThreshold = threadSpawnModel.getPointThresholdForNotify();
        SearchModeEnum searchMode = threadSpawnModel.getAdvancedSearchHelper().getSearchMode();
        String parentThreadId = threadSpawnModel.getParentThreadId();
        String startingSeed = threadSpawnModel.getSeed();
        List<Integer> disabledWords = threadSpawnModel.getDisabledWords();

        List<String> currentLoopInfo = new ArrayList<>();

        currentLoopInfo.add("Search mode: " + searchMode.getLabel());
        currentLoopInfo.add(buildSeedInfo(searchMode, threadSpawnModel));
        currentLoopInfo.add("Disabled words: " + (disabledWords == null || disabledWords.isEmpty() ? "NONE" : disabledWords));
        if (notifyThreshold > 0) {
            currentLoopInfo.add("Notification will be played when points are equal to or more than: " + notifyThreshold);
        }
        if (!SearchModeEnum.isRandomRelatedMode(searchMode)) {
            currentLoopInfo.add("Starting seed (current loop): " + startingSeed);
            currentLoopInfo.add("Ending key (current loop): " + predictEndingKey(threadSpawnModel.getAdvancedSearchHelper(), disabledWords, startingSeed));
        }
        currentLoopInfo.add("Save location for this thread: " + saveLocation);
        currentLoopInfo.add("Loops remaining for this worker: " + loopsRemaining + (loopsRemaining == 0 ? " (this is the last loop)" : ""));
        if (startingSeed != null) {
            threadSpawnModel.getSeedMutationConfigsAsOptional().ifPresent(seedMutationConfig -> currentLoopInfo.add(buildSeedModificationInfo(new SeedMutationConfigDataAccessor(seedMutationConfig))));
        }
        currentLoopInfo.add("Parent thread id: " + parentThreadId);

        return currentLoopInfo;
    }

    @NonNull
    private String predictEndingKey(AdvancedSearchHelper advancedSearchHelper, List<Integer> disabledWords, @NonNull String seed) {
        if (advancedSearchHelper instanceof PredictableEnd) {
            return ((PredictableEnd) advancedSearchHelper).predictLastItem(seed, advancedSearchHelper.getIterations(), disabledWords);
        }

        return "UNKNOWN (cannot make predictions for current search mode)";
    }

    @NonNull
    private String buildSeedModificationInfo(SeedMutationConfigDataAccessor accessor) {
        StringBuilder sb = new StringBuilder("List of enabled optional configs (seed modifications after every loop): ");
        String newLine = System.lineSeparator();

        if (accessor.isEmpty()) {
            sb.append("NONE SELECTED");
        } else {
            accessor.getIncDecPair().ifPresent(incDecPair -> sb.append(incDecPair).append(newLine));
            accessor.getHRotationPair().ifPresent(hRotPair -> sb.append(hRotPair).append(newLine));
            accessor.getVRotationPair().ifPresent(vRotPair -> {
                sb.append(accessor.getVRotationType()).append(newLine);
                sb.append("The following indexes will be rotated vertically by ").append(vRotPair.getKey()).append(": ").append(vRotPair.getValue()).append(newLine);
            });
        }
        return sb.toString();
    }

    @NonNull
    private String buildSeedInfo(SearchModeEnum searchMode, ThreadSpawnModel threadSpawnModel) {
        StringBuilder sb = new StringBuilder("Starting seed was: ");
        String newLine = System.lineSeparator();

        if (!SearchModeEnum.isRandomRelatedMode(searchMode)) {
            sb.append(threadSpawnModel.getSeed());
            return sb.toString();
        }

        sb.append(" not important [this is a random-related mode]").append(newLine);

        String wordPrefix = threadSpawnModel.getPrefix();
        Optional<RandomWordPrefixMutationTypeEnum> mutationType = threadSpawnModel.getPrefixMutationType();
        Optional<String> mutationValue = threadSpawnModel.getPrefixMutationValue();

        if (SearchModeEnum.RANDOM_PREFIXED_WORD == searchMode || SearchModeEnum.RANDOM_SAME_WORD == searchMode) {
            sb.append("Prefix for every word: ").append(wordPrefix == null || wordPrefix.isEmpty() ? "NONE" : wordPrefix).append(newLine);

            if (!mutationValue.isPresent() || !mutationType.isPresent()) {
                sb.append("This prefix will not be mutated during the run.").append(newLine);
            } else {
                sb.append("This prefix will be ").append(mutationType.get() == RandomWordPrefixMutationTypeEnum.INCREMENT ? "INCREMENTED" : "DECREMENTED")
                        .append(" by ").append(mutationValue.get()).append(" (hex value) after every loop (will overflow/underflow as needed)").append(newLine);
            }
        }
        return sb.toString();
    }
}
