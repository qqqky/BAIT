package com.bearlycattable.bait.bl.searchHelper.impl;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.bl.searchHelper.AbstractQuickSearchHelper;
import com.bearlycattable.bait.commons.contexts.QuickSearchContext;
import com.bearlycattable.bait.commons.contexts.SimpleSearchHelperCreationContext;
import com.bearlycattable.bait.commons.enums.OutputCaseEnum;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.interfaces.IndexRotatorHorizontal;
import com.bearlycattable.bait.commons.wrappers.QuickSearchTaskWrapper;
import com.bearlycattable.bait.utility.addressModifiers.stringModifiers.HRotatorModifier;
import com.bearlycattable.bait.utility.addressModifiers.stringModifiers.HRotatorModifierImpl;

public final class QuickSearchHelperRotationPrivWords extends AbstractQuickSearchHelper implements IndexRotatorHorizontal {

    private static final Logger LOG = Logger.getLogger(QuickSearchHelperRotationPrivWords.class.getName());
    private static final SearchModeEnum searchMode = SearchModeEnum.ROTATION_WORDS;

    private final HRotatorModifier modifier = new HRotatorModifierImpl(OutputCaseEnum.UPPERCASE);

    public QuickSearchHelperRotationPrivWords(@NonNull SimpleSearchHelperCreationContext creationContext) {
        super(creationContext);
    }

    @Override
    public QuickSearchTaskWrapper createNewQuickSearchTask(QuickSearchContext quickSearchContext) {
        return quickSearchTaskGuiCreationHelper(quickSearchContext);
    }

    @NonNull
    @Override
    public String rotateLeft(String current) {
        return rotateLeft(current, Collections.emptyList());
    }

    @NonNull
    @Override
    public String rotateLeftBy(String current, int rotateBy) {
        return rotateLeftBy(current, rotateBy, Collections.emptyList());
    }

    @NonNull
    @Override
    public String rotateLeft(String current, List<Integer> disabledWords) {
        return modifier.rotateAllWordsBy(current, 1, disabledWords);
    }

    @NonNull
    @Override
    public String rotateLeftBy(String current, int rotateBy, List<Integer> disabledWords) {
        return modifier.rotateAllWordsBy(current, rotateBy, disabledWords);
    }

    @Override
    public SearchModeEnum getSearchMode() {
        return searchMode;
    }
}
