package com.bearlycattable.bait.advanced.searchHelper.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.advancedCommons.contexts.AdvancedSearchHelperCreationContext;
import com.bearlycattable.bait.advanced.searchHelper.impl.AdvancedSearchHelperDecrementalAbsolute;
import com.bearlycattable.bait.advanced.searchHelper.impl.AdvancedSearchHelperDecrementalWords;
import com.bearlycattable.bait.advanced.searchHelper.impl.AdvancedSearchHelperIncrementalAbsolute;
import com.bearlycattable.bait.advanced.searchHelper.impl.AdvancedSearchHelperIncrementalWords;
import com.bearlycattable.bait.advanced.searchHelper.impl.AdvancedSearchHelperRandom;
import com.bearlycattable.bait.advanced.searchHelper.impl.AdvancedSearchHelperRandomPrefixedWord;
import com.bearlycattable.bait.advanced.searchHelper.impl.AdvancedSearchHelperRandomSameWord;
import com.bearlycattable.bait.advancedCommons.interfaces.AdvancedSearchHelper;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.interfaces.CustomKeyGenerator;

public class AdvancedSearchHelperFactory {

    private static AdvancedSearchHelper getSearchHelper(SearchModeEnum searchMode, @NonNull AdvancedSearchHelperCreationContext creationContext) {
        if (searchMode == null) {
            return null;
        }

        switch (searchMode) {
            case DECREMENTAL_ABSOLUTE:
                return new AdvancedSearchHelperDecrementalAbsolute(creationContext);
            case DECREMENTAL_WORDS:
                return new AdvancedSearchHelperDecrementalWords(creationContext);
            case INCREMENTAL_ABSOLUTE:
                return new AdvancedSearchHelperIncrementalAbsolute(creationContext);
            case INCREMENTAL_WORDS:
                return new AdvancedSearchHelperIncrementalWords(creationContext);
            case RANDOM:
                return new AdvancedSearchHelperRandom(creationContext);
            case RANDOM_PREFIXED_WORD:
                return new AdvancedSearchHelperRandomPrefixedWord(creationContext);
            case RANDOM_SAME_WORD:
                return new AdvancedSearchHelperRandomSameWord(creationContext);
            // case ROTATION_PRIV_FULL_NORMAL:
            //     return new AdvancedSearchHelperRotationPrivFullNormal(creationContext);
            // case ROTATION_PRIV_FULL_PREFIXED:
            //     return new AdvancedSearchHelperRotationPrivFullPrefixed(creationContext);
            // case ROTATION_PRIV_INDEX_VERTICAL:
            //     return new AdvancedSearchHelperRotationPrivIndexVertical(creationContext);
            // case ROTATION_PRIV_WORDS:
            //     return new AdvancedSearchHelperRotationPrivWords(creationContext);
            // case FUZZING:
            //     return new AdvancedSearchHelperFuzzing(creationContext);
            // case MIXED:
            //     throw new IllegalArgumentException("This cannot be used for the following search helper: MIXED");
            default:
                throw new UnsupportedOperationException("Search type [type=" + searchMode + "] not supported at #getSimpleSearchHelper");
        }
    }

    // private static AdvancedSearchHelper getMixedSearchHelper(@NonNull AdvancedSearchHelperCreationContext creationContext) {
    //     return new AdvancedSearchHelperMixed(creationContext);
    // }

    public static synchronized AdvancedSearchHelper findRequestedSearchHelper(SearchModeEnum searchMode, AdvancedSearchHelperCreationContext context, List<SearchModeEnum> mixedSearchSequence) {
        return getSearchHelper(searchMode, context);
        // if (SearchModeEnum.MIXED != searchMode) {
        //     return getSearchHelper(searchMode, context);
        // }

        // Objects.requireNonNull(mixedSearchSequence);
        // AdvancedSearchHelper advancedSearchHelper = getMixedSearchHelper(context);
        // ((AdvancedSearchHelperMixed) advancedSearchHelper).setSearchSequence(mixedSearchSequence);
        // return advancedSearchHelper;
    }

    //TODO: export this?
    public static Function<String, String> asNextPrivFunction(SearchModeEnum searchMode, List<Integer> disabledWords) {
        // if (SearchModeEnum.MIXED == searchMode) {
        //     throw new IllegalArgumentException("Cannot create 'next priv function' for type: " + searchMode);
        // }

        AdvancedSearchHelper sh = getSearchHelper(searchMode, AdvancedSearchHelperCreationContext.builder().build());
        if (SearchModeEnum.isRandomRelatedMode(sh.getSearchMode()) || SearchModeEnum.isIncDecRelatedMode(sh.getSearchMode())) {
            CustomKeyGenerator generator = (CustomKeyGenerator) sh;
            List<Integer> disabledWordsCopy = new ArrayList<>(disabledWords);
            return input -> generator.buildNextPriv(input, disabledWordsCopy);
        }

        throw new IllegalArgumentException("Cannot create 'next priv function' for type: " + searchMode);
    }
}
