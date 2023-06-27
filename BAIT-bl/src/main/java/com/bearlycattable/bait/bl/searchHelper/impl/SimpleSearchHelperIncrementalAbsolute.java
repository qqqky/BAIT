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
import com.bearlycattable.bait.commons.interfaces.CustomKeyGenerator;
import com.bearlycattable.bait.commons.wrappers.PubComparisonResultWrapper;
import com.bearlycattable.bait.commons.wrappers.QuickSearchTaskWrapper;
import com.bearlycattable.bait.utility.AddressModifier;

public final class SimpleSearchHelperIncrementalAbsolute extends AbstractSimpleSearchHelper implements CustomKeyGenerator {

    private static final Logger LOG = Logger.getLogger(SimpleSearchHelperIncrementalAbsolute.class.getName());
    private static final SearchModeEnum searchMode = SearchModeEnum.INCREMENTAL_ABSOLUTE;

    private final AddressModifier modifier = new AddressModifier(OutputCaseEnum.UPPERCASE);
    private String currentPrivKey;

    public SimpleSearchHelperIncrementalAbsolute(@NonNull SimpleSearchHelperCreationContext creationContext) {
        super(creationContext);
    }

    @NonNull
    @Override
    public PubComparisonResultWrapper quickSearchSameThread(QuickSearchContext quickSearchContext) {
        List<Integer> disabledWords = quickSearchContext.getDisabledWords();
        quickSearchContext.setNextPrivFunction(seed -> buildNextPriv(seed, disabledWords));
        return iterateForIncDecMode(quickSearchContext);
    }

    @Override
    public QuickSearchTaskWrapper createNewQuickSearchTask(QuickSearchContext quickSearchContext) {
        List<Integer> disabledWords = quickSearchContext.getDisabledWords();
        quickSearchContext.setNextPrivFunction(seed -> buildNextPriv(seed, disabledWords));
        quickSearchContext.setEvaluationFunction(super.createGeneralEvaluationFunction(quickSearchContext));

        return QuickSearchTaskFactory.createNewQuickSearchTask(quickSearchContext);
    }

    @NonNull
    @Override
    public String buildNextPriv(String current, List<Integer> disabledWords) {
        return modifier.incrementPrivAbsolute(current, disabledWords);
    }

    @Override
    public SearchModeEnum getSearchMode() {
        return searchMode;
    }
}
