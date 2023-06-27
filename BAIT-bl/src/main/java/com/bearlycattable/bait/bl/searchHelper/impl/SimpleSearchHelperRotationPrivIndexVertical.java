package com.bearlycattable.bait.bl.searchHelper.impl;

import java.util.List;
import java.util.logging.Logger;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.bl.searchHelper.AbstractSimpleSearchHelper;
import com.bearlycattable.bait.bl.searchHelper.factory.QuickSearchTaskFactory;
import com.bearlycattable.bait.commons.contexts.QuickSearchContext;
import com.bearlycattable.bait.commons.contexts.SimpleSearchHelperCreationContext;
import com.bearlycattable.bait.commons.enums.OutputCaseEnum;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.interfaces.IndexRotatorVertical;
import com.bearlycattable.bait.commons.wrappers.PubComparisonResultWrapper;
import com.bearlycattable.bait.commons.validators.SearchHelperIterationsValidator;
import com.bearlycattable.bait.commons.wrappers.QuickSearchTaskWrapper;
import com.bearlycattable.bait.utility.AddressModifier;

/**
    Keeps iterating until no better result can be found by vertical rotation only.
    Number of iterations is ignored for this particular implementation of SimpleSearchHelper
     */
public final class SimpleSearchHelperRotationPrivIndexVertical extends AbstractSimpleSearchHelper implements IndexRotatorVertical {

    private static final Logger LOG = Logger.getLogger(SimpleSearchHelperRotationPrivIndexVertical.class.getName());
    private static final SearchModeEnum searchMode = SearchModeEnum.ROTATION_PRIV_INDEX_VERTICAL;

    private final AddressModifier modifier = new AddressModifier(OutputCaseEnum.UPPERCASE);

    public SimpleSearchHelperRotationPrivIndexVertical(@NonNull SimpleSearchHelperCreationContext creationContext) {
        super(creationContext);
    }

    @NonNull
    @Override
    public PubComparisonResultWrapper quickSearchSameThread(QuickSearchContext quickSearchContext) {
        List<Integer> disabledWords = quickSearchContext.getDisabledWords();
        quickSearchContext.setNextPrivFunctionVertical((key, index) -> rotateAtIndex(key, disabledWords, index));
        quickSearchContext.setValidityCheckFunction((key, index) -> isValidIndexForVerticalRotation(key, disabledWords, index));

        return iterateForVRotationMode(quickSearchContext);
    }

    @Override
    public QuickSearchTaskWrapper createNewQuickSearchTask(QuickSearchContext quickSearchContext) {
        List<Integer> disabledWords = quickSearchContext.getDisabledWords();

        quickSearchContext.setNextPrivFunctionVertical((key, index) -> rotateAtIndex(key, disabledWords, index));
        quickSearchContext.setValidityCheckFunction((key, index) -> isValidIndexForVerticalRotation(key, disabledWords, index));
        quickSearchContext.setEvaluationFunction(super.createGeneralEvaluationFunction(quickSearchContext));
        quickSearchContext.setIterations(SearchHelperIterationsValidator.validateAndGet(searchMode, quickSearchContext.getIterations())); //force-limit the iterations

        return QuickSearchTaskFactory.createNewQuickSearchTask(quickSearchContext);
    }

    @Override
    public SearchModeEnum getSearchMode() {
        return searchMode;
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
}
