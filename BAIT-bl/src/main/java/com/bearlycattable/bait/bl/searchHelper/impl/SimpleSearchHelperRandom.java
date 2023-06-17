package com.bearlycattable.bait.bl.searchHelper.impl;

import java.util.List;
import java.util.logging.Logger;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.bl.searchHelper.AbstractSimpleSearchHelper;
import com.bearlycattable.bait.bl.searchHelper.factory.QuickSearchTaskFactory;
import com.bearlycattable.bait.commons.contexts.QuickSearchContext;
import com.bearlycattable.bait.commons.contexts.SimpleSearchHelperCreationContext;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.interfaces.CustomKeyGenerator;
import com.bearlycattable.bait.commons.wrappers.PubComparisonResultWrapper;
import com.bearlycattable.bait.utility.RandomAddressGenerator;

public class SimpleSearchHelperRandom extends AbstractSimpleSearchHelper implements CustomKeyGenerator {

    private static final Logger LOG = Logger.getLogger(SimpleSearchHelperRandom.class.getName());
    private static final SearchModeEnum searchMode = SearchModeEnum.RANDOM;

    private final RandomAddressGenerator generator = RandomAddressGenerator.getSecureGenerator(64);

    public SimpleSearchHelperRandom(@NonNull SimpleSearchHelperCreationContext creationContext) {
        super(creationContext);
    }

    @NonNull
    @Override
    public PubComparisonResultWrapper quickSearchSameThread(QuickSearchContext quickSearchContext) {
        List<Integer> disabledWords = quickSearchContext.getDisabledWords();
        quickSearchContext.setNextPrivFunction(seed -> buildNextPriv(seed, disabledWords));
        return iterateForRandomMode(quickSearchContext);
    }

    @Override
    public QuickSearchTaskWrapper createNewQuickSearchTask(QuickSearchContext quickSearchContext) {
        List<Integer> disabledWords = quickSearchContext.getDisabledWords();
        quickSearchContext.setNextPrivFunction(seed -> buildNextPriv(seed, disabledWords));
        quickSearchContext.setEvaluationFunction(super.createGeneralEvaluationFunction(quickSearchContext));
        return QuickSearchTaskFactory.createNewQuickSearchTask(quickSearchContext);
    }

    @Override
    public String buildNextPriv(String current, List<Integer> disabledWords) {
        if (disabledWords == null || disabledWords.isEmpty()) {
            return generator.generateValidKey();
        }
        return generator.generateWithBlacklist(current, disabledWords);
    }

    @Override
    public SearchModeEnum getSearchMode() {
        return searchMode;
    }
}
