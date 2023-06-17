package com.bearlycattable.bait.advanced.searchHelper.impl;

import java.util.List;
import java.util.logging.Logger;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.advanced.context.AdvancedSearchHelperCreationContext;
import com.bearlycattable.bait.advanced.searchHelper.AbstractAdvancedSearchHelper;
import com.bearlycattable.bait.advancedCommons.interfaces.PredictableEnd;
import com.bearlycattable.bait.advancedCommons.wrappers.AdvancedSearchTaskWrapper;
import com.bearlycattable.bait.advancedCommons.contexts.AdvancedSearchContext;
import com.bearlycattable.bait.commons.enums.OutputCaseEnum;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.interfaces.CustomKeyGenerator;
import com.bearlycattable.bait.utility.AddressModifier;

public final class AdvancedSearchHelperIncrementalWords extends AbstractAdvancedSearchHelper implements CustomKeyGenerator, PredictableEnd {

    private static final Logger LOG = Logger.getLogger(AdvancedSearchHelperIncrementalWords.class.getName());
    private static final SearchModeEnum searchMode = SearchModeEnum.INCREMENTAL_WORDS;

    private final AddressModifier modifier = new AddressModifier(OutputCaseEnum.UPPERCASE);

    public AdvancedSearchHelperIncrementalWords(@NonNull AdvancedSearchHelperCreationContext creationContext) {
        super(creationContext);
    }

    @Override
    public AdvancedSearchTaskWrapper createNewAdvancedSearchTask(AdvancedSearchContext advancedSearchContext) {
        advancedSearchContext.setNextPrivFunction((input) -> buildNextPriv(input, advancedSearchContext.getDisabledWords()));
        return advancedSearchTaskGuiCreationHelper(advancedSearchContext);
    }

    @Override
    public String buildNextPriv(String current, List<Integer> disabledWords) {
        return modifier.incrementAllWords(current, disabledWords);
    }

    @Override
    public SearchModeEnum getSearchMode() {
        return searchMode;
    }

    @Override
    public @NonNull String predictLastItem(@NonNull String seed, int iterations, List<Integer> disabledWords) {
        return modifier.incrementWordsBy(seed, iterations, disabledWords);
    }
}
