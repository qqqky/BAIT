package com.bearlycattable.bait.advanced.searchHelper.impl;

import java.util.List;
import java.util.logging.Logger;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.advancedCommons.contexts.AdvancedSearchHelperCreationContext;
import com.bearlycattable.bait.advancedCommons.wrappers.AdvancedSearchTaskWrapper;
import com.bearlycattable.bait.advancedCommons.contexts.AdvancedSearchContext;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.interfaces.PrefixedKeyGenerator;
import com.bearlycattable.bait.utility.RandomAddressGenerator;

public final class AdvancedSearchHelperRandomSameWord extends AdvancedSearchHelperRandom implements PrefixedKeyGenerator {

    private final RandomAddressGenerator generator = RandomAddressGenerator.getSecureGenerator(64);
    private static final Logger LOG = Logger.getLogger(AdvancedSearchHelperRandomSameWord.class.getName());
    private static final SearchModeEnum searchMode = SearchModeEnum.RANDOM_SAME_WORD;

    public AdvancedSearchHelperRandomSameWord(@NonNull AdvancedSearchHelperCreationContext creationContext) {
        super(creationContext);
    }

    @Override
    public AdvancedSearchTaskWrapper createNewAdvancedSearchTask(AdvancedSearchContext advancedSearchContext) {
        return advancedSearchTaskGuiCreationHelper(advancedSearchContext);
    }

    @Override
    public String buildNextPrivPrefixed(String current, List<Integer> disabledWords, String prefix) {
        return generator.generateRandomSameWordWithBlacklist(prefix, current, disabledWords);
    }

    @Override
    public boolean isByteComparisonSupported() {
        return false;
    }

    @Override
    public SearchModeEnum getSearchMode() {
        return searchMode;
    }


}
