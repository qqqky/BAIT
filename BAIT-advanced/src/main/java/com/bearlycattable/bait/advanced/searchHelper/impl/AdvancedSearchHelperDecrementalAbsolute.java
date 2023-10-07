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
import com.bearlycattable.bait.utility.addressModifiers.byteModifiers.DecrementModifierB;
import com.bearlycattable.bait.utility.addressModifiers.byteModifiers.DecrementModifierBImpl;
import com.bearlycattable.bait.utility.addressModifiers.byteModifiers.IncrementModifierB;
import com.bearlycattable.bait.utility.addressModifiers.byteModifiers.IncrementModifierBImpl;
import com.bearlycattable.bait.utility.addressModifiers.stringModifiers.DecrementModifier;
import com.bearlycattable.bait.utility.addressModifiers.stringModifiers.DecrementModifierImpl;

public final class AdvancedSearchHelperDecrementalAbsolute extends AbstractAdvancedSearchHelper implements CustomKeyGenerator, PredictableEnd {

    private static final Logger LOG = Logger.getLogger(AdvancedSearchHelperDecrementalAbsolute.class.getName());
    private static final SearchModeEnum searchMode = SearchModeEnum.DECREMENTAL_ABSOLUTE;

    private final DecrementModifier modifier = new DecrementModifierImpl(OutputCaseEnum.UPPERCASE);
    private final DecrementModifierB modifierB = new DecrementModifierBImpl();

    public AdvancedSearchHelperDecrementalAbsolute(@NonNull AdvancedSearchHelperCreationContext creationContext) {
        super(creationContext);
    }

    @Override
    public AdvancedSearchTaskWrapper createNewAdvancedSearchTask(AdvancedSearchContext advancedSearchContext) {
        return advancedSearchTaskGuiCreationHelper(advancedSearchContext);
    }

    @NonNull
    @Override
    public String buildNextPriv(String current, List<Integer> disabledWords) {
        return modifier.decrementPrivAbsolute(current, disabledWords);
    }

    @Override
    public byte[] buildNextPrivBytes(byte[] current, List<Integer> disabledWords) {
        return modifierB.decrementPrivAbsoluteB(current, disabledWords);
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
        return modifier.decrementPrivAbsoluteBy(seed, iterations, disabledWords);
    }
}
