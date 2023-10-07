package com.bearlycattable.bait.bl.searchHelper.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.bl.searchHelper.impl.QuickSearchHelperDecrementalAbsolute;
import com.bearlycattable.bait.bl.searchHelper.impl.QuickSearchHelperDecrementalWords;
import com.bearlycattable.bait.bl.searchHelper.impl.QuickSearchHelperIncrementalAbsolute;
import com.bearlycattable.bait.bl.searchHelper.impl.QuickSearchHelperIncrementalWords;
import com.bearlycattable.bait.bl.searchHelper.impl.QuickSearchHelperRandom;
import com.bearlycattable.bait.bl.searchHelper.impl.QuickSearchHelperRandomSameWord;
import com.bearlycattable.bait.bl.searchHelper.impl.QuickSearchHelperRotationPrivFullNormal;
import com.bearlycattable.bait.bl.searchHelper.impl.QuickSearchHelperRotationPrivFullPrefixed;
import com.bearlycattable.bait.bl.searchHelper.impl.QuickSearchHelperRotationPrivIndexVertical;
import com.bearlycattable.bait.bl.searchHelper.impl.QuickSearchHelperRotationPrivWords;
import com.bearlycattable.bait.commons.contexts.SimpleSearchHelperCreationContext;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.interfaces.CustomKeyGenerator;
import com.bearlycattable.bait.commons.interfaces.QuickSearchHelper;

public class SimpleSearchHelperFactory {

    private static QuickSearchHelper getSearchHelper(SearchModeEnum searchMode, @NonNull SimpleSearchHelperCreationContext creationContext) {
        switch (searchMode) {
            case DECREMENTAL_ABSOLUTE:
                return new QuickSearchHelperDecrementalAbsolute(creationContext);
            case DECREMENTAL_WORDS:
                return new QuickSearchHelperDecrementalWords(creationContext);
            case INCREMENTAL_ABSOLUTE:
                return new QuickSearchHelperIncrementalAbsolute(creationContext);
            case INCREMENTAL_WORDS:
                return new QuickSearchHelperIncrementalWords(creationContext);
            case RANDOM:
                return new QuickSearchHelperRandom(creationContext);
            case RANDOM_SAME_WORD:
                return new QuickSearchHelperRandomSameWord(creationContext);
            case ROTATION_FULL:
                return new QuickSearchHelperRotationPrivFullNormal(creationContext);
            case ROTATION_FULL_WITH_HEADER:
                return new QuickSearchHelperRotationPrivFullPrefixed(creationContext);
            case ROTATION_INDEX_VERTICAL:
                return new QuickSearchHelperRotationPrivIndexVertical(creationContext);
            case ROTATION_WORDS:
                return new QuickSearchHelperRotationPrivWords(creationContext);
            // case FUZZING:
            //     return new SimpleSearchHelperFuzzing(creationContext);
            // case MIXED:
            //     throw new IllegalArgumentException("This cannot be used for the following search helper: MIXED");
            default:
                throw new UnsupportedOperationException("Search type [type=" + searchMode + "] not supported at #getSimpleSearchHelper");
        }
    }

    // private static SimpleSearchHelper getMixedSearchHelper(@NonNull SimpleSearchHelperCreationContext creationContext) {
    //     return new SimpleSearchHelperMixed(creationContext);
    // }

    public static synchronized QuickSearchHelper findRequestedSearchHelper(SearchModeEnum searchMode, SimpleSearchHelperCreationContext context, List<SearchModeEnum> mixedSearchSequence) {
        return getSearchHelper(searchMode, context);

        // if (SearchModeEnum.MIXED != searchMode) {
        //     return getSearchHelper(searchMode, context);
        // }
        //
        // Objects.requireNonNull(mixedSearchSequence);
        // SimpleSearchHelper simpleSearchHelper = getMixedSearchHelper(context);
        // ((SimpleSearchHelperMixed) simpleSearchHelper).setSearchSequence(mixedSearchSequence);
        // return simpleSearchHelper;
    }

    public static Function<String, String> asNextPrivFunction(SearchModeEnum searchMode, List<Integer> disabledWords) {
        if (SearchModeEnum.MIXED == searchMode) {
           throw new IllegalArgumentException("Cannot create 'next priv function' for type: " + searchMode);
        }

        QuickSearchHelper sh = getSearchHelper(searchMode, SimpleSearchHelperCreationContext.builder().build());
        if (SearchModeEnum.isRandomRelatedMode(sh.getSearchMode()) || SearchModeEnum.isIncDecRelatedMode(sh.getSearchMode())) {
            CustomKeyGenerator generator = (CustomKeyGenerator)sh;
            List<Integer> disabledWordsCopy = new ArrayList<>(disabledWords);
            return input -> generator.buildNextPriv(input, disabledWordsCopy);
        }

        throw new IllegalArgumentException("Cannot create 'next priv function' for type: " + searchMode);
    }
}
