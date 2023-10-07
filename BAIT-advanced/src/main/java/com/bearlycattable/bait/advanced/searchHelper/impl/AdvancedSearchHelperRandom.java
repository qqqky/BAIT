package com.bearlycattable.bait.advanced.searchHelper.impl;

import java.util.List;
import java.util.logging.Logger;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.advancedCommons.contexts.AdvancedSearchHelperCreationContext;
import com.bearlycattable.bait.advanced.searchHelper.AbstractAdvancedSearchHelper;
import com.bearlycattable.bait.advancedCommons.wrappers.AdvancedSearchTaskWrapper;
import com.bearlycattable.bait.advancedCommons.contexts.AdvancedSearchContext;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.interfaces.CustomKeyGenerator;
import com.bearlycattable.bait.utility.RandomAddressGenerator;

public class AdvancedSearchHelperRandom extends AbstractAdvancedSearchHelper implements CustomKeyGenerator {

    private static final Logger LOG = Logger.getLogger(AdvancedSearchHelperRandom.class.getName());
    private static final SearchModeEnum searchMode = SearchModeEnum.RANDOM;

    private final RandomAddressGenerator generator = RandomAddressGenerator.getSecureGenerator(64);

    public AdvancedSearchHelperRandom(@NonNull AdvancedSearchHelperCreationContext creationContext) {
        super(creationContext);
    }

    @Override
    public AdvancedSearchTaskWrapper createNewAdvancedSearchTask(AdvancedSearchContext advancedSearchContext) {
        return advancedSearchTaskGuiCreationHelper(advancedSearchContext);
    }

    @Override
    public String buildNextPriv(String current, List<Integer> disabledWords) {
        if (disabledWords == null || disabledWords.isEmpty()) {
            return generator.generateValidKeyString();
        }
        return generator.generateWithBlacklist(current, disabledWords);
    }

    @Override
    public byte[] buildNextPrivBytes(byte[] current, List<Integer> disabledWords) {
        if (disabledWords == null || disabledWords.isEmpty()) {
            return generator.generateValidKeyBytes();
        }

        return generator.generateWithBlacklist(current, disabledWords);
    }

    @Override
    public SearchModeEnum getSearchMode() {
        return searchMode;
    }

    @Override
    public boolean isByteComparisonSupported() {
        return true;
    }
}
