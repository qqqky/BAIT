package com.bearlycattable.bait.bl.searchHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.commons.BaitConstants;
import com.bearlycattable.bait.commons.contexts.QuickSearchContext;
import com.bearlycattable.bait.commons.contexts.QuickSearchResponseModel;
import com.bearlycattable.bait.commons.contexts.SimpleSearchHelperCreationContext;
import com.bearlycattable.bait.commons.enums.QuickSearchResponseEnum;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.helpers.AbstractBaseQuickSearchHelper;
import com.bearlycattable.bait.commons.interfaces.CustomKeyGenerator;
import com.bearlycattable.bait.commons.interfaces.IndexRotatorHorizontal;
import com.bearlycattable.bait.commons.interfaces.IndexRotatorVertical;
import com.bearlycattable.bait.commons.interfaces.PrefixedKeyGenerator;
import com.bearlycattable.bait.commons.interfaces.QuickSearchHelper;
import com.bearlycattable.bait.commons.validators.SearchHelperIterationsValidator;
import com.bearlycattable.bait.commons.pubKeyComparison.PubComparisonResultSWrapper;
import com.bearlycattable.bait.commons.wrappers.QuickSearchTaskWrapper;

import javafx.concurrent.Task;
import lombok.Getter;

@Getter
public abstract class AbstractQuickSearchHelper extends AbstractBaseQuickSearchHelper implements QuickSearchHelper, CustomKeyGenerator, PrefixedKeyGenerator, IndexRotatorHorizontal,
        IndexRotatorVertical {

    private static final Logger LOG = Logger.getLogger(AbstractQuickSearchHelper.class.getName());
    private final ScaleFactorEnum scaleFactor;
    private final SimpleSearchHelperCreationContext creationContext;
    private int accuracy;

    private AbstractQuickSearchHelper() {
        throw new UnsupportedOperationException("Creation of AbstractSimpleSearchHelper without context is not allowed");
    }

    protected AbstractQuickSearchHelper(@NonNull SimpleSearchHelperCreationContext creationContext) {
        super(creationContext);
        this.accuracy = creationContext.getAccuracy();
        this.scaleFactor = creationContext.getScaleFactor();
        this.creationContext = creationContext;
    }

    protected final QuickSearchTaskWrapper quickSearchTaskGuiCreationHelper(QuickSearchContext quickSearchContext) {
        quickSearchContext.setIterations(SearchHelperIterationsValidator.validateAndGet(quickSearchContext.getSearchMode(), quickSearchContext.getIterations()));

        Optional<String> validationError = quickSearchContext.validate();
        if (validationError.isPresent()) {
            return QuickSearchTaskWrapper.builder()
                    .task(null)
                    .error("Some QuickSearch parameters appear to be invalid. Reason: " + validationError.get())
                    .build();
        }

        Task<PubComparisonResultSWrapper> task;

        SearchModeEnum mode = quickSearchContext.getSearchMode();
        if (SearchModeEnum.MIXED == mode || SearchModeEnum.FUZZING == mode) {
            return QuickSearchTaskWrapper.builder()
                    .task(null)
                    .error("Mode not supported for QuickSearch at AbstractQuickSearchHelper [mode=" + mode + "]")
                    .build();
        } else if (SearchModeEnum.isVerticalRotationMode(mode)) {
            task = createNewVerticalRotationQuickSearchTask(quickSearchContext);
        } else if (SearchModeEnum.isHorizontalRotationMode(mode)) {
            task = createNewHorizontalRotationQuickSearchTask(quickSearchContext);
        } else {
            //this is for incDec modes and random modes
            task = createNewGeneralQuickSearchTask(quickSearchContext);
        }

        return QuickSearchTaskWrapper.builder()
                .task(task)
                .error(null)
                .build();
    }

    private Task<PubComparisonResultSWrapper> createNewGeneralQuickSearchTask(QuickSearchContext quickSearchContext) {
        return new Task<>() {
            int iterations = quickSearchContext.getIterations();
            int accuracy = quickSearchContext.getAccuracy();
            String seed = quickSearchContext.getSeed();
            List<Integer> disabledWords = quickSearchContext.getDisabledWords();
            final boolean prefixed = SearchModeEnum.RANDOM_SAME_WORD == quickSearchContext.getSearchMode();
            // Function<String, String> nextPrivFunction = quickSearchContext.getNextPrivFunction();
            BiFunction<QuickSearchResponseModel, String, QuickSearchResponseModel> evaluationFunction = createGeneralEvaluationFunction(quickSearchContext);
            final int printSpacing = quickSearchContext.getPrintSpacing();
            final boolean verbose = quickSearchContext.isVerbose();

            @Override
            public PubComparisonResultSWrapper call() {
                String currentPriv = seed;

                SearchModeEnum searchMode = quickSearchContext.getSearchMode();

                QuickSearchResponseModel model = new QuickSearchResponseModel();
                model.setHighestResult(PubComparisonResultSWrapper.empty());

                for (int i = 0; i < iterations; i++) {
                    seed = prefixed ? buildNextPrivPrefixed(seed, disabledWords, null) : buildNextPriv(seed, disabledWords);

                    if (verbose && ((i + 1) % printSpacing == 0)) {
                        System.out.println("Current priv (current mode=" + searchMode + ") is " + "[" + (i + 1) + "]: " + seed);
                    }
                    QuickSearchResponseModel response = evaluationFunction.apply(model, seed);

                    if (QuickSearchResponseEnum.BREAK == response.getResponseCommand()) {
                        if (verbose) {
                            System.out.println("Result found in " + (i + 1) + " iterations (for requested accuracy of " + accuracy + "%)");
                        }
                        updateProgress((i + 1), iterations);
                        break;
                    }
                    updateProgress((i + 1), iterations);
                }
                return model.getHighestResult();
            }
        };
    }

    private Task<PubComparisonResultSWrapper> createNewHorizontalRotationQuickSearchTask(QuickSearchContext quickSearchContext) {
        return new Task<>() {
            @Override
            public PubComparisonResultSWrapper call() {
                int iterations = quickSearchContext.getIterations();
                int accuracy = quickSearchContext.getAccuracy();
                String seed = quickSearchContext.getSeed();
                List<Integer> disabledWords = quickSearchContext.getDisabledWords();
                String targetPriv = quickSearchContext.getTargetPriv();
                BiFunction<QuickSearchResponseModel, String, QuickSearchResponseModel> evaluationFunction = createGeneralEvaluationFunction(quickSearchContext);
                final int printSpacing = quickSearchContext.getPrintSpacing();
                final boolean verbose = quickSearchContext.isVerbose();

                SearchModeEnum searchMode = quickSearchContext.getSearchMode();
                final boolean fullPrefixedMode = SearchModeEnum.ROTATION_FULL_WITH_HEADER == searchMode;
                String savedSeed = seed;

                QuickSearchResponseModel model = new QuickSearchResponseModel();
                model.setHighestResult(PubComparisonResultSWrapper.empty());

                for (int i = 0; i < iterations; i++) {
                    if (!fullPrefixedMode) {
                        seed = rotateLeft(seed, disabledWords);
                    } else {
                        seed = rotateLeftBy(savedSeed, (i + 1));
                    }

                    if (verbose && ((i + 1) % printSpacing == 0)) {
                        System.out.println("Current priv (current mode=" + searchMode + ") is " + "[" + (i + 1) + "]: " + seed);
                    }
                    if (targetPriv != null && targetPriv.equals(seed)) {
                        updateProgress((i + 1), iterations);
                        continue;
                    }

                    QuickSearchResponseModel response = evaluationFunction.apply(model, seed);

                    if (QuickSearchResponseEnum.BREAK == response.getResponseCommand()) {
                        if (verbose) {
                            System.out.println("Result found in " + (i + 1) + " iterations (for requested accuracy of " + accuracy + "%)");
                        }
                        updateProgress((i + 1), iterations);
                        break;
                    }
                    updateProgress((i + 1), iterations);
                }
                return model.getHighestResult();
            }
        };
    }

    private Task<PubComparisonResultSWrapper> createNewVerticalRotationQuickSearchTask(QuickSearchContext quickSearchContext) {
        return new Task<>() {
            @Override
            public PubComparisonResultSWrapper call() {
                int iterations = quickSearchContext.getIterations();
                int accuracy = quickSearchContext.getAccuracy();
                String seed = quickSearchContext.getSeed();
                final List<Integer> disabledWords = quickSearchContext.getDisabledWords();
                final String targetPriv = quickSearchContext.getTargetPriv();
                final BiFunction<QuickSearchResponseModel, String, QuickSearchResponseModel> evaluationFunction = createGeneralEvaluationFunction(quickSearchContext);
                final SearchModeEnum searchMode = quickSearchContext.getSearchMode();
                final int printSpacing = quickSearchContext.getPrintSpacing();
                final boolean verbose = quickSearchContext.isVerbose();

                QuickSearchResponseModel model = new QuickSearchResponseModel();
                model.setHighestResult(PubComparisonResultSWrapper.empty());

                final int maxRotations = 0xF;
                String start = seed;
                int highestPoints = model.getHighestResult().getHighestPoints();

                INDEX_LOOP:
                for (int i = 0; i < iterations; i++) {
                    seed = start;
                    //check if index is valid for vertical rotation (not locked), otherwise we will get null
                    if (!isValidIndexForVerticalRotation(seed, disabledWords, i)) {
                        updateProgress((i + 1), iterations);
                        continue;
                    }

                    for (int j = 0; j < maxRotations; j++) {
                        seed = rotateAtIndex(seed, disabledWords, i);

                        if (verbose && ((i + 1) % printSpacing == 0)) {
                            System.out.println("Current priv (current mode=" + searchMode + ") is " + "[" + (i + 1) + "]: " + seed);
                        }
                        if (targetPriv != null && targetPriv.equals(seed)) {
                            updateProgress((i + 1), iterations);
                            continue;
                        }

                        QuickSearchResponseModel response = evaluationFunction.apply(model, seed);

                        if (QuickSearchResponseEnum.BREAK == response.getResponseCommand()) {
                            if (verbose) {
                                System.out.println("Result found in " + (i + 1) + " iterations (for requested accuracy of " + accuracy + "%)");
                            }
                            updateProgress((i + 1), iterations);
                            break INDEX_LOOP;
                        }
                    }
                    if (highestPoints < model.getHighestResult().getHighestPoints()) {
                        start = model.getHighestResult().getCommonPriv();
                        highestPoints = model.getHighestResult().getHighestPoints();
                        if (i != 0) {
                            i = -1; //and restart from beginning with a new seed
                        }
                    }
                    updateProgress((i + 1), iterations);
                }
                return model.getHighestResult();
            }
        };
    }

    private Task<PubComparisonResultSWrapper> createNewFuzzingQuickSearchTask(QuickSearchContext quickSearchContext) {
        throw new UnsupportedOperationException(getClass().getName() + "#createNewFuzzingQuickSearchTask() unavailable in current version!");
    }

    private Task<PubComparisonResultSWrapper> createNewMixedQuickSearchTask(QuickSearchContext quickSearchContext) {
        throw new UnsupportedOperationException(getClass().getName() + "#createNewMixedQuickSearchTask() unavailable in current version!");
    }

    public List<Integer> makeEnabledWordsList(List<Integer> disabledWords) {
        if (disabledWords == null || disabledWords.isEmpty()) {
            return new ArrayList<>(BaitConstants.ALL_WORD_NUMBERS);
        }

        return BaitConstants.ALL_WORD_NUMBERS.stream()
                .filter(currentWordNum -> !disabledWords.contains(currentWordNum))
                .collect(Collectors.toList());
    }

    // private Pair<String, String> deriveTargetPKHs(@NonNull QuickSearchContext quickSearchContext) {
    //     String targetUPKH;
    //     String targetCPKH;
    //
    //     switch (quickSearchContext.getType()) {
    //         case COLLISION:
    //             targetUPKH = helper.getPubKeyHashUncompressed(quickSearchContext.getTargetPriv(), false);
    //             targetCPKH = helper.getPubKeyHashCompressed(quickSearchContext.getTargetPriv(), false);
    //             break;
    //         case BLIND:
    //             targetUPKH = quickSearchContext.getTargetPub();
    //             targetCPKH = quickSearchContext.getTargetPub();
    //             break;
    //         default:
    //             throw new IllegalStateException("Type is not supported at AbstractSimpleSearchHelper#deriveTargetPKHs: " + quickSearchContext.getType());
    //     }
    //     return new Pair<>(targetUPKH, targetCPKH);
    // }

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
