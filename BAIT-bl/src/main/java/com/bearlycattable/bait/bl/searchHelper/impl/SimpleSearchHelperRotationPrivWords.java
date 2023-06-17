package com.bearlycattable.bait.bl.searchHelper.impl;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.bl.searchHelper.AbstractSimpleSearchHelper;
import com.bearlycattable.bait.bl.searchHelper.factory.QuickSearchTaskFactory;
import com.bearlycattable.bait.commons.contexts.QuickSearchContext;
import com.bearlycattable.bait.commons.contexts.SimpleSearchHelperCreationContext;
import com.bearlycattable.bait.commons.enums.OutputCaseEnum;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.interfaces.IndexRotatorHorizontal;
import com.bearlycattable.bait.commons.wrappers.PubComparisonResultWrapper;
import com.bearlycattable.bait.utility.AddressModifier;

public final class SimpleSearchHelperRotationPrivWords extends AbstractSimpleSearchHelper implements IndexRotatorHorizontal {

    private static final Logger LOG = Logger.getLogger(SimpleSearchHelperRotationPrivWords.class.getName());
    private static final SearchModeEnum searchMode = SearchModeEnum.ROTATION_PRIV_WORDS;

    private final AddressModifier modifier = new AddressModifier(OutputCaseEnum.UPPERCASE);

    public SimpleSearchHelperRotationPrivWords(@NonNull SimpleSearchHelperCreationContext creationContext) {
        super(creationContext);
    }

    @NonNull
    @Override
    public PubComparisonResultWrapper quickSearchSameThread(QuickSearchContext quickSearchContext) {
        List<Integer> disabledWords = quickSearchContext.getDisabledWords();
        quickSearchContext.setNextPrivFunction(key -> rotateLeft(key, disabledWords));
        return iterateForHRotationMode(quickSearchContext);
    }

    @Override
    public QuickSearchTaskWrapper createNewQuickSearchTask(QuickSearchContext quickSearchContext) {
        quickSearchContext.setEvaluationFunction(super.createGeneralEvaluationFunction(quickSearchContext));
        quickSearchContext.setNextPrivFunction(this::rotateLeft);
        return QuickSearchTaskFactory.createNewQuickSearchTask(quickSearchContext);
    }

    @Override
    public SearchModeEnum getSearchMode() {
        return searchMode;
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
}
