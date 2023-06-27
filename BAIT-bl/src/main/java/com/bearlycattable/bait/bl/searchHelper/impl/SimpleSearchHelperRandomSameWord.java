package com.bearlycattable.bait.bl.searchHelper.impl;

import java.util.List;
import java.util.logging.Logger;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.bl.searchHelper.factory.QuickSearchTaskFactory;
import com.bearlycattable.bait.commons.contexts.QuickSearchContext;
import com.bearlycattable.bait.commons.contexts.SimpleSearchHelperCreationContext;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.interfaces.PrefixedKeyGenerator;
import com.bearlycattable.bait.commons.wrappers.PubComparisonResultWrapper;
import com.bearlycattable.bait.commons.wrappers.QuickSearchTaskWrapper;
import com.bearlycattable.bait.utility.RandomAddressGenerator;

public final class SimpleSearchHelperRandomSameWord extends SimpleSearchHelperRandom implements PrefixedKeyGenerator {

    private final RandomAddressGenerator generator = RandomAddressGenerator.getSecureGenerator(64);
    private static final Logger LOG = Logger.getLogger(SimpleSearchHelperRandomSameWord.class.getName());
    private static final SearchModeEnum searchMode = SearchModeEnum.RANDOM_SAME_WORD;

    public SimpleSearchHelperRandomSameWord(@NonNull SimpleSearchHelperCreationContext creationContext) {
        super(creationContext);
    }

    @NonNull
    @Override
    public PubComparisonResultWrapper quickSearchSameThread(QuickSearchContext quickSearchContext) {
        List<Integer> disabledWords = quickSearchContext.getDisabledWords();
        String prefix = quickSearchContext.getWordPrefix();
        quickSearchContext.setNextPrivFunction(key -> buildNextPriv(key, disabledWords, prefix));
        return iterateForRandomMode(quickSearchContext);
    }

    @Override
    public QuickSearchTaskWrapper createNewQuickSearchTask(QuickSearchContext quickSearchContext) {
        List<Integer> disabledWords = quickSearchContext.getDisabledWords();
        String prefix = quickSearchContext.getWordPrefix();
        quickSearchContext.setNextPrivFunction(key -> buildNextPriv(key, disabledWords, prefix));
        quickSearchContext.setEvaluationFunction(super.createGeneralEvaluationFunction(quickSearchContext));
        return QuickSearchTaskFactory.createNewQuickSearchTask(quickSearchContext);
    }

    @Override
    public String buildNextPriv(String current, List<Integer> disabledWords, String prefix) {
        return generator.generateRandomSameWordWithBlacklist(prefix, current, disabledWords);
    }

    @Override
    public SearchModeEnum getSearchMode() {
        return searchMode;
    }
}
