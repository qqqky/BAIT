package com.bearlycattable.bait.advanced.searchHelper.impl;

import java.util.List;
import java.util.logging.Logger;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.advanced.searchHelper.AbstractAdvancedSearchHelper;
import com.bearlycattable.bait.advancedCommons.contexts.AdvancedSearchContext;
import com.bearlycattable.bait.advancedCommons.contexts.AdvancedSearchHelperCreationContext;
import com.bearlycattable.bait.advancedCommons.interfaces.PredictableEnd;
import com.bearlycattable.bait.advancedCommons.wrappers.AdvancedSearchTaskWrapper;
import com.bearlycattable.bait.commons.enums.OutputCaseEnum;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.interfaces.CustomKeyGenerator;
import com.bearlycattable.bait.utility.addressModifiers.stringModifiers.IncrementModifier;
import com.bearlycattable.bait.utility.addressModifiers.byteModifiers.IncrementModifierB;
import com.bearlycattable.bait.utility.addressModifiers.byteModifiers.IncrementModifierBImpl;
import com.bearlycattable.bait.utility.addressModifiers.stringModifiers.IncrementModifierImpl;

public final class AdvancedSearchHelperIncrementalAbsolute extends AbstractAdvancedSearchHelper implements CustomKeyGenerator, PredictableEnd {

    private static final Logger LOG = Logger.getLogger(AdvancedSearchHelperIncrementalAbsolute.class.getName());
    private static final SearchModeEnum searchMode = SearchModeEnum.INCREMENTAL_ABSOLUTE;

    private final IncrementModifier modifier = new IncrementModifierImpl(OutputCaseEnum.UPPERCASE);
    private final IncrementModifierB modifierB = new IncrementModifierBImpl();

    public AdvancedSearchHelperIncrementalAbsolute(@NonNull AdvancedSearchHelperCreationContext creationContext) {
        super(creationContext);
    }

    @Override
    public AdvancedSearchTaskWrapper createNewAdvancedSearchTask(AdvancedSearchContext advancedSearchContext) {
        return advancedSearchTaskGuiCreationHelper(advancedSearchContext);
    }

    @Override
    public @NonNull String buildNextPriv(String current, List<Integer> disabledWords) {
        return modifier.incrementPrivAbsolute(current, disabledWords);
    }

    @Override
    public byte[] buildNextPrivBytes(byte[] current, List<Integer> disabledWords) {
        return modifierB.incrementPrivAbsoluteB(current, disabledWords);
    }

    @Override
    public SearchModeEnum getSearchMode() {
        return searchMode;
    }

    @Override
    public boolean isByteComparisonSupported() {
        return true;
    }

    @Override
    public @NonNull String predictLastItem(@NonNull String seed, int iterations, List<Integer> disabledWords) {
        return modifier.incrementPrivAbsoluteBy(seed, iterations, disabledWords);
    }
}
