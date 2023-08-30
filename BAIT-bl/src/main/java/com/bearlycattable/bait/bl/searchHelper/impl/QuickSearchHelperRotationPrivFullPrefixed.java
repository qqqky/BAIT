package com.bearlycattable.bait.bl.searchHelper.impl;

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
import com.bearlycattable.bait.utility.AddressModifier;

public final class QuickSearchHelperRotationPrivFullPrefixed extends AbstractQuickSearchHelper implements IndexRotatorHorizontal {

    private static final Logger LOG = Logger.getLogger(QuickSearchHelperRotationPrivFullPrefixed.class.getName());
    private static final SearchModeEnum searchMode = SearchModeEnum.ROTATION_PRIV_FULL_PREFIXED;

    private final AddressModifier modifier = new AddressModifier(OutputCaseEnum.UPPERCASE);

    public QuickSearchHelperRotationPrivFullPrefixed(@NonNull SimpleSearchHelperCreationContext creationContext) {
        super(creationContext);
    }

    // @NonNull
    // @Override
    // public PubComparisonResultWrapper quickSearchSameThread(QuickSearchContext quickSearchContext) {
    //     quickSearchContext.setNextPrivFunctionFullPrefixed(this::rotateLeftBy);
    //     return iterateForHRotationMode(quickSearchContext);
    // }

    @Override
    public QuickSearchTaskWrapper createNewQuickSearchTask(QuickSearchContext quickSearchContext) {
        // quickSearchContext.setEvaluationFunction(super.createGeneralEvaluationFunction(quickSearchContext));
        // quickSearchContext.setNextPrivFunctionFullPrefixed(this::rotateLeftBy);
        // return QuickSearchTaskFactory.createNewQuickSearchTask(quickSearchContext);
        return quickSearchTaskGuiCreationHelper(quickSearchContext);
    }

    @NonNull
    @Override
    public String rotateLeft(String current) {
        return modifier.rotateAddressLeftBy(current, 1, true);
    }

    @NonNull
    @Override
    public String rotateLeftBy(String current, int rotateBy) {
        return modifier.rotateAddressLeftBy(current, rotateBy, true);
    }

    @NonNull
    @Override
    public String rotateLeft(String current, List<Integer> disabledWords) {
        return rotateLeft(current); //ignore disabled words
    }

    @NonNull
    @Override
    public String rotateLeftBy(String current, int rotateBy, List<Integer> disabledWords) {
        return rotateLeftBy(current, rotateBy); //ignore disabled words
    }

    @Override
    public SearchModeEnum getSearchMode() {
        return searchMode;
    }

    // /*------ unsupported methods from base class: ------- */
    //
    // @Override
    // public @NonNull String rotateAtIndex(String current, List<Integer> disabledWords, int index) {
    //     throw new UnsupportedOperationException("Not supported " + this.getClass().getName() + "@rotateAtIndex(String, List<Integer>, int)");
    // }
    //
    // @Override
    // public boolean isValidIndexForVerticalRotation(String address, List<Integer> disabledWords, int selectedIndex) {
    //     throw new UnsupportedOperationException("Not supported " + this.getClass().getName() + "@isValidIndexForVerticalRotation(args)");
    // }
    //
    // @Override
    // public @NonNull String buildNextPriv(String current, List<Integer> disabledWords) {
    //     throw new UnsupportedOperationException("Not supported " + this.getClass().getName() + "@buildNextPriv(String, List<Integer>)");
    // }
    //
    // @Override
    // public String buildNextPriv(String current, List<Integer> disabledWords, String prefix) {
    //     throw new UnsupportedOperationException("Not supported " + this.getClass().getName() + "@buildNextPriv(String, List<Integer>, String)");
    // }
}
