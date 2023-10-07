package com.bearlycattable.bait.advanced.searchHelper;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
import com.bearlycattable.bait.advancedCommons.pubKeyComparison.AdvancedPubComparerB;
import com.bearlycattable.bait.advancedCommons.pubKeyComparison.AdvancedPubComparerS;
import com.bearlycattable.bait.advancedCommons.pubKeyComparison.AdvancedPubComparisonResultB;
import com.bearlycattable.bait.advancedCommons.wrappers.AdvancedSearchTaskWrapper;
import com.bearlycattable.bait.commons.Config;
import com.bearlycattable.bait.commons.HeatVisualizerConstants;
import com.bearlycattable.bait.commons.contexts.TaskDiagnosticsModel;
import com.bearlycattable.bait.commons.enums.JsonResultScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.JsonResultTypeEnum;
import com.bearlycattable.bait.commons.enums.LogTextTypeEnum;
import com.bearlycattable.bait.commons.enums.AddressGenerationAndComparisonType;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.functions.TriConsumer;
import com.bearlycattable.bait.commons.helpers.AbstractGeneralSearchHelper;
import com.bearlycattable.bait.commons.helpers.HeatVisualizerHelper;
import com.bearlycattable.bait.commons.interfaces.CustomKeyGenerator;
import com.bearlycattable.bait.commons.interfaces.IndexRotatorHorizontal;
import com.bearlycattable.bait.commons.interfaces.IndexRotatorVertical;
import com.bearlycattable.bait.commons.interfaces.PrefixedKeyGenerator;
import com.bearlycattable.bait.commons.validators.SearchHelperIterationsValidator;
import com.bearlycattable.bait.commons.pubKeyComparison.PubComparisonResultSWrapper;
import com.bearlycattable.bait.utility.DurationUtils;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableStringValue;
import javafx.concurrent.Task;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import lombok.Getter;

public abstract class AbstractAdvancedSearchHelper extends AbstractGeneralSearchHelper implements AdvancedSearchHelper, CustomKeyGenerator, PrefixedKeyGenerator, IndexRotatorHorizontal,
        IndexRotatorVertical {

    private static final Logger LOG = Logger.getLogger(AbstractAdvancedSearchHelper.class.getName());

    @Getter
    private final HeatVisualizerHelper helper = new HeatVisualizerHelper();
    private final AdvancedPubComparerS advancedPubComparerS = new AdvancedPubComparerS();
    private final AdvancedPubComparerB advancedPubComparerB = new AdvancedPubComparerB();

    private final Set<String> unknownPKHs = new HashSet<>();
    private final Set<ByteBuffer> unknownPKHsB = new HashSet<>();

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

            //TODO: think about modifier type placement
            task = advancedSearchContext.getAddressGenerationAndComparisonType() == AddressGenerationAndComparisonType.STRING ? createNewGeneralAdvancedSearchTask(advancedSearchContext) : createNewGeneralAdvancedSearchTaskBytes(advancedSearchContext);
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

        return new Task<>() {
            private static final int FORTY = 40;
            private final TaskDiagnosticsModel diagnostics = context.getTaskDiagnosticsModel();

            private final String seed = context.getSeed();
            private final List<Integer> disabledWords = context.getDisabledWords();
            private final String parentThreadId = context.getParentThreadId();
            private final String childThreadId = diagnostics.getChildThreadId();

            private final P2PKHSingleResultData[] dataArray = context.getDataArray();
            private final ObservableStringValue observableProgressLabelValue = context.getObservableProgressLabelValue();
            private final int printSpacing = context.getPrintSpacing();
            private final int updateSpacing = context.getProgressSpacing();
            private final int notifyThreshold = context.getPointThresholdForNotify();
            private final TriConsumer<String, Color, LogTextTypeEnum> logConsumer = Objects.requireNonNull(context.getLogConsumer());
            private final SearchModeEnum searchMode = Objects.requireNonNull(context.getSearchMode());
            private final String prefix = context.getWordPrefix();
            private final boolean prefixed = prefix != null && HeatVisualizerConstants.PATTERN_HEX_01_TO_08.matcher(prefix).matches();
            private final int iterations = SearchHelperIterationsValidator.validateAndGet(searchMode, getIterations());
            // private final Function<String, String> buildNextPrivFunction = Objects.requireNonNull(context.getNextPrivFunction());
            private final boolean exactMatchCheckOnly = false; //TODO: cbx 'exact match check only'
            private final boolean exactMatchCheckEnabled = AbstractAdvancedSearchHelper.this.exactMatchCheckEnabled;
            private final boolean verbose = context.isVerbose();
            private final Map<String, String> LOWERCASE_HEX_ALPHABET = HeatVisualizerHelper.newLowercaseHexMap();

            @Override
            public P2PKHSingleResultData[] call() {
                String currentPriv = seed;

                initializeTemplateCaches(dataArray, getScaleFactor(), AddressGenerationAndComparisonType.STRING, verbose, logConsumer);

                final JsonResultScaleFactorEnum currentScaleFactor = ScaleFactorEnum.toJsonScaleFactorEnum(getScaleFactor());
                final boolean allPointMappingsCached = Arrays.stream(dataArray).allMatch(item -> item.isGeneralPointsCachedForScaleFactor(currentScaleFactor, AddressGenerationAndComparisonType.STRING));

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
                        .addressGenerationAndComparisonType(AddressGenerationAndComparisonType.STRING)
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

                    currentPriv = prefixed ? buildNextPrivPrefixed(currentPriv, disabledWords, prefix) : buildNextPriv(currentPriv, disabledWords);

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

                    PubComparisonResultSWrapper newResult;
                    String unknownP2PKH;

                    dataModel.setCurrentPrivKey(currentPriv);

                    for (P2PKHSingleResultData item : dataArray) {
                        dataModel.setResultContainer(item);

                        unknownP2PKH = item.getHash();
                        newResult = allPointMappingsCached ? calculateCurrentResultCached(currentPriv, UPKHArray, CPKHArray, item) : calculateCurrentResult(currentPriv, unknownP2PKH, unknownP2PKH,
                                getScaleFactor());

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
                String progressMessage = "Progress: " + calculateProgressPercent(i) + "% [parentTID=" + parentThreadId + ", childTID=" + childThreadId + ", i=" + i + ", key=" + currentPriv + ", mode="
                        + searchMode.getAbbr() + "] (" + DurationUtils.getCurrentDateTime() + ")";
                System.out.println(progressMessage);
                Platform.runLater(() -> logConsumer.accept(progressMessage, Color.GREEN, LogTextTypeEnum.SEARCH_PROGRESS));
            }

            private void updateProgress(int i, int iterations, ObservableStringValue observableProgressLabelValue) {
                updateProgress(i, iterations);
                updateProgressLabel(i, observableProgressLabelValue);
            }

            private String buildEndOfSearchMessage(long seconds, int totalNewResults, String firstKey, String lastKey) {
                StringBuilder sb = new StringBuilder();
                sb.append("First priv (this one is not checked): ").append(firstKey).append(System.lineSeparator())
                        .append("Last priv (this one is checked): ").append(lastKey).append(System.lineSeparator())
                        .append("Total number of iterations processed: ").append(iterations).append(System.lineSeparator())
                        .append("Total duration in seconds: ").append(seconds).append(System.lineSeparator())
                        .append("Total duration in a more convenient format: ").append(DurationUtils.getDurationDHMS(seconds)).append(System.lineSeparator())
                        .append("Total number of better results found during this loop: ").append(totalNewResults).append(System.lineSeparator())
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

    private Task<P2PKHSingleResultData[]> createNewGeneralAdvancedSearchTaskBytes(AdvancedSearchContext context) {

        return new Task<>() {
            private final TaskDiagnosticsModel diagnostics = context.getTaskDiagnosticsModel();

            private final String seed = context.getSeed();
            private final List<Integer> disabledWords = context.getDisabledWords();
            private final String parentThreadId = context.getParentThreadId();
            private final String childThreadId = diagnostics.getChildThreadId();

            private final P2PKHSingleResultData[] dataArray = context.getDataArray();
            private final ObservableStringValue observableProgressLabelValue = context.getObservableProgressLabelValue();
            private final int printSpacing = context.getPrintSpacing();
            private final int updateSpacing = context.getProgressSpacing();
            private final int notifyThreshold = context.getPointThresholdForNotify();
            private final TriConsumer<String, Color, LogTextTypeEnum> logConsumer = Objects.requireNonNull(context.getLogConsumer());
            private final SearchModeEnum searchMode = Objects.requireNonNull(context.getSearchMode());
            private final String prefix = context.getWordPrefix();
            private final boolean prefixed = prefix != null && HeatVisualizerConstants.PATTERN_HEX_01_TO_08.matcher(prefix).matches();
            private final int iterations = SearchHelperIterationsValidator.validateAndGet(searchMode, getIterations());
            private final boolean exactMatchCheckOnly = false; //TODO: cbx 'exact match check only'
            private final boolean exactMatchCheckEnabled = AbstractAdvancedSearchHelper.this.exactMatchCheckEnabled;
            private final boolean verbose = context.isVerbose();

            @Override
            public P2PKHSingleResultData[] call() {
                byte[] currentPriv = hexToByteData(seed);

                initializeTemplateCaches(dataArray, getScaleFactor(), AddressGenerationAndComparisonType.BYTE, verbose, logConsumer);
                convertExactMatchCheckMapToByteVersion();

                final JsonResultScaleFactorEnum currentScaleFactor = ScaleFactorEnum.toJsonScaleFactorEnum(getScaleFactor());
                final boolean allPointMappingsCached = Arrays.stream(dataArray).allMatch(item -> item.isGeneralPointsCachedForScaleFactor(currentScaleFactor, AddressGenerationAndComparisonType.BYTE));

                //cache existing points to avoid having to recalculate (stored in dataArray)
                P2PKHSingleResultDataHelper.revalidateAndInitCacheForExistingPoints(dataArray, ScaleFactorEnum.toJsonScaleFactorEnum(getScaleFactor()));

                //save current min points for every item in the template
                Map<String, Integer> currentMinPointsMap = P2PKHSingleResultDataHelper.createCurrentMinPointsMap(dataArray, ScaleFactorEnum.toJsonScaleFactorEnum(getScaleFactor())); //for every P2PKHSingleResultData item

                byte[] UPKHBytes;
                byte[] CPKHBytes;

                final Map<String, AdvancedPubComparisonResultB> allByteComparisonModels = createAllByteComparisonModels(dataArray, JsonResultScaleFactorEnum.toScaleFactorEnum(currentScaleFactor));

                final AdvancedSearchSingleItemComparisonModel dataModel = AdvancedSearchSingleItemComparisonModel.builder()
                        .logConsumer(logConsumer)
                        .verbose(verbose)
                        .scaleFactor(currentScaleFactor)
                        .addressGenerationAndComparisonType(AddressGenerationAndComparisonType.BYTE)
                        .pointThresholdForNotify(notifyThreshold)
                        .build();

                int count = 0; //counting num of better results in the ongoing run
                long start = System.nanoTime();

                for (int i = 0; i < iterations; i++) {
                    //if thread is cancelled at any point, we might want to know at which iteration it was stopped
                    updateMessage((i + 1) + ":" + bytesToHexString(currentPriv));

                    if (isCancelled()) { //task might be cancelled at any point while doing work
                        updateProgress((i + 1), iterations);
                        updateProgressLabel((i + 1), observableProgressLabelValue);
                        return dataArray;
                    }

                    currentPriv = prefixed ? buildNextPrivPrefixedBytes(currentPriv, disabledWords, prefix) : buildNextPrivBytes(currentPriv, disabledWords);

                    if ((i + 1) % printSpacing == 0) {
                        printCurrentKey((i + 1), searchMode, bytesToHexString(currentPriv), logConsumer);
                    }

                    if ((i + 1) % updateSpacing == 0) {
                        updateProgress(i, iterations, observableProgressLabelValue);
                    }

                    //do not check the same key multiple times
                    if (!isValidKey(currentPriv)) {
                        continue;
                    }

                    //use this so we dont need to recalculate hashes for the same keys multiple times
                    UPKHBytes = helper.getPubKeyHashUncompressedBytes(currentPriv, true);
                    CPKHBytes = helper.getPubKeyHashCompressedBytes(currentPriv, true);

                    if (exactMatchCheckEnabled && isPresentInAdditionalMap(UPKHBytes, CPKHBytes)) {
                        processExactMatchResult(i, iterations, bytesToHexString(currentPriv), bytesToHexString(UPKHBytes), bytesToHexString(CPKHBytes), logConsumer);
                        return dataArray;
                    }

                    if (exactMatchCheckOnly) {
                        continue;
                    }

                    //update arrays with current values of UPKH and CPKH
                    // updatePKHArray(UPKHArray, UPKH);
                    // updatePKHArray(CPKHArray, CPKH);

                    String unknownP2PKH;

                    // dataModel.setCurrentPrivKey(currentPriv);

                    for (P2PKHSingleResultData item : dataArray) {
                        unknownP2PKH = item.getHash();
                        dataModel.setResultContainer(item);
                        dataModel.setCurrentByteComparisonModel(allByteComparisonModels.get(unknownP2PKH));
                        dataModel.getCurrentByteComparisonModel().setCurrentUPKH(UPKHBytes);
                        dataModel.getCurrentByteComparisonModel().setCurrentCPKH(CPKHBytes);

                        // unknownP2PKH = item.getHash();
                        // newResult = dataModel.getCurrentByteComparisonModel(item.getHash());
                        // assert newResult != null : "Byte comparison model was null, this should never happen!";

                        // if (allPointMappingsCached) {
                        //     updateResultBModelCached(dataModel.getCurrentByteComparisonModel());
                        // } else {
                            updateResultBModel(dataModel.getCurrentByteComparisonModel());
                        // }

                        int oldMinPoints;
                        int pointsGained;
                        int pointsNew;

                        for (JsonResultTypeEnum type : JsonResultTypeEnum.values()) {
                            pointsNew = dataModel.getCurrentByteComparisonModel().getResultPointsByType(type);

                            //small optimization for long-running data sets (execution will rarely proceed past this point)
                            if (pointsNew <= currentMinPointsMap.get(unknownP2PKH)) {
                                continue;
                            }

                            // dataModel.setNewResult(newResult);
                            dataModel.setType(type);
                            dataModel.setCurrentPrivKey(bytesToHexString(currentPriv));

                            pointsGained = compareAndUpdatePairResults(dataModel);

                            if (pointsGained == -1) {
                                continue;
                            }

                            count++;

                            if (diagnostics.getHighestAcquiredPoints() < pointsGained) {
                                diagnostics.setHighestAcquiredPoints(pointsGained);
                            }

                            //update currentMinPointsMap (slow, but on old templates it will happen VERY very rarely to care about this
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

                String messageForDiagnostics = buildEndOfSearchMessage(seconds, count, seed, bytesToHexString(currentPriv));

                diagnostics.setLoopCompletionMessage(messageForDiagnostics);
                diagnostics.setTotalNumOfResultsFound(count);

                LOG.info(messageForDiagnostics);
                Platform.runLater(() -> logConsumer.accept(messageForDiagnostics, Color.WHEAT, LogTextTypeEnum.END_OF_SEARCH));

                return dataArray;
            }

            private Map<String, AdvancedPubComparisonResultB> createAllByteComparisonModels(P2PKHSingleResultData[] dataArray, ScaleFactorEnum scaleFactor) {
                Map<String, AdvancedPubComparisonResultB> map = new HashMap<>();

                Arrays.stream(dataArray).forEach(template -> {
                    map.put(template.getHash(), AdvancedPubComparisonResultB.builder()
                    .referencePKH(hexToByteData(template.getHash()))
                    .forScaleFactor(scaleFactor)
                    .build());
                });

                return map;
            }

            private byte[] hexToByteData(@NonNull String hex) {
                byte[] convertedByteArray = new byte[hex.length() / 2];
                int count = 0;

                for (int i = 0; i < hex.length() - 1; i += 2) {
                    String output;
                    output = hex.substring(i, (i + 2));
                    int decimal = Integer.parseInt(output, 16);
                    convertedByteArray[count] = (byte) (decimal & 0xFF);
                    count++;
                }
                return convertedByteArray;
            }

            private String bytesToHexString(byte[] hexbytes) {
                StringBuilder builder = new StringBuilder();
                for (byte a : hexbytes) {
                    int i = a & 0xFF;        //apply mask so result is always positive
                    if (i < 16) {
                        builder.append("0").append(Integer.toHexString(i)); //always want 2 symbols
                    } else {
                        builder.append(Integer.toHexString(i));
                    }
                }
                return builder.toString();
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
                String progressMessage = "Progress: " + calculateProgressPercent(i) + "% [parentTID=" + parentThreadId + ", childTID=" + childThreadId + ", i=" + i + ", key=" + currentPriv + ", mode="
                        + searchMode.getAbbr() + "] (" + DurationUtils.getCurrentDateTime() + ")";
                System.out.println(progressMessage);
                Platform.runLater(() -> logConsumer.accept(progressMessage, Color.GREEN, LogTextTypeEnum.SEARCH_PROGRESS));
            }

            private void updateProgress(int i, int iterations, ObservableStringValue observableProgressLabelValue) {
                updateProgress(i, iterations);
                updateProgressLabel(i, observableProgressLabelValue);
            }

            private String buildEndOfSearchMessage(long seconds, int totalNewResults, String firstKey, String lastKey) {
                StringBuilder sb = new StringBuilder();
                sb.append("First priv (this one is not checked): ").append(firstKey).append(System.lineSeparator())
                        .append("Last priv (this one is checked): ").append(lastKey).append(System.lineSeparator())
                        .append("Total number of iterations processed: ").append(iterations).append(System.lineSeparator())
                        .append("Total duration in seconds: ").append(seconds).append(System.lineSeparator())
                        .append("Total duration in a more convenient format: ").append(DurationUtils.getDurationDHMS(seconds)).append(System.lineSeparator())
                        .append("Total number of better results found during this loop: ").append(totalNewResults).append(System.lineSeparator())
                        .append("Highest points acquired on a single result (during this loop): ").append(diagnostics.getHighestAcquiredPoints());

                return sb.toString();
            }

            private void updatePKHArray(byte[] array, byte[] PKH) {
                System.arraycopy(PKH, 0, array, 0, PKH.length);
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

    private int compareAndUpdatePairResults(AdvancedSearchSingleItemComparisonModel comparisonModel) {
        Pair<String, Integer> currentBest = comparisonModel.getCurrentBestPair();
        String oldPriv = currentBest.getKey();
        int oldAccuracy = currentBest.getValue();

        int pointsOld = findOldPoints(oldPriv, comparisonModel);
        int pointsNew = comparisonModel.getNewPoints();

        if (pointsNew <= pointsOld) {
            return -1; //no better result found, leave as is
        }

        int newAccuracy = getSimilarityMappings().get(helper.recalculateIndexForSimilarityMappings(pointsNew, getScaleFactor())).setScale(0, RoundingMode.HALF_UP).intValue();
        int pointsGained = (pointsNew - pointsOld);

        //dev fun
        textToSpeechPointsGained(pointsGained);

        printMessageFoundBetterResult(comparisonModel, pointsGained, pointsNew, oldAccuracy, newAccuracy);
        printMessageKeySwap(comparisonModel, oldPriv);
        playNotificationSoundMaybe(comparisonModel, pointsGained);

        //save new result
        comparisonModel.setNewBestPair(new Pair<>(comparisonModel.getCurrentPrivKey(), newAccuracy));

        //re-cache current points if needed
        if (comparisonModel.isExistingPointsCached()) {
            comparisonModel.updateExistingCachedPoints(pointsNew);
        }

        return pointsGained;
    }

    private void printMessageKeySwap(AdvancedSearchSingleItemComparisonModel comparisonModel, String oldPriv) {
        if (comparisonModel.isVerbose()) {
            String keySwapMessage = "Swapped key to: " + comparisonModel.getCurrentPrivKey() + " [targetPHK=" + comparisonModel.getResultContainer().getHash() + ", type " + comparisonModel.getType() + ", oldKey=" + oldPriv + "]";
            LOG.info(keySwapMessage);
            Platform.runLater(() -> comparisonModel.getLogConsumer().accept(keySwapMessage, Color.GREEN, LogTextTypeEnum.KEY_SWAP));
        }
    }

    private void printMessageFoundBetterResult(AdvancedSearchSingleItemComparisonModel comparisonModel, int pointsGained, int updatedPoints, int oldAccuracy, int newAccuracy) {
        String betterResultFoundMessage = "Better result found: --- +" + pointsGained + " point(s) --- [total: " + updatedPoints + "/" + getScaleFactor().getMaxPoints() + "; acc: " + oldAccuracy + " --> " + newAccuracy + "] (" + DurationUtils.getCurrentDateTime() + ")";
        System.out.println(betterResultFoundMessage);
        Platform.runLater(() -> comparisonModel.getLogConsumer().accept(betterResultFoundMessage, Color.GREEN, LogTextTypeEnum.POINTS_GAINED));

    }

    private void playNotificationSoundMaybe(AdvancedSearchSingleItemComparisonModel comparisonModel, int pointsGained) {
        if (comparisonModel.getPointThresholdForNotify() > 0 && pointsGained >= comparisonModel.getPointThresholdForNotify()) {
            ShortSoundEffects.SINGLE_BEEP.play();
        }
    }

    private void textToSpeechPointsGained(int pointsGained) {
        if (System.getProperty("os.name").contains("Windows")) {
            try {
                Runtime.getRuntime().exec("PowerShell -Command \"Add-Type -AssemblyName System.Speech; (New-Object System.Speech.Synthesis.SpeechSynthesizer).Speak('" + pointsGained + " points gained');\"");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    protected void convertExactMatchCheckMapToByteVersion() {
        if (!exactMatchCheckEnabled || unknownPKHs == null || unknownPKHs.size() == 0) {
            return;
        }

        LOG.info("Converting PKHs (byte version) for 'exact match check' option...");

        List<ByteBuffer> validPKHs = unknownPKHs.stream()
                .filter(key -> HeatVisualizerConstants.PATTERN_SIMPLE_40.matcher(key).matches())
                .map(key -> ByteBuffer.wrap(helper.hexToByteData(key)).asReadOnlyBuffer())
                .collect(Collectors.toList());

        unknownPKHsB.clear();
        unknownPKHsB.addAll(validPKHs);

        unknownPKHs.clear();

        LOG.info(unknownPKHsB.size() + " PHKs have been converted from strings to their byte versions (for 'exact match check')");
    }

    protected boolean isPresentInAdditionalMap(String UPKH, String CPKH) {
        return unknownPKHs.contains(UPKH) || unknownPKHs.contains(CPKH);
    }

    protected boolean isPresentInAdditionalMap(byte[] UPKH, byte[] CPKH) {
        return unknownPKHsB.contains(ByteBuffer.wrap(UPKH)) || unknownPKHsB.contains(ByteBuffer.wrap(CPKH));
    }

    public PubComparisonResultSWrapper calculateCurrentResultCached(String currentPrivKey, String[] UPKHArray, String[] CPKHArray, P2PKHSingleResultData data) {
        return advancedPubComparerS.calculateCurrentResultCachedS(currentPrivKey, UPKHArray, CPKHArray, data, getScaleFactor());
    }

    public void updateResultBModel(AdvancedPubComparisonResultB model) {
        advancedPubComparerB.comparePubKeyHashesB(model);
    }

    public void updateResultBModelCached(AdvancedPubComparisonResultB model) {
        advancedPubComparerB.comparePubKeyHashesCachedB(model);
    }

    private void initializeTemplateCaches(P2PKHSingleResultData[] deepCopy, ScaleFactorEnum scaleFactor, AddressGenerationAndComparisonType cacheType, boolean verbose, TriConsumer<String, Color, LogTextTypeEnum> logConsumer) {
        if (deepCopy.length <= Config.MAX_CACHEABLE_ADDRESSES_IN_TEMPLATE) {
            P2PKHSingleResultDataHelper.initializeCaches(deepCopy, ScaleFactorEnum.toJsonScaleFactorEnum(scaleFactor), cacheType);
            if (verbose) {
                String cachedSuccessfully = "All templates have been cached";
                LOG.info(cachedSuccessfully);
                Platform.runLater(() -> logConsumer.accept(cachedSuccessfully, Color.GREEN, LogTextTypeEnum.START_OF_SEARCH));
            }
            return;
        }

        if (!verbose) {
            return;
        }

        String templatesCannotBeCached = "Proceeding with uncached search!";
        LOG.info(templatesCannotBeCached);
        Platform.runLater(() -> logConsumer.accept(templatesCannotBeCached, Color.DARKORANGE, LogTextTypeEnum.START_OF_SEARCH));
    }

    /*------ implementing classes will override the methods they want: ------- */

    @Override
    public @NonNull String rotateLeft(String current) {
        throw new UnsupportedOperationException("Extending class must override this method. Can't use at " + this.getClass().getName() + "#rotateLeft(String)");
    }

    @Override
    public @NonNull String rotateLeftBy(String current, int rotateBy) {
        throw new UnsupportedOperationException("Extending class must override this method. Can't use at " + this.getClass().getName() + "#rotateLeftBy(String, int)");
    }

    @Override
    public @NonNull String rotateLeft(String current, List<Integer> disabledWords) {
        throw new UnsupportedOperationException("Extending class must override this method. Can't use at " + this.getClass().getName() + "#rotateLeft(String, List<Integer>)");
    }

    @Override
    public @NonNull String rotateLeftBy(String current, int rotateBy, List<Integer> disabledWords) {
        throw new UnsupportedOperationException("Extending class must override this method. Can't use at " + this.getClass().getName() + "#rotateLeftBy(String, int, List<Integer>)");
    }

    @Override
    public @NonNull String rotateAtIndex(String current, List<Integer> disabledWords, int index) {
        throw new UnsupportedOperationException("Extending class must override this method. Can't use at " + this.getClass().getName() + "#rotateAtIndex(String, List<Integer>, int)");
    }

    @Override
    public boolean isValidIndexForVerticalRotation(String address, List<Integer> disabledWords, int selectedIndex) {
        throw new UnsupportedOperationException("Extending class must override this method. Can't use at " + this.getClass().getName() + "#isValidIndexForVerticalRotation(String, List<Integer>, int)");
    }

    @Override
    public @NonNull String buildNextPriv(String current, List<Integer> disabledWords) {
        throw new UnsupportedOperationException("Extending class must override this method. Can't use at " + this.getClass().getName() + "#buildNextPriv(String, List<Integer>)");
    }

    @Override
    public byte[] buildNextPrivBytes(byte[] current, List<Integer> disabledWords) {
        throw new UnsupportedOperationException("Extending class must override this method. Can't use at " + this.getClass().getName() + "#buildNextPrivBytes(byte[], List<Integer>)");
    }

    @Override
    public String buildNextPrivPrefixed(String current, List<Integer> disabledWords, String prefix) {
        throw new UnsupportedOperationException("Extending class must override this method. Can't use at " + this.getClass().getName() + "#buildNextPrivPrefixed(String, List<Integer>, String)");
    }

    @Override
    public byte[] buildNextPrivPrefixedBytes(byte[] current, List<Integer> disabledWords, String prefix) {
        throw new UnsupportedOperationException("Extending class must override this method. Can't use at " + this.getClass().getName() + "#buildNextPrivPrefixedBytes(String, List<Integer>, String)");
    }

}
