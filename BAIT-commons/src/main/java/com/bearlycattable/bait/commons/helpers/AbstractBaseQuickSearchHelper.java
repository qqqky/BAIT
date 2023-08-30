package com.bearlycattable.bait.commons.helpers;

import java.util.Objects;
import java.util.function.BiFunction;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.commons.contexts.AbstractSearchHelperCreationContext;
import com.bearlycattable.bait.commons.contexts.QuickSearchContext;
import com.bearlycattable.bait.commons.contexts.QuickSearchResponseModel;
import com.bearlycattable.bait.commons.enums.QuickSearchComparisonType;
import com.bearlycattable.bait.commons.enums.QuickSearchResponseEnum;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.other.PubComparer;
import com.bearlycattable.bait.commons.wrappers.PubComparisonResultWrapper;

public abstract class AbstractBaseQuickSearchHelper extends AbstractGeneralSearchHelper {
    private boolean dynamicAccuracy;
    private final HeatVisualizerHelper helper = new HeatVisualizerHelper();
    private final PubComparer pubComparer = new PubComparer();

    protected AbstractBaseQuickSearchHelper() {
        throw new UnsupportedOperationException("Creation of AbstractBaseSearchHelper without context is not allowed");
    }

    protected AbstractBaseQuickSearchHelper(@NonNull AbstractSearchHelperCreationContext creationContext) {
        super(creationContext);
    }

    protected BiFunction<QuickSearchResponseModel, String, QuickSearchResponseModel> createGeneralEvaluationFunction(QuickSearchContext quickSearchContext) {
        SearchModeEnum searchMode = Objects.requireNonNull(quickSearchContext.getSearchMode());
        if (SearchModeEnum.MIXED == searchMode || SearchModeEnum.FUZZING == searchMode) {
            throw new IllegalArgumentException("This method cannot be used to create evaluation function for this type [type=" + quickSearchContext.getSearchMode());
        }

        int requestedAccuracy = quickSearchContext.getAccuracy();

        if (QuickSearchComparisonType.COLLISION == quickSearchContext.getType()) {
            String targetPriv = quickSearchContext.getTargetPriv();
            if (!isValidKey(targetPriv)) {
                throw new IllegalArgumentException("Target key must be valid for QuickSearch!");
            }
            String targetPKHUncompressed = helper.getPubKeyHashUncompressed(targetPriv, false);
            String targetPKHCompressed = helper.getPubKeyHashCompressed(targetPriv, false);

            return (model, currentKey) -> compareAndUpdateModel(searchMode, model, currentKey, targetPKHUncompressed, targetPKHCompressed, requestedAccuracy);
        } else if (QuickSearchComparisonType.BLIND == quickSearchContext.getType()) {
            String targetPub = quickSearchContext.getTargetPub();

            return (model, currentKey) -> compareAndUpdateModel(searchMode, model, currentKey, targetPub, targetPub, requestedAccuracy);
        }

        throw new IllegalStateException("General evaluation function could not be created for comparison type: " + quickSearchContext.getType());
    }

    private QuickSearchResponseModel compareAndUpdateModel(SearchModeEnum searchMode, @NonNull QuickSearchResponseModel model, String currentPrivKey, String targetPKHUncompressed, String targetPKHCompressed, int requestedAccuracy) {
        if (!isValidKey(currentPrivKey)) {
            model.setResponseCommand(QuickSearchResponseEnum.CONTINUE);
            return model;
        }

        PubComparisonResultWrapper current = calculateCurrentResult(currentPrivKey, targetPKHUncompressed, targetPKHCompressed, getScaleFactor());
        if (isEmpty(current)) {
            model.setResponseCommand(QuickSearchResponseEnum.CONTINUE);
            return model;
        }

        switch (searchMode) {
            case RANDOM:
            case RANDOM_SAME_WORD:
            case RANDOM_PREFIXED_WORD:
            case DECREMENTAL_WORDS:
            case INCREMENTAL_WORDS:
            case DECREMENTAL_ABSOLUTE:
            case INCREMENTAL_ABSOLUTE:
            case ROTATION_PRIV_WORDS:
            case ROTATION_PRIV_FULL_NORMAL:
            case ROTATION_PRIV_FULL_PREFIXED:
            case ROTATION_PRIV_INDEX_VERTICAL:
                compareAndUpdateModelGeneral(current, model, requestedAccuracy);
                break;
            case FUZZING:
                compareAndUpdateModelFuzzing(current, model, requestedAccuracy);
                break;
            case MIXED:
                compareAndUpdateModelMixed(current, model, requestedAccuracy);
                break;
            default:
                throw new IllegalArgumentException("Search mode not supported at " + this.getClass().getName() + "@compareAndUpdateModel() [mode=" + searchMode + "]");
        }

        return model;
    }

    private void compareAndUpdateModelGeneral(PubComparisonResultWrapper current, QuickSearchResponseModel model, int requestedAccuracy) {
        model.setHighestResult(pubComparer.selectBest(model.getHighestResult(), current));

        if (isMoreOrEqualToRequestedAccuracy(model.getHighestResult(), requestedAccuracy)) {
            model.setResponseCommand(QuickSearchResponseEnum.BREAK);
        } else {
            model.setResponseCommand(QuickSearchResponseEnum.IGNORE);
        }
    }

    private void compareAndUpdateModelFuzzing(PubComparisonResultWrapper current, QuickSearchResponseModel model, int requestedAccuracy) {
        model.setHighestResult(pubComparer.selectBest(model.getHighestResult(), current));

        if (isMoreOrEqualToRequestedAccuracy(model.getHighestResult(), requestedAccuracy)) {
            model.setResponseCommand(QuickSearchResponseEnum.IGNORE);
        } else {
            model.setResponseCommand(QuickSearchResponseEnum.CONTINUE);
        }
    }

    private void compareAndUpdateModelMixed(PubComparisonResultWrapper current, QuickSearchResponseModel model, int requestedAccuracy) {
        //TODO: implement comparison model for MIXED search mode

        // if (isEmpty(subResult.getHighestResult())) {
        //     System.out.println("Received empty result from searcher: " + currentSearcher.getClass());
        //     highest.setResponseCommand(QuickSearchResponseEnum.CONTINUE);
        //     return highest;
        // }
        // if (!subResult.getHighestResult().isBothPrivsValidAndNonNull()) {
        //     throw new IllegalStateException("Privs are not the same at SimpleSearchHelperMixed#search. They should be");
        // }
        //
        // System.out.println("Current priv (current mode=" + searchMode + ") is " + "[" + (iteration + 1) + "]: " + subResult.getHighestResult().getCommonPriv());
        //
        // PubComparisonResultWrapper current = pubComparer.getCurrentResult(subResult.getHighestResult().getCommonPriv(), lockedPKHUncompressed, lockedPKHCompressed, getScaleFactor());
        // if (isEmpty(current)) {
        //     highest.setResponseCommand(QuickSearchResponseEnum.CONTINUE);
        //     return highest;
        // }
        //
        // highest.setHighestResult(pubComparer.selectBest(highest.getHighestResult(), current));
        // // highest = selectBetterResult(highest, current);
        //
        // if (dynamicAccuracy) {
        //     if (isMoreThanRequestedAccuracy(highest.getHighestResult(), accuracy)) {
        //         highest.setResponseCommand(QuickSearchResponseEnum.BREAK);
        //     } else {
        //         highest.setResponseCommand(QuickSearchResponseEnum.IGNORE);
        //     }
        //     return highest;
        // }
        //
        // if (isMoreOrEqualToRequestedAccuracy(highest.getHighestResult(), accuracy)) {
        //     highest.setResponseCommand(QuickSearchResponseEnum.BREAK);
        // } else {
        //     highest.setResponseCommand(QuickSearchResponseEnum.IGNORE);
        // }
        //
        // return highest;
    }

    protected boolean isEmpty(PubComparisonResultWrapper wrapper) {
        return wrapper == null || wrapper.equalsEmpty();
    }

    protected void setDynamicAccuracy(boolean isDynamic) {
        this.dynamicAccuracy = isDynamic;
    }

    protected boolean isMoreOrEqualToRequestedAccuracy(PubComparisonResultWrapper highestResult, int requestedAccuracy) {
        return highestResult.resultStream().anyMatch(holder -> getSimilarityMappings().get(helper.recalculateIndexForSimilarityMappings(holder.getHighest(), getScaleFactor())).intValue() >= requestedAccuracy);
    }

    protected boolean isMoreThanRequestedAccuracy(PubComparisonResultWrapper highestResult, int requestedAccuracy) {
        return highestResult.resultStream().anyMatch(holder -> getSimilarityMappings().get(helper.recalculateIndexForSimilarityMappings(holder.getHighest(), getScaleFactor())).intValue() > requestedAccuracy);
    }
}
