package com.bearlycattable.bait.bl.searchHelper.impl;

import java.util.List;
import java.util.logging.Logger;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.bl.searchHelper.AbstractQuickSearchHelper;
import com.bearlycattable.bait.commons.contexts.QuickSearchContext;
import com.bearlycattable.bait.commons.contexts.SimpleSearchHelperCreationContext;
import com.bearlycattable.bait.commons.enums.OutputCaseEnum;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.interfaces.CustomKeyGenerator;
import com.bearlycattable.bait.commons.wrappers.QuickSearchTaskWrapper;
import com.bearlycattable.bait.utility.addressModifiers.stringModifiers.IncrementModifier;
import com.bearlycattable.bait.utility.addressModifiers.stringModifiers.IncrementModifierImpl;

public final class QuickSearchHelperIncrementalWords extends AbstractQuickSearchHelper implements CustomKeyGenerator {

    private static final Logger LOG = Logger.getLogger(QuickSearchHelperIncrementalWords.class.getName());
    private static final SearchModeEnum searchMode = SearchModeEnum.INCREMENTAL_WORDS;

    private final IncrementModifier modifier = new IncrementModifierImpl(OutputCaseEnum.UPPERCASE);

    public QuickSearchHelperIncrementalWords(@NonNull SimpleSearchHelperCreationContext creationContext) {
        super(creationContext);
    }

    @Override
    public QuickSearchTaskWrapper createNewQuickSearchTask(QuickSearchContext quickSearchContext) {
        return quickSearchTaskGuiCreationHelper(quickSearchContext);
    }

    @Override
    public String buildNextPriv(String current, List<Integer> disabledWords) {
        return modifier.incrementAllWords(current, disabledWords);
    }

    @Override
    public SearchModeEnum getSearchMode() {
        return searchMode;
    }
}
