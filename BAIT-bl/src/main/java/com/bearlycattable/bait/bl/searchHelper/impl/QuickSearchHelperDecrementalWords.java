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
import com.bearlycattable.bait.utility.AddressModifier;

public final class QuickSearchHelperDecrementalWords extends AbstractQuickSearchHelper implements CustomKeyGenerator {

    private static final Logger LOG = Logger.getLogger(QuickSearchHelperDecrementalWords.class.getName());
    private static final SearchModeEnum searchMode = SearchModeEnum.DECREMENTAL_WORDS;

    private final AddressModifier modifier = new AddressModifier(OutputCaseEnum.UPPERCASE);

    public QuickSearchHelperDecrementalWords(@NonNull SimpleSearchHelperCreationContext creationContext) {
        super(creationContext);
    }

    // @NonNull
    // @Override
    // public PubComparisonResultWrapper quickSearchSameThread(QuickSearchContext quickSearchContext) {
    //     List<Integer> disabledWords = quickSearchContext.getDisabledWords();
    //     quickSearchContext.setNextPrivFunction(seed -> buildNextPriv(seed, disabledWords));
    //     return iterateForIncDecMode(quickSearchContext);
    // }

    @Override
    public QuickSearchTaskWrapper createNewQuickSearchTask(QuickSearchContext quickSearchContext) {
        // List<Integer> disabledWords = quickSearchContext.getDisabledWords();
        // quickSearchContext.setNextPrivFunction(seed -> buildNextPriv(seed, disabledWords));
        // quickSearchContext.setEvaluationFunction(super.createGeneralEvaluationFunction(quickSearchContext));
        // return QuickSearchTaskFactory.createNewQuickSearchTask(quickSearchContext);
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

    // /*------ unsupported methods from base class: ------- */
    //
    // @Override
    // public @NonNull String rotateLeft(String current) {
    //     throw new UnsupportedOperationException("Not supported " + this.getClass().getName() + "@rotateLeft(String)");
    // }
    //
    // @Override
    // public @NonNull String rotateLeftBy(String current, int rotateBy) {
    //     throw new UnsupportedOperationException("Not supported " + this.getClass().getName() + "@rotateLeftBy(String, int)");
    // }
    //
    // @Override
    // public @NonNull String rotateLeft(String current, List<Integer> disabledWords) {
    //     throw new UnsupportedOperationException("Not supported " + this.getClass().getName() + "@rotateLeft(String, List<Integer>)");
    // }
    //
    // @Override
    // public @NonNull String rotateLeftBy(String current, int rotateBy, List<Integer> disabledWords) {
    //     throw new UnsupportedOperationException("Not supported " + this.getClass().getName() + "@rotateLeftBy(String, int, List<Integer>)");
    // }
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
    // public String buildNextPriv(String current, List<Integer> disabledWords, String prefix) {
    //     throw new UnsupportedOperationException("Not supported " + this.getClass().getName() + "@buildNextPriv(String, List<Integer>, String)");
    // }
}
