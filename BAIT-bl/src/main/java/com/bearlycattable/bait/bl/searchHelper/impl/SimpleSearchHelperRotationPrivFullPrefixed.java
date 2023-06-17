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
import com.bearlycattable.bait.utility.AddressModifier;

public final class SimpleSearchHelperRotationPrivFullPrefixed extends AbstractSimpleSearchHelper implements IndexRotatorHorizontal {

    private static final Logger LOG = Logger.getLogger(SimpleSearchHelperRotationPrivFullPrefixed.class.getName());
    private static final SearchModeEnum searchMode = SearchModeEnum.ROTATION_PRIV_FULL_PREFIXED;

    private final AddressModifier modifier = new AddressModifier(OutputCaseEnum.UPPERCASE);

    public SimpleSearchHelperRotationPrivFullPrefixed(@NonNull SimpleSearchHelperCreationContext creationContext) {
        super(creationContext);
    }

    @NonNull
    @Override
    public PubComparisonResultWrapper quickSearchSameThread(QuickSearchContext quickSearchContext) {
        quickSearchContext.setNextPrivFunctionFullPrefixed(this::rotateLeftBy);
        return iterateForHRotationMode(quickSearchContext);
    }

    @Override
    public QuickSearchTaskWrapper createNewQuickSearchTask(QuickSearchContext quickSearchContext) {
        quickSearchContext.setEvaluationFunction(super.createGeneralEvaluationFunction(quickSearchContext));
        quickSearchContext.setNextPrivFunctionFullPrefixed(this::rotateLeftBy);
        return QuickSearchTaskFactory.createNewQuickSearchTask(quickSearchContext);
    }

    @Override
    public SearchModeEnum getSearchMode() {
        return searchMode;
    }

    @NonNull
    @Override
    public String rotateLeft(String current) {
        return modifier.rotateAddressLeftBy(current, 1, true);
    }

    @NonNull
    @Override
    public String rotateLeftBy(String current, int rotateBy) {
        return modifier.rotateAddressLeftBy(current, rotateBy, true);
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
}
