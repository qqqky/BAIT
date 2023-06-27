package com.bearlycattable.bait.bl.searchHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.commons.HeatVisualizerConstants;
import com.bearlycattable.bait.commons.contexts.QuickSearchContext;
import com.bearlycattable.bait.commons.contexts.QuickSearchResponseModel;
import com.bearlycattable.bait.commons.contexts.SimpleSearchHelperCreationContext;
import com.bearlycattable.bait.commons.enums.QuickSearchComparisonType;
import com.bearlycattable.bait.commons.enums.QuickSearchResponseEnum;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.helpers.GeneralSearchHelper;
import com.bearlycattable.bait.commons.helpers.HeatVisualizerHelper;
import com.bearlycattable.bait.commons.interfaces.SimpleSearchHelper;
import com.bearlycattable.bait.commons.other.PubComparer;
import com.bearlycattable.bait.commons.wrappers.PubComparisonResultWrapper;
import com.bearlycattable.bait.commons.validators.SearchHelperIterationsValidator;

import javafx.util.Pair;
import lombok.Getter;

@Getter
public abstract class AbstractSimpleSearchHelper extends GeneralSearchHelper implements SimpleSearchHelper {

    private static final Logger LOG = Logger.getLogger(AbstractSimpleSearchHelper.class.getName());

    private final HeatVisualizerHelper helper = new HeatVisualizerHelper();
    private final PubComparer pubComparer = new PubComparer();
    private final ScaleFactorEnum scaleFactor;
    private final SimpleSearchHelperCreationContext creationContext;
    private int accuracy;
    private boolean dynamicAccuracy;

    private AbstractSimpleSearchHelper() {
        throw new UnsupportedOperationException("Creation of AbstractSimpleSearchHelper without context is not allowed");
    }

    protected AbstractSimpleSearchHelper(@NonNull SimpleSearchHelperCreationContext creationContext) {
        super(creationContext);
        this.accuracy = creationContext.getAccuracy();
        this.scaleFactor = creationContext.getScaleFactor();
        this.creationContext = creationContext;
    }

    // public PubComparisonResultWrapper calculateCurrentResult(String currentPrivKey, String lockedPKHUncompressed, String lockedPKHCompressed) {
    //     return pubComparer.getCurrentResult(currentPrivKey, lockedPKHUncompressed, lockedPKHCompressed, scaleFactor);
    // }

    // public PubComparisonResultWrapper calculateCurrentResultCached(String currentPrivKey, String[] UPKHArray, String[] CPKHArray, P2PKHSingleResultData data) {
    //     return pubComparer.getCurrentResultCached(currentPrivKey, UPKHArray, CPKHArray, data, scaleFactor);
    // }

    public List<Integer> makeEnabledWordsList(List<Integer> disabledWords) {
        if (disabledWords == null || disabledWords.isEmpty()) {
            return new ArrayList<>(HeatVisualizerConstants.ALL_WORD_NUMBERS);
        }

        return HeatVisualizerConstants.ALL_WORD_NUMBERS.stream()
                .filter(currentWordNum -> !disabledWords.contains(currentWordNum))
                .collect(Collectors.toList());
    }

    boolean isEmpty(PubComparisonResultWrapper wrapper) {
        return wrapper == null || wrapper.equalsEmpty();
    }

    public boolean isMoreOrEqualToRequestedAccuracy(PubComparisonResultWrapper highestResult, int requestedAccuracy) {
        return highestResult.resultStream().anyMatch(holder -> getSimilarityMappings().get(helper.recalculateIndexForSimilarityMappings(holder.getHighest(), getScaleFactor())).intValue() >= requestedAccuracy);
    }

    public boolean isMoreThanRequestedAccuracy(PubComparisonResultWrapper highestResult, int requestedAccuracy) {
        return highestResult.resultStream().anyMatch(holder -> getSimilarityMappings().get(helper.recalculateIndexForSimilarityMappings(holder.getHighest(), getScaleFactor())).intValue() > requestedAccuracy);
    }

    protected void setDynamicAccuracy(boolean isDynamic) {
        this.dynamicAccuracy = isDynamic;
    }

    private PubComparisonResultWrapper iterateGeneral(@NonNull QuickSearchContext quickSearchContext) {
        //unpack
        int requestedAccuracy = quickSearchContext.getAccuracy();
        String seed = SearchHelperUtils.determineInitialSeed(quickSearchContext);
        Function<String, String> nextPrivFunction = quickSearchContext.getNextPrivFunction();
        SearchModeEnum searchMode = quickSearchContext.getSearchMode();
        final boolean verbose = quickSearchContext.isVerbose();
        final int iterations = getIterations();

        String targetUPKH;
        String targetCPKH;
        switch (quickSearchContext.getType()) {
            case COLLISION:
                targetUPKH = helper.getPubKeyHashUncompressed(quickSearchContext.getTargetPriv(), false);
                targetCPKH = helper.getPubKeyHashCompressed(quickSearchContext.getTargetPriv(), false);
                break;
            case BLIND:
                targetUPKH = quickSearchContext.getTargetPub();
                targetCPKH = quickSearchContext.getTargetPub();
                break;
            default:
                throw new IllegalStateException("Type is not supported at AbstractSimpleSearchHelper#iterateGeneral: " + quickSearchContext.getType());
        }

        QuickSearchResponseModel model = new QuickSearchResponseModel();
        model.setHighestResult(PubComparisonResultWrapper.empty());

        for (int i = 0; i < iterations; i++) {
            seed = nextPrivFunction.apply(seed);

            if (verbose) {
                System.out.println("Current priv (current mode=" + searchMode + ") is " + "[" + (i + 1) + "]: " + seed);
            }

            //model is updated here
            QuickSearchResponseModel response = compareAndUpdateModel(model, seed, targetUPKH, targetCPKH, requestedAccuracy);

            if (QuickSearchResponseEnum.BREAK == response.getResponseCommand()) {
                LOG.info("Result found in " + (i + 1) + " iterations (for requested accuracy of " + requestedAccuracy + "%)");
                break;
            }
        }

        return model.getHighestResult();
    }

    protected PubComparisonResultWrapper iterateForVertical(@NonNull QuickSearchContext quickSearchContext) {
        //unpack
        int requestedAccuracy = quickSearchContext.getAccuracy();
        String currentSeed = SearchHelperUtils.determineInitialSeed(quickSearchContext);
        BiFunction<String, Integer, String> nextPrivFunction = quickSearchContext.getNextPrivFunctionVertical();
        BiPredicate<String, Integer> validityCheckFunction = quickSearchContext.getValidityCheckFunction();
        //iterations must be enforced for vertical rotation type
        int iterations = SearchHelperIterationsValidator.validateAndGet(quickSearchContext.getSearchMode(), quickSearchContext.getIterations());
        SearchModeEnum searchMode = quickSearchContext.getSearchMode();
        final boolean verbose = quickSearchContext.isVerbose();
        final String targetPriv = quickSearchContext.getTargetPriv();

        Pair<String, String> targetPKHs = deriveTargetPKHs(quickSearchContext);
        String targetUPKH = targetPKHs.getKey();
        String targetCPKH = targetPKHs.getValue();

        QuickSearchResponseModel model = new QuickSearchResponseModel();
        model.setHighestResult(PubComparisonResultWrapper.empty());

        final int maxRotations = 0xF;
        String nextSeed = currentSeed;
        int highestPoints = model.getHighestResult().getHighestPoints();

        INDEX_LOOP: for (int i = 0; i < iterations; i++) {
            currentSeed = nextSeed;
            //check if index is valid for vertical rotation (not locked), otherwise we will get null
            if (!validityCheckFunction.test(currentSeed, i)) {
                continue;
            }

            for (int j = 0; j < maxRotations; j++) {
                currentSeed = nextPrivFunction.apply(currentSeed, i);

                if (verbose) {
                    System.out.println("Current priv (current mode=" + searchMode + ") is " + "[" + (i + 1) + "]: " + currentSeed);
                }

                if (targetPriv != null && targetPriv.equals(currentSeed)) {
                    System.out.println("Keys are equal. Continuing...");
                    continue;
                }

                QuickSearchResponseModel response = compareAndUpdateModel(model, currentSeed, targetUPKH, targetCPKH, requestedAccuracy);

                if (QuickSearchResponseEnum.BREAK == response.getResponseCommand()) {
                    LOG.info("Result found in " + (i + 1) + " iterations (for requested accuracy of " + requestedAccuracy + "%)");
                    break INDEX_LOOP;
                }
            }
            if (highestPoints < model.getHighestResult().getHighestPoints()) {
                nextSeed = model.getHighestResult().getCommonPriv();
                highestPoints = model.getHighestResult().getHighestPoints();
                if (i != 0) {
                    i = 0; //and restart from beginning with a new seed
                }
            }
        }

        if (verbose) {
            LOG.info("Highest result found: " + model.getHighestResult().getCommonPriv());
        }

        return model.getHighestResult();
    }

    private PubComparisonResultWrapper iterateForHorizontal(@NonNull QuickSearchContext quickSearchContext) {
        //unpack
        int requestedAccuracy = quickSearchContext.getAccuracy();
        String seed = SearchHelperUtils.determineInitialSeed(quickSearchContext);
        Function<String, String> nextPrivFunction = quickSearchContext.getNextPrivFunction();
        BiFunction<String, Integer, String> nextPrivFunctionForFullPrefixed = quickSearchContext.getNextPrivFunctionFullPrefixed();
        //iterations must be enforced for any horizontal rotation type
        int iterations = SearchHelperIterationsValidator.validateAndGet(quickSearchContext.getSearchMode(), quickSearchContext.getIterations());
        SearchModeEnum searchMode = quickSearchContext.getSearchMode();
        final boolean verbose = quickSearchContext.isVerbose();
        final String targetPriv = quickSearchContext.getTargetPriv();

        Pair<String, String> targetPKHs = deriveTargetPKHs(quickSearchContext);
        String targetUPKH = targetPKHs.getKey();
        String targetCPKH = targetPKHs.getValue();

        QuickSearchResponseModel model = new QuickSearchResponseModel();
        model.setHighestResult(PubComparisonResultWrapper.empty());
        final boolean fullPrefixedMode = SearchModeEnum.ROTATION_PRIV_FULL_PREFIXED == searchMode;
        String savedSeed = seed;

        for (int i = 0; i < iterations; i++) {
            if (!fullPrefixedMode) {
                seed = nextPrivFunction.apply(seed);
            } else {
                seed = nextPrivFunctionForFullPrefixed.apply(savedSeed, (i + 1));
            }

            if (verbose) {
                System.out.println("Current priv (current mode=" + searchMode + ") is " + "[" + (i + 1) + "]: " + seed);
            }

            if (targetPriv != null && targetPriv.equals(seed)) {
                // System.out.println("Keys are equal. Continuing...");
                continue;
            }

            QuickSearchResponseModel response = compareAndUpdateModel(model, seed, targetUPKH, targetCPKH, requestedAccuracy);

            if (QuickSearchResponseEnum.BREAK == response.getResponseCommand()) {
                LOG.info("Result found in " + (i + 1) + " iterations (for requested accuracy of " + requestedAccuracy + "%)");
                break;
            }
        }

        return model.getHighestResult();
    }

    private Pair<String, String> deriveTargetPKHs(@NonNull QuickSearchContext quickSearchContext) {
        String targetUPKH;
        String targetCPKH;

        switch (quickSearchContext.getType()) {
            case COLLISION:
                targetUPKH = helper.getPubKeyHashUncompressed(quickSearchContext.getTargetPriv(), false);
                targetCPKH = helper.getPubKeyHashCompressed(quickSearchContext.getTargetPriv(), false);
                break;
            case BLIND:
                targetUPKH = quickSearchContext.getTargetPub();
                targetCPKH = quickSearchContext.getTargetPub();
                break;
            default:
                throw new IllegalStateException("Type is not supported at AbstractSimpleSearchHelper#deriveTargetPKHs: " + quickSearchContext.getType());
        }
        return new Pair<>(targetUPKH, targetCPKH);
    }

    protected final QuickSearchResponseModel compareAndUpdateModel(@NonNull QuickSearchResponseModel model, String currentPrivKey, String targetPKHUncompressed, String targetPKHCompressed, int requestedAccuracy) {
        if (!isValidKey(currentPrivKey)) {
            model.setResponseCommand(QuickSearchResponseEnum.CONTINUE);
            return model;
        }

        PubComparisonResultWrapper current = calculateCurrentResult(currentPrivKey, targetPKHUncompressed, targetPKHCompressed, getScaleFactor());
        if (isEmpty(current)) {
            model.setResponseCommand(QuickSearchResponseEnum.CONTINUE);
            return model;
        }

        model.setHighestResult(pubComparer.selectBest(model.getHighestResult(), current));

        if (isMoreOrEqualToRequestedAccuracy(model.getHighestResult(), requestedAccuracy)) {
            model.setResponseCommand(QuickSearchResponseEnum.BREAK);
        } else {
            model.setResponseCommand(QuickSearchResponseEnum.IGNORE);
        }

        return model;
    }

    //TODO: change args to 'MixedSearchEvaluationContext'
    protected final QuickSearchResponseModel checkAndEvaluateMixed(QuickSearchResponseModel subResult, QuickSearchResponseModel highest, String lockedPKHUncompressed, String lockedPKHCompressed,
            SimpleSearchHelper currentSearcher, SearchModeEnum searchMode, int iteration, int accuracy) {
        if (isEmpty(subResult.getHighestResult())) {
            System.out.println("Received empty result from searcher: " + currentSearcher.getClass());
            highest.setResponseCommand(QuickSearchResponseEnum.CONTINUE);
            return highest;
        }
        if (!subResult.getHighestResult().isBothPrivsValidAndNonNull()) {
            throw new IllegalStateException("Privs are not the same at SimpleSearchHelperMixed#search. They should be");
        }

        System.out.println("Current priv (current mode=" + searchMode + ") is " + "[" + (iteration + 1) + "]: " + subResult.getHighestResult().getCommonPriv());

        PubComparisonResultWrapper current = pubComparer.getCurrentResult(subResult.getHighestResult().getCommonPriv(), lockedPKHUncompressed, lockedPKHCompressed, getScaleFactor());
        if (isEmpty(current)) {
            highest.setResponseCommand(QuickSearchResponseEnum.CONTINUE);
            return highest;
        }

        highest.setHighestResult(pubComparer.selectBest(highest.getHighestResult(), current));
        // highest = selectBetterResult(highest, current);

        if (dynamicAccuracy) {
            if (isMoreThanRequestedAccuracy(highest.getHighestResult(), accuracy)) {
                highest.setResponseCommand(QuickSearchResponseEnum.BREAK);
            } else {
                highest.setResponseCommand(QuickSearchResponseEnum.IGNORE);
            }
            return highest;
        }

        if (isMoreOrEqualToRequestedAccuracy(highest.getHighestResult(), accuracy)) {
            highest.setResponseCommand(QuickSearchResponseEnum.BREAK);
        } else {
            highest.setResponseCommand(QuickSearchResponseEnum.IGNORE);
        }

        return highest;
    }

    protected final QuickSearchResponseEnum checkAndEvaluateFuzzing(String currentPrivKey, PubComparisonResultWrapper highest, String lockedPKHUncompressed, String lockedPKHCompressed, int accuracy) {
        if (!isValidKey(currentPrivKey)) {
            return QuickSearchResponseEnum.CONTINUE;
        }

        PubComparisonResultWrapper current = calculateCurrentResult(currentPrivKey, lockedPKHUncompressed, lockedPKHCompressed, getScaleFactor());
        if (isEmpty(current)) {
            return QuickSearchResponseEnum.CONTINUE;
        }

        highest = selectBetterResult(highest, current);

        return isMoreOrEqualToRequestedAccuracy(highest, accuracy) ? QuickSearchResponseEnum.IGNORE : QuickSearchResponseEnum.CONTINUE;
    }

    private PubComparisonResultWrapper selectBetterResult(PubComparisonResultWrapper highest, PubComparisonResultWrapper current) {
        return pubComparer.selectBest(highest, current);
    }

    protected BiFunction<QuickSearchResponseModel, String,QuickSearchResponseModel> createGeneralEvaluationFunction(QuickSearchContext quickSearchContext) {
        if (SearchModeEnum.MIXED == quickSearchContext.getSearchMode() || SearchModeEnum.FUZZING == quickSearchContext.getSearchMode()) {
            throw new IllegalArgumentException("This method cannot be used to create evaluation function for this type [type=" + quickSearchContext.getSearchMode());
        }

        int requestedAccuracy = quickSearchContext.getAccuracy();

        if (QuickSearchComparisonType.COLLISION == quickSearchContext.getType()) {
            String targetPriv = quickSearchContext.getTargetPriv();
            String targetPKHUncompressed = helper.getPubKeyHashUncompressed(targetPriv, false);
            String targetPKHCompressed = helper.getPubKeyHashCompressed(targetPriv, false);

            return (model, currentKey) -> compareAndUpdateModel(model, currentKey, targetPKHUncompressed, targetPKHCompressed, requestedAccuracy);
        } else if (QuickSearchComparisonType.BLIND == quickSearchContext.getType()) {
            String targetPub = quickSearchContext.getTargetPub();

            return (model, currentKey) -> compareAndUpdateModel(model, currentKey, targetPub, targetPub, requestedAccuracy);
        }

        throw new IllegalStateException("General evaluation function could not be created for comparison type: " + quickSearchContext.getType());
    }

    protected PubComparisonResultWrapper iterateForIncDecMode(@NonNull QuickSearchContext quickSearchContext) {
        Optional<String> error = quickSearchContext.validate();
        if (error.isPresent()) {
            throw new IllegalStateException(error.get());
        }

        return iterateGeneral(quickSearchContext);
    }

    protected PubComparisonResultWrapper iterateForRandomMode(@NonNull QuickSearchContext quickSearchContext) {
        Optional<String> error = quickSearchContext.validate();
        if (error.isPresent()) {
            throw new IllegalStateException(error.get());
        }

        return iterateGeneral(quickSearchContext);
    }

    protected PubComparisonResultWrapper iterateForVRotationMode(@NonNull QuickSearchContext quickSearchContext) {
        Optional<String> error = quickSearchContext.validate();
        if (error.isPresent()) {
            throw new IllegalStateException(error.get());
        }

        return iterateForVertical(quickSearchContext);
    }

    protected PubComparisonResultWrapper iterateForHRotationMode(@NonNull QuickSearchContext quickSearchContext) {
        Optional<String> error = quickSearchContext.validate();
        if (error.isPresent()) {
            throw new IllegalStateException(error.get());
        }

        return iterateForHorizontal(quickSearchContext);
    }
}
