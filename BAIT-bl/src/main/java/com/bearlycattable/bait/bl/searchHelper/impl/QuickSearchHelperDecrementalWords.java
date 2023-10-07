package com.bearlycattable.bait.bl.searchHelper.impl;

import java.util.List;
import java.util.logging.Logger;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.bl.searchHelper.AbstractQuickSearchHelper;
import com.bearlycattable.bait.commons.contexts.QuickSearchContext;
import com.bearlycattable.bait.commons.contexts.SimpleSearchHelperCreationContext;
import com.bearlycattable.bait.commons.enums.OutputCaseEnum;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.wrappers.QuickSearchTaskWrapper;
import com.bearlycattable.bait.utility.addressModifiers.stringModifiers.DecrementModifier;
import com.bearlycattable.bait.utility.addressModifiers.stringModifiers.DecrementModifierImpl;

public final class QuickSearchHelperDecrementalWords extends AbstractQuickSearchHelper {

    private static final Logger LOG = Logger.getLogger(QuickSearchHelperDecrementalWords.class.getName());
    private static final SearchModeEnum searchMode = SearchModeEnum.DECREMENTAL_WORDS;

    private final DecrementModifier modifier = new DecrementModifierImpl(OutputCaseEnum.UPPERCASE);

    public QuickSearchHelperDecrementalWords(@NonNull SimpleSearchHelperCreationContext creationContext) {
        super(creationContext);
    }

    @Override
    public QuickSearchTaskWrapper createNewQuickSearchTask(QuickSearchContext quickSearchContext) {
        return quickSearchTaskGuiCreationHelper(quickSearchContext);
    }

    @NonNull
    @Override
    public String buildNextPriv(String current, List<Integer> disabledWords) {
        return modifier.decrementAllWords(current, disabledWords);
    }

    @Override
    public SearchModeEnum getSearchMode() {
        return searchMode;
    }
}
