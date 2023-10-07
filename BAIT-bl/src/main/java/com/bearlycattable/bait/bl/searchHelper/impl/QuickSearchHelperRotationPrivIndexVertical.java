package com.bearlycattable.bait.bl.searchHelper.impl;

import java.util.List;
import java.util.logging.Logger;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.bl.searchHelper.AbstractQuickSearchHelper;
import com.bearlycattable.bait.commons.contexts.QuickSearchContext;
import com.bearlycattable.bait.commons.contexts.SimpleSearchHelperCreationContext;
import com.bearlycattable.bait.commons.enums.OutputCaseEnum;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.interfaces.IndexRotatorVertical;
import com.bearlycattable.bait.commons.wrappers.QuickSearchTaskWrapper;
import com.bearlycattable.bait.utility.addressModifiers.stringModifiers.VRotatorModifier;
import com.bearlycattable.bait.utility.addressModifiers.stringModifiers.VRotatorModifierImpl;

/**
    Keeps iterating until no better result can be found by vertical rotation only.
    Number of iterations is ignored for this particular implementation of SimpleSearchHelper
     */
public final class QuickSearchHelperRotationPrivIndexVertical extends AbstractQuickSearchHelper implements IndexRotatorVertical {

    private static final Logger LOG = Logger.getLogger(QuickSearchHelperRotationPrivIndexVertical.class.getName());
    private static final SearchModeEnum searchMode = SearchModeEnum.ROTATION_INDEX_VERTICAL;

    private final VRotatorModifier modifier = new VRotatorModifierImpl(OutputCaseEnum.UPPERCASE);

    public QuickSearchHelperRotationPrivIndexVertical(@NonNull SimpleSearchHelperCreationContext creationContext) {
        super(creationContext);
    }

    @Override
    public QuickSearchTaskWrapper createNewQuickSearchTask(QuickSearchContext quickSearchContext) {
        return quickSearchTaskGuiCreationHelper(quickSearchContext);
    }

    @NonNull
    @Override
    public String rotateAtIndex(String current, List<Integer> disabledWords, int index) {
        return modifier.rotateSelectedIndexVertically(current, disabledWords, index);
    }

    @Override
    public boolean isValidIndexForVerticalRotation(String address, List<Integer> disabledWords, int selectedIndex) {
        return modifier.isValidIndexForVerticalRotation(address, disabledWords, selectedIndex);
    }

    @Override
    public SearchModeEnum getSearchMode() {
        return searchMode;
    }
}
