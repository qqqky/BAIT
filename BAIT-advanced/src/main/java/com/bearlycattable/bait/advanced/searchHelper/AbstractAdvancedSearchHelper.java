package com.bearlycattable.bait.advanced.searchHelper;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.advanced.searchHelper.comparisonModel.AdvancedSearchSingleItemComparisonModel;
import com.bearlycattable.bait.advanced.searchHelper.helpers.ExactMatchHelper;
import com.bearlycattable.bait.advancedCommons.ShortSoundEffects;
import com.bearlycattable.bait.advancedCommons.contexts.AdvancedSearchContext;
import com.bearlycattable.bait.advancedCommons.contexts.AdvancedSearchHelperCreationContext;
import com.bearlycattable.bait.advancedCommons.contexts.P2PKHSingleResultData;
import com.bearlycattable.bait.advancedCommons.helpers.P2PKHSingleResultDataHelper;
import com.bearlycattable.bait.advancedCommons.interfaces.AdvancedSearchHelper;
import com.bearlycattable.bait.advancedCommons.other.AdvancedPubComparer;
import com.bearlycattable.bait.advancedCommons.wrappers.AdvancedSearchTaskWrapper;
import com.bearlycattable.bait.commons.Config;
import com.bearlycattable.bait.commons.HeatVisualizerConstants;
import com.bearlycattable.bait.commons.contexts.TaskDiagnosticsModel;
import com.bearlycattable.bait.commons.enums.JsonResultScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.JsonResultTypeEnum;
import com.bearlycattable.bait.commons.enums.LogTextTypeEnum;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.functions.TriConsumer;
import com.bearlycattable.bait.commons.helpers.GeneralSearchHelper;
import com.bearlycattable.bait.commons.helpers.HeatVisualizerHelper;
import com.bearlycattable.bait.commons.validators.SearchHelperIterationsValidator;
import com.bearlycattable.bait.commons.wrappers.PubComparisonResultWrapper;
import com.bearlycattable.bait.utility.DurationUtils;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableStringValue;
import javafx.concurrent.Task;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import lombok.Getter;

public abstract class AbstractAdvancedSearchHelper extends GeneralSearchHelper implements AdvancedSearchHelper {

    private static final Logger LOG = Logger.getLogger(AbstractAdvancedSearchHelper.class.getName());

    @Getter
    private final HeatVisualizerHelper helper = new HeatVisualizerHelper();
    private final AdvancedPubComparer advancedPubComparer = new AdvancedPubComparer();
    private final Set<String> unknownPKHs = new HashSet<>();

    private final boolean exactMatchCheckEnabled;

    private AbstractAdvancedSearchHelper() {
        super();
        throw new UnsupportedOperationException("Creation of AbstractAdvancedSearchHelper without context is not allowed");
    }

    protected AbstractAdvancedSearchHelper(@NonNull AdvancedSearchHelperCreationContext creationContext) {
        super(creationContext);
        this.exactMatchCheckEnabled = creationContext.isExactMatchCheckEnabled();
    }

    protected final AdvancedSearchTaskWrapper advancedSearchTaskGuiCreationHelper(AdvancedSearchContext advancedSearchContext) {
        advancedSearchContext.setIterations(SearchHelperIterationsValidator.validateAndGet(advancedSearchContext.getSearchMode(), advancedSearchContext.getIterations()));

        Optional<String> validationError = advancedSearchContext.validate(true);
        if (validationError.isPresent()) {
            return AdvancedSearchTaskWrapper.builder()
                    .task(null)
                    .error("Some advanced search parameters appear to be invalid. Reason: " + validationError.get())
                    .build();
        }

        Task<P2PKHSingleResultData[]> task;

        SearchModeEnum mode = advancedSearchContext.getSearchMode();
        if (SearchModeEnum.MIXED == mode || SearchModeEnum.FUZZING == mode) {
            return AdvancedSearchTaskWrapper.builder()
                    .task(null)
                    .error("Mode not supported for AdvancedSearch at AbstractAdvancedSearchHelper [mode=" + mode + "]")
                    .build();
        } else if (SearchModeEnum.isVerticalRotationMode(mode)) {
            task = createNewVerticalRotationAdvancedSearchTask(advancedSearchContext);
        } else if (SearchModeEnum.isHorizontalRotationMode(mode)) {
            task = createNewHorizontalRotationAdvancedSearchTask(advancedSearchContext);
        } else {
            //this is for incDec modes and random modes
            task = createNewGeneralAdvancedSearchTask(advancedSearchContext);
        }

        return AdvancedSearchTaskWrapper.builder()
                .task(task)
                .error(null)
                .build();
    }

    private Task<P2PKHSingleResultData[]> createNewVerticalRotationAdvancedSearchTask(AdvancedSearchContext advancedSearchContext) {
        throw new IllegalStateException("Vertical rotation modes are not supported at AbstractAdvancedSearchHelper#createNewVerticalRotationAdvancedSearchTask");
    }

    private Task<P2PKHSingleResultData[]> createNewHorizontalRotationAdvancedSearchTask(AdvancedSearchContext advancedSearchContext) {
        throw new IllegalStateException("Horizontal rotation modes are not supported at AbstractAdvancedSearchHelper#createNewHorizontalRotationAdvancedSearchTask");
    }

    private Task<P2PKHSingleResultData[]> createNewGeneralAdvancedSearchTask(AdvancedSearchContext context) {

        return new Task<P2PKHSingleResultData[]>() {
            private static final int FORTY = 40;
            private final TaskDiagnosticsModel diagnostics = context.getTaskDiagnosticsModel();

            private final String seed = context.getSeed();
            private final String parentThreadId = context.getParentThreadId();
            private final String childThreadId = diagnostics.getChildThreadId();

            private final P2PKHSingleResultData[] dataArray = context.getDataArray();
            private final ObservableStringValue observableProgressLabelValue = context.getObservableProgressLabelValue();
            private final int printSpacing = context.getPrintSpacing();
            private final int updateSpacing = context.getProgressSpacing();
            private final int notifyThreshold = context.getPointThresholdForNotify();
            private final TriConsumer<String, Color, LogTextTypeEnum> logConsumer = Objects.requireNonNull(context.getLogConsumer());
            private final SearchModeEnum searchMode = Objects.requireNonNull(context.getSearchMode());
            private final int iterations = SearchHelperIterationsValidator.validateAndGet(searchMode, getIterations());
            private final Function<String, String> buildNextPrivFunction = Objects.requireNonNull(context.getNextPrivFunction());
            private final boolean exactMatchCheckOnly = false; //TODO: cbx 'exact match check only'
            private final boolean exactMatchCheckEnabled = AbstractAdvancedSearchHelper.this.exactMatchCheckEnabled;
            private final boolean verbose = context.isVerbose();
            private final Map<String, String> LOWERCASE_HEX_ALPHABET = HeatVisualizerHelper.newLowercaseHexMap();

            @Override
            public P2PKHSingleResultData[] call() {
                String currentPriv = seed;

                final JsonResultScaleFactorEnum currentScaleFactor = ScaleFactorEnum.toJsonScaleFactorEnum(getScaleFactor());
                final boolean allPointMappingsCached = Arrays.stream(dataArray).allMatch(item -> item.isGeneralPointsCachedForScaleFactor(currentScaleFactor));

                //cache existing points to avoid having to recalculate (stored in dataArray)
                P2PKHSingleResultDataHelper.revalidateAndInitCacheForExistingPoints(dataArray, ScaleFactorEnum.toJsonScaleFactorEnum(getScaleFactor()));

                //save current min points for every item in the template
                Map<String, Integer> currentMinPointsMap = P2PKHSingleResultDataHelper.createCurrentMinPointsMap(dataArray, ScaleFactorEnum.toJsonScaleFactorEnum(getScaleFactor())); //for every P2PKHSingleResultData item
                String UPKH;
                String CPKH;
                String[] UPKHArray = new String[40];
                String[] CPKHArray = new String[40];

                final AdvancedSearchSingleItemComparisonModel dataModel = AdvancedSearchSingleItemComparisonModel.builder()
                        .logConsumer(logConsumer)
                        .verbose(verbose)
                        .scaleFactor(currentScaleFactor)
                        .pointThresholdForNotify(notifyThreshold)
                        .build();

                int count = 0; //counting num of better results in the ongoing run
                long start = System.nanoTime();

                for (int i = 0; i < iterations; i++) {
                    //if thread is cancelled at any point, we might want to know at which iteration it was stopped
                    updateMessage((i + 1) + ":" + currentPriv);

                    if (isCancelled()) { //task might be cancelled at any point while doing work
                        updateProgress((i + 1), iterations);
                        updateProgressLabel((i + 1), observableProgressLabelValue);
                        return dataArray;
                    }

                    currentPriv = buildNextPrivFunction.apply(currentPriv); //disabled words are taken into account

                    if ((i + 1) % printSpacing == 0) {
                        printCurrentKey((i + 1), searchMode, currentPriv, logConsumer);
                    }

                    if ((i + 1) % updateSpacing == 0) {
                        updateProgress(i, iterations, observableProgressLabelValue);
                    }

                    //do not check the same key multiple times
                    if (!isValidKey(currentPriv)) {
                        continue;
                    }

                    //use this so we dont need to recalculate hashes for the same keys multiple times
                    UPKH = helper.getPubKeyHashUncompressed(currentPriv, true);
                    CPKH = helper.getPubKeyHashCompressed(currentPriv, true);

                    if (exactMatchCheckEnabled && isPresentInAdditionalMap(UPKH, CPKH)) {
                        processExactMatchResult(i, iterations, currentPriv, UPKH, CPKH, logConsumer);
                        return dataArray;
                    }

                    if (exactMatchCheckOnly) {
                        continue;
                    }

                    //update arrays with current values of UPKH and CPKH
                    updatePKHArray(UPKHArray, UPKH);
                    updatePKHArray(CPKHArray, CPKH);

                    PubComparisonResultWrapper newResult;
                    String unknownP2PKH;

                    dataModel.setCurrentPrivKey(currentPriv);

                    for (P2PKHSingleResultData item : dataArray) {
                        dataModel.setResultContainer(item);

                        unknownP2PKH = item.getHash();
                        newResult = allPointMappingsCached ? calculateCurrentResultCached(currentPriv, UPKHArray, CPKHArray, item) : calculateCurrentResult(currentPriv, unknownP2PKH, unknownP2PKH, getScaleFactor());

                        int oldMinPoints;
                        int pointsGained;
                        int pointsNew;

                        for (JsonResultTypeEnum type : JsonResultTypeEnum.values()) {
                            pointsNew = newResult.getResultByType(type);

                            //small optimization for long-running data sets (execution will rarely proceed past this point)
                            if (pointsNew <= currentMinPointsMap.get(unknownP2PKH)) {
                                continue;
                            }

                            dataModel.setNewResult(newResult);
                            dataModel.setType(type);

                            pointsGained = compareAndUpdatePairResults(dataModel);

                            if (pointsGained == -1) {
                                continue;
                            }

                            count++;

                            if (diagnostics.getHighestAcquiredPoints() < pointsGained) {
                                diagnostics.setHighestAcquiredPoints(pointsGained);
                            }

                            //update currentMinPointsMap (slow, but on old templates it will happen MUCH too rarely to care about this
                            oldMinPoints = currentMinPointsMap.get(unknownP2PKH);
                            currentMinPointsMap = P2PKHSingleResultDataHelper.createCurrentMinPointsMap(dataArray, ScaleFactorEnum.toJsonScaleFactorEnum(getScaleFactor()));

                            if (verbose && oldMinPoints != currentMinPointsMap.get(unknownP2PKH)) {
                                String minStartingPointsUpdate = "Min points updated to: " + currentMinPointsMap.get(unknownP2PKH) + " [targetPKH=" + unknownP2PKH + ", updatedFrom=" + oldMinPoints + ", updatedTo=" + currentMinPointsMap.get(unknownP2PKH) + "]";
                                LOG.info(minStartingPointsUpdate);
                                Platform.runLater(() -> logConsumer.accept(minStartingPointsUpdate, Color.GREEN, LogTextTypeEnum.POINTS_GAINED));
                            }
                        }
                    }
                }

                //end of run
                long end = System.nanoTime();
                long seconds = (end - start) / 1000000000;

                updateProgress(iterations, iterations, observableProgressLabelValue);

                String messageForDiagnostics = buildEndOfSearchMessage(seconds, count, seed, currentPriv);

                diagnostics.setLoopCompletionMessage(messageForDiagnostics);
                diagnostics.setTotalNumOfResultsFound(count);

                LOG.info(messageForDiagnostics);
                Platform.runLater(() -> logConsumer.accept(messageForDiagnostics, Color.WHEAT, LogTextTypeEnum.END_OF_SEARCH));

                return dataArray;
            }

            private void processExactMatchResult(int i, int iterations, String currentPriv, String UPKH, String CPKH, TriConsumer<String, Color, LogTextTypeEnum> logConsumer) {
                String exactMatchFoundMessage = "Match found for key: " + currentPriv + " (matched either its UPKH[" + UPKH + "] or CPKH[" + CPKH + "])";
                LOG.info(exactMatchFoundMessage);
                Platform.runLater(() -> logConsumer.accept(exactMatchFoundMessage, Color.DEEPPINK, LogTextTypeEnum.LOG_CLEAR)); //intentional type

                String targetPath = Config.EXACT_MATCH_SAVE_PATH;
                ExactMatchHelper.appendMatchToFile(currentPriv, unknownPKHs.contains(UPKH) ? UPKH : CPKH, targetPath);
                ShortSoundEffects.DOUBLE_BEEP.play();
                updateProgress((i + 1), iterations);
                updateProgressLabel((i + 1), observableProgressLabelValue);
            }

            private void printCurrentKey(int i, SearchModeEnum searchMode, String currentPriv, TriConsumer<String, Color, LogTextTypeEnum> logConsumer) {
                String progressMessage = "Progress: " + calculateProgressPercent(i) + "% [parentTID=" + parentThreadId + ", childTID=" + childThreadId + ", i=" + i + ", key=" + currentPriv + ", mode=" + searchMode.getAbbr() + "] (" + DurationUtils.getCurrentDateTime() + ")";
                System.out.println(progressMessage);
                Platform.runLater(() -> logConsumer.accept(progressMessage, Color.GREEN, LogTextTypeEnum.SEARCH_PROGRESS));
            }

            private void updateProgress(int i, int iterations, ObservableStringValue observableProgressLabelValue) {
                updateProgress(i, iterations);
                updateProgressLabel(i, observableProgressLabelValue);
            }

            private String buildEndOfSearchMessage(long seconds, int totalNewResults, String firstKey, String lastKey) {
                StringBuilder sb = new StringBuilder();
                sb.append("First priv (this one is not checked): ").append(firstKey)
                        .append(System.lineSeparator())
                        .append("Last priv (this one is checked): ").append(lastKey)
                        .append(System.lineSeparator())
                        .append("Total number of iterations processed: ").append(iterations)
                        .append(System.lineSeparator())
                        .append("Total duration in seconds: ").append(seconds)
                        .append(System.lineSeparator())
                        .append("Total duration in a more convenient format: ").append(DurationUtils.getDurationDHMS(seconds))
                        .append(System.lineSeparator())
                        .append("Total number of better results found during this loop: ").append(totalNewResults)
                        .append(System.lineSeparator())
                        .append("Highest points acquired on a single result (during this loop): ").append(diagnostics.getHighestAcquiredPoints());

                return sb.toString();
            }

            //this should be the most efficient way so far
            private void updatePKHArray(String[] array, String PKH) {
                for (int i = 0; i < FORTY; i++) {
                    array[i] = LOWERCASE_HEX_ALPHABET.get(String.valueOf(PKH.charAt(i)));
                }
            }

            private void updateProgressLabel(int i, ObservableStringValue observableProgressLabelValue) {
                Platform.runLater(() -> ((SimpleStringProperty) observableProgressLabelValue).set(calculateProgressPercent(i) + "%"));
            }

            private BigDecimal calculateProgressPercent(int i) {
                BigDecimal all = new BigDecimal(100).setScale(3, RoundingMode.HALF_UP).multiply(new BigDecimal(i));
                //set RoundingMode for divisor to avoid 'Non-terminating decimal expansion' exception
                return all.divide(new BigDecimal(iterations), RoundingMode.HALF_UP).setScale(1, RoundingMode.HALF_UP);
            }
        };
    }

    private int compareAndUpdatePairResults(AdvancedSearchSingleItemComparisonModel buildContext) {
        Pair<String, Integer> currentBest = buildContext.getCurrentBestPair();
        String oldPriv = currentBest.getKey();
        int oldAccuracy = currentBest.getValue();

        int pointsOld = findOldPoints(oldPriv, buildContext);
        int pointsNew = buildContext.getNewPoints();

        if (pointsNew <= pointsOld) {
            return -1; //no better result found, leave as is
        }

        int newAccuracy = getSimilarityMappings().get(helper.recalculateIndexForSimilarityMappings(pointsNew, getScaleFactor())).setScale(0, RoundingMode.HALF_UP).intValue();
        int pointsGained = (pointsNew - pointsOld);

        String betterResultFoundMessage = "Better result found: --- +" + pointsGained + " point(s) --- [total: " + pointsNew + "/" + getScaleFactor().getMaxPoints() + "; acc: " + oldAccuracy + " --> " + newAccuracy + "] (" + DurationUtils.getCurrentDateTime() + ")";

        //dev fun
        if (System.getProperty("os.name").contains("Windows")) {
            try {
                Runtime.getRuntime().exec("PowerShell -Command \"Add-Type -AssemblyName System.Speech; (New-Object System.Speech.Synthesis.SpeechSynthesizer).Speak('" + pointsGained + " points gained');\"");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println(betterResultFoundMessage);
        Platform.runLater(() -> buildContext.getLogConsumer().accept(betterResultFoundMessage, Color.GREEN, LogTextTypeEnum.POINTS_GAINED));

        if (buildContext.isVerbose()) {
            String keySwapMessage = "Swapped key to: " + buildContext.getCurrentPrivKey() + "[targetPHK=" + buildContext.getResultContainer().getHash() + ", type " + buildContext.getType() + ", oldKey=" + oldPriv + "]";
            LOG.info(keySwapMessage);
            Platform.runLater(() -> buildContext.getLogConsumer().accept(keySwapMessage, Color.GREEN, LogTextTypeEnum.KEY_SWAP));
        }

        if (buildContext.getPointThresholdForNotify() > 0 && pointsGained >= buildContext.getPointThresholdForNotify()) {
            ShortSoundEffects.SINGLE_BEEP.play();
        }

        //save new result
        buildContext.setNewBestPair(new Pair<>(buildContext.getCurrentPrivKey(), newAccuracy));

        //re-cache current points if needed
        if (buildContext.isExistingPointsCached()) {
            buildContext.updateExistingCachedPoints(pointsNew);
        }

        return pointsGained;
    }

    private int findOldPoints(@NonNull String oldPriv, AdvancedSearchSingleItemComparisonModel buildContext) {
        if (oldPriv.isEmpty()) {
            return 0; //nothing yet, give default value
        }
        if (buildContext.isPointMappingsCached()) {
            return buildContext.getExistingPoints();
        }

        //slower, but checks if old version is actually correct (rehashes and recalculates points)
        String unknownP2PKH = buildContext.getResultContainer().getHash();
        return calculateCurrentResult(oldPriv, unknownP2PKH, unknownP2PKH, JsonResultScaleFactorEnum.toScaleFactorEnum(buildContext.getScaleFactor())).getResultByType(buildContext.getType());
    }

    @Override
    public int exactMatchCheckDataLength() {
        return unknownPKHs.size();
    }

    @Override
    public boolean isExactMatchCheckEnabled() {
        return exactMatchCheckEnabled;
    }

    @Override
    public void updateTargetForExactMatchCheck(Set<String> unencodedAddresses) {
        if (!exactMatchCheckEnabled) {
            throw new IllegalStateException("Exact map check must be enabled before adding the map");
        }

        if (unencodedAddresses == null || unencodedAddresses.isEmpty()) {
            return;
        }

        LOG.info("Initializing PKHs for 'exact check option'...");

        List<String> validPKHs = unencodedAddresses.stream()
                .filter(key -> HeatVisualizerConstants.PATTERN_SIMPLE_40.matcher(key).matches())
                .map(key -> key.toLowerCase(Locale.ROOT))
                .collect(Collectors.toList());

        unknownPKHs.clear();
        unknownPKHs.addAll(validPKHs);

        LOG.info(unknownPKHs.size() + " PHKs have been cached for 'exact check option'");
    }

    protected boolean isPresentInAdditionalMap(String UPKH, String CPKH) {
        return unknownPKHs.contains(UPKH) || unknownPKHs.contains(CPKH);
    }

    public PubComparisonResultWrapper calculateCurrentResultCached(String currentPrivKey, String[] UPKHArray, String[] CPKHArray, P2PKHSingleResultData data) {
        return advancedPubComparer.getCurrentResultCached(currentPrivKey, UPKHArray, CPKHArray, data, getScaleFactor());
    }

}
