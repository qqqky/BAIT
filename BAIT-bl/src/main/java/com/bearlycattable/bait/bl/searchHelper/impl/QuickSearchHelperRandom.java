package com.bearlycattable.bait.bl.searchHelper.impl;

import java.util.List;
import java.util.logging.Logger;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.bl.searchHelper.AbstractQuickSearchHelper;
import com.bearlycattable.bait.commons.contexts.QuickSearchContext;
import com.bearlycattable.bait.commons.contexts.SimpleSearchHelperCreationContext;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.interfaces.CustomKeyGenerator;
import com.bearlycattable.bait.commons.wrappers.QuickSearchTaskWrapper;
import com.bearlycattable.bait.utility.RandomAddressGenerator;

public class QuickSearchHelperRandom extends AbstractQuickSearchHelper implements CustomKeyGenerator {

    private static final Logger LOG = Logger.getLogger(QuickSearchHelperRandom.class.getName());
    private static final SearchModeEnum searchMode = SearchModeEnum.RANDOM;

    private final RandomAddressGenerator generator = RandomAddressGenerator.getSecureGenerator(64);

    public QuickSearchHelperRandom(@NonNull SimpleSearchHelperCreationContext creationContext) {
        super(creationContext);
    }

    @Override
    public QuickSearchTaskWrapper createNewQuickSearchTask(QuickSearchContext quickSearchContext) {
        return quickSearchTaskGuiCreationHelper(quickSearchContext);
    }

    @Override
    public String buildNextPriv(String current, List<Integer> disabledWords) {
        if (disabledWords == null || disabledWords.isEmpty()) {
            return generator.generateValidKeyString();
        }
        return generator.generateWithBlacklist(current, disabledWords);
    }

    @Override
    public SearchModeEnum getSearchMode() {
        return searchMode;
    }
}
