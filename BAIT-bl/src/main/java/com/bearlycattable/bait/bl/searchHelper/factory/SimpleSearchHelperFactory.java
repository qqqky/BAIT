package com.bearlycattable.bait.bl.searchHelper.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.bl.searchHelper.impl.SimpleSearchHelperDecrementalAbsolute;
import com.bearlycattable.bait.bl.searchHelper.impl.SimpleSearchHelperDecrementalWords;
import com.bearlycattable.bait.bl.searchHelper.impl.SimpleSearchHelperIncrementalAbsolute;
import com.bearlycattable.bait.bl.searchHelper.impl.SimpleSearchHelperIncrementalWords;
import com.bearlycattable.bait.bl.searchHelper.impl.SimpleSearchHelperRandom;
import com.bearlycattable.bait.bl.searchHelper.impl.SimpleSearchHelperRandomSameWord;
import com.bearlycattable.bait.bl.searchHelper.impl.SimpleSearchHelperRotationPrivFullNormal;
import com.bearlycattable.bait.bl.searchHelper.impl.SimpleSearchHelperRotationPrivFullPrefixed;
import com.bearlycattable.bait.bl.searchHelper.impl.SimpleSearchHelperRotationPrivIndexVertical;
import com.bearlycattable.bait.bl.searchHelper.impl.SimpleSearchHelperRotationPrivWords;
import com.bearlycattable.bait.commons.contexts.SimpleSearchHelperCreationContext;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.interfaces.CustomKeyGenerator;
import com.bearlycattable.bait.commons.interfaces.SimpleSearchHelper;

public class SimpleSearchHelperFactory {

    private static SimpleSearchHelper getSearchHelper(SearchModeEnum searchMode, @NonNull SimpleSearchHelperCreationContext creationContext) {
        switch (searchMode) {
            case DECREMENTAL_ABSOLUTE:
                return new SimpleSearchHelperDecrementalAbsolute(creationContext);
            case DECREMENTAL_WORDS:
                return new SimpleSearchHelperDecrementalWords(creationContext);
            case INCREMENTAL_ABSOLUTE:
                return new SimpleSearchHelperIncrementalAbsolute(creationContext);
            case INCREMENTAL_WORDS:
                return new SimpleSearchHelperIncrementalWords(creationContext);
            case RANDOM:
                return new SimpleSearchHelperRandom(creationContext);
            case RANDOM_SAME_WORD:
                return new SimpleSearchHelperRandomSameWord(creationContext);
            case ROTATION_PRIV_FULL_NORMAL:
                return new SimpleSearchHelperRotationPrivFullNormal(creationContext);
            case ROTATION_PRIV_FULL_PREFIXED:
                return new SimpleSearchHelperRotationPrivFullPrefixed(creationContext);
            case ROTATION_PRIV_INDEX_VERTICAL:
                return new SimpleSearchHelperRotationPrivIndexVertical(creationContext);
            case ROTATION_PRIV_WORDS:
                return new SimpleSearchHelperRotationPrivWords(creationContext);
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

    public static synchronized SimpleSearchHelper findRequestedSearchHelper(SearchModeEnum searchMode, SimpleSearchHelperCreationContext context, List<SearchModeEnum> mixedSearchSequence) {
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

        SimpleSearchHelper sh = getSearchHelper(searchMode, SimpleSearchHelperCreationContext.builder().build());
        if (SearchModeEnum.isRandomRelatedMode(sh.getSearchMode()) || SearchModeEnum.isIncDecRelatedMode(sh.getSearchMode())) {
            CustomKeyGenerator generator = (CustomKeyGenerator)sh;
            List<Integer> disabledWordsCopy = new ArrayList<>(disabledWords);
            return input -> generator.buildNextPriv(input, disabledWordsCopy);
        }

        throw new IllegalArgumentException("Cannot create 'next priv function' for type: " + searchMode);
    }
}
