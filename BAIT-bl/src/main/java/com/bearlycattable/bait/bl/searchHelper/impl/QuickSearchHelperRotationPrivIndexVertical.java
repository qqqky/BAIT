package com.bearlycattable.bait.bl.searchHelper.impl;

import java.util.List;
import java.util.logging.Logger;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.bl.searchHelper.AbstractQuickSearchHelper;
import com.bearlycattable.bait.commons.contexts.QuickSearchContext;
import com.bearlycattable.bait.commons.contexts.SimpleSearchHelperCreationContext;
import com.bearlycattable.bait.commons.enums.OutputCaseEnum;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.interfaces.IndexRotatorVertical;
import com.bearlycattable.bait.commons.wrappers.QuickSearchTaskWrapper;
import com.bearlycattable.bait.utility.AddressModifier;

/**
    Keeps iterating until no better result can be found by vertical rotation only.
    Number of iterations is ignored for this particular implementation of SimpleSearchHelper
     */
public final class QuickSearchHelperRotationPrivIndexVertical extends AbstractQuickSearchHelper implements IndexRotatorVertical {

    private static final Logger LOG = Logger.getLogger(QuickSearchHelperRotationPrivIndexVertical.class.getName());
    private static final SearchModeEnum searchMode = SearchModeEnum.ROTATION_PRIV_INDEX_VERTICAL;

    private final AddressModifier modifier = new AddressModifier(OutputCaseEnum.UPPERCASE);

    public QuickSearchHelperRotationPrivIndexVertical(@NonNull SimpleSearchHelperCreationContext creationContext) {
        super(creationContext);
    }

    // @NonNull
    // @Override
    // public PubComparisonResultWrapper quickSearchSameThread(QuickSearchContext quickSearchContext) {
    //     // List<Integer> disabledWords = quickSearchContext.getDisabledWords();
    //     // quickSearchContext.setNextPrivFunctionVertical((key, index) -> rotateAtIndex(key, disabledWords, index));
    //     // quickSearchContext.setValidityCheckFunction((key, index) -> isValidIndexForVerticalRotation(key, disabledWords, index));
    //
    //     return iterateForVRotationMode(quickSearchContext);
    // }

    @Override
    public QuickSearchTaskWrapper createNewQuickSearchTask(QuickSearchContext quickSearchContext) {
        // List<Integer> disabledWords = quickSearchContext.getDisabledWords();
        //
        // quickSearchContext.setNextPrivFunctionVertical((key, index) -> rotateAtIndex(key, disabledWords, index));
        // quickSearchContext.setValidityCheckFunction((key, index) -> isValidIndexForVerticalRotation(key, disabledWords, index));
        // quickSearchContext.setEvaluationFunction(super.createGeneralEvaluationFunction(quickSearchContext));
        // quickSearchContext.setIterations(SearchHelperIterationsValidator.validateAndGet(searchMode, quickSearchContext.getIterations())); //force-limit the iterations
        return quickSearchTaskGuiCreationHelper(quickSearchContext);
        // return QuickSearchTaskFactory.createNewQuickSearchTask(quickSearchContext);
    }

    @NonNull
    @Override
    public String rotateAtIndex(String current, List<Integer> disabledWords, int index) {
        return modifier.rotateSelectedIndexVertically(current, disabledWords, index);
    }

    @Override
    public boolean isValidIndexForVerticalRotation(String address, List<Integer> disabledWords, int selectedIndex) {
        return modifier.isValidIndexForVerticalRotation(address, disabledWords, selectedIndex);
    }

    @Override
    public SearchModeEnum getSearchMode() {
        return searchMode;
    }

    // /*------ unsupported methods from base class: ------- */
    //
    // @Override
    // public @NonNull String rotateLeft(String current) {
    //     throw new UnsupportedOperationException("Not supported " + this.getClass().getName() + "@rotateLeft(String)");
    // }
    //
    // @Override
    // public @NonNull String rotateLeftBy(String current, int rotateBy) {
    //     throw new UnsupportedOperationException("Not supported " + this.getClass().getName() + "@rotateLeftBy(String, int)");
    // }
    //
    // @Override
    // public @NonNull String rotateLeft(String current, List<Integer> disabledWords) {
    //     throw new UnsupportedOperationException("Not supported " + this.getClass().getName() + "@rotateLeft(String, List<Integer>)");
    // }
    //
    // @Override
    // public @NonNull String rotateLeftBy(String current, int rotateBy, List<Integer> disabledWords) {
    //     throw new UnsupportedOperationException("Not supported " + this.getClass().getName() + "@rotateLeftBy(String, int, List<Integer>)");
    // }
    //
    // @Override
    // public @NonNull String buildNextPriv(String current, List<Integer> disabledWords) {
    //     throw new UnsupportedOperationException("Not supported " + this.getClass().getName() + "@buildNextPriv(String, List<Integer>)");
    // }
    //
    // @Override
    // public String buildNextPriv(String current, List<Integer> disabledWords, String prefix) {
    //     throw new UnsupportedOperationException("Not supported " + this.getClass().getName() + "@buildNextPriv(String, List<Integer>, String)");
    // }
}
