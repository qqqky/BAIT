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
import com.bearlycattable.bait.commons.interfaces.IndexRotatorHorizontal;
import com.bearlycattable.bait.commons.wrappers.PubComparisonResultWrapper;
import com.bearlycattable.bait.commons.validators.SearchHelperIterationsValidator;
import com.bearlycattable.bait.utility.AddressModifier;

public final class SimpleSearchHelperRotationPrivFullNormal extends AbstractSimpleSearchHelper implements IndexRotatorHorizontal {

    private static final Logger LOG = Logger.getLogger(SimpleSearchHelperRotationPrivFullNormal.class.getName());
    private static final SearchModeEnum searchMode = SearchModeEnum.ROTATION_PRIV_FULL_NORMAL;

    private final AddressModifier modifier = new AddressModifier(OutputCaseEnum.UPPERCASE);

    public SimpleSearchHelperRotationPrivFullNormal(@NonNull SimpleSearchHelperCreationContext creationContext) {
        super(creationContext);
    }

    @NonNull
    @Override
    public PubComparisonResultWrapper quickSearchSameThread(QuickSearchContext quickSearchContext) {
        quickSearchContext.setNextPrivFunction(this::rotateLeft);
        return iterateForHRotationMode(quickSearchContext);
    }

    @Override
    public QuickSearchTaskWrapper createNewQuickSearchTask(QuickSearchContext quickSearchContext) {
        quickSearchContext.setEvaluationFunction(super.createGeneralEvaluationFunction(quickSearchContext));
        quickSearchContext.setNextPrivFunction((this::rotateLeft));
        quickSearchContext.setIterations(SearchHelperIterationsValidator.validateAndGet(quickSearchContext.getSearchMode(), quickSearchContext.getIterations()));
        return QuickSearchTaskFactory.createNewQuickSearchTask(quickSearchContext);
    }

    @NonNull
    @Override
    public String rotateLeft(String current) {
        return modifier.rotateAddressLeftBy(current, 1, false);
    }

    @NonNull
    @Override
    public String rotateLeftBy(String current, int rotateBy) {
        return modifier.rotateAddressLeftBy(current, rotateBy, false);
    }

    @NonNull
    @Override
    public String rotateLeft(String current, List<Integer> disabledWords) {
        return rotateLeft(current); //ignore disabled words
    }

    @NonNull
    @Override
    public String rotateLeftBy(String current, int rotateBy, List<Integer> disabledWords) {
        return rotateLeftBy(current, rotateBy); //ignore disabled words
    }

    @Override
    public SearchModeEnum getSearchMode() {
        return searchMode;
    }
}
