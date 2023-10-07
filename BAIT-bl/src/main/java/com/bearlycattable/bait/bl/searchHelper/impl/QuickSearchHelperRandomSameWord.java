package com.bearlycattable.bait.bl.searchHelper.impl;

import java.util.List;
import java.util.logging.Logger;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.commons.contexts.QuickSearchContext;
import com.bearlycattable.bait.commons.contexts.SimpleSearchHelperCreationContext;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.interfaces.PrefixedKeyGenerator;
import com.bearlycattable.bait.commons.wrappers.QuickSearchTaskWrapper;
import com.bearlycattable.bait.utility.RandomAddressGenerator;

public final class QuickSearchHelperRandomSameWord extends QuickSearchHelperRandom implements PrefixedKeyGenerator {

    private final RandomAddressGenerator generator = RandomAddressGenerator.getSecureGenerator(64);
    private static final Logger LOG = Logger.getLogger(QuickSearchHelperRandomSameWord.class.getName());
    private static final SearchModeEnum searchMode = SearchModeEnum.RANDOM_SAME_WORD;

    public QuickSearchHelperRandomSameWord(@NonNull SimpleSearchHelperCreationContext creationContext) {
        super(creationContext);
    }

    @Override
    public QuickSearchTaskWrapper createNewQuickSearchTask(QuickSearchContext quickSearchContext) {
        return quickSearchTaskGuiCreationHelper(quickSearchContext);
    }

    @Override
    public String buildNextPrivPrefixed(String current, List<Integer> disabledWords, String prefix) {
        return generator.generateRandomSameWordWithBlacklist(prefix, current, disabledWords);
    }

    @Override
    public SearchModeEnum getSearchMode() {
        return searchMode;
    }
}
