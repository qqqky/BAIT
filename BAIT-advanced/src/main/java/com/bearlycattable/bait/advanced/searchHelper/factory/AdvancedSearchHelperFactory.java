package com.bearlycattable.bait.advanced.searchHelper.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    private static Optional<AdvancedSearchHelper> getSearchHelper(SearchModeEnum searchMode, @NonNull AdvancedSearchHelperCreationContext creationContext) {
        if (searchMode == null) {
            return Optional.empty();
        }

        switch (searchMode) {
            case DECREMENTAL_ABSOLUTE:
                return Optional.of(new AdvancedSearchHelperDecrementalAbsolute(creationContext));
            case DECREMENTAL_WORDS:
                return Optional.of(new AdvancedSearchHelperDecrementalWords(creationContext));
            case INCREMENTAL_ABSOLUTE:
                return Optional.of(new AdvancedSearchHelperIncrementalAbsolute(creationContext));
            case INCREMENTAL_WORDS:
                return Optional.of(new AdvancedSearchHelperIncrementalWords(creationContext));
            case RANDOM:
                return Optional.of(new AdvancedSearchHelperRandom(creationContext));
            case RANDOM_PREFIXED_WORD:
                return Optional.of(new AdvancedSearchHelperRandomPrefixedWord(creationContext));
            case RANDOM_SAME_WORD:
                return Optional.of(new AdvancedSearchHelperRandomSameWord(creationContext));
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

    public static synchronized Optional<AdvancedSearchHelper> findRequestedSearchHelper(SearchModeEnum searchMode, AdvancedSearchHelperCreationContext context, List<SearchModeEnum> mixedSearchSequence) {
        return getSearchHelper(searchMode, context);
        // if (SearchModeEnum.MIXED != searchMode) {
        //     return getSearchHelper(searchMode, context);
        // }

        // Objects.requireNonNull(mixedSearchSequence);
        // AdvancedSearchHelper advancedSearchHelper = getMixedSearchHelper(context);
        // ((AdvancedSearchHelperMixed) advancedSearchHelper).setSearchSequence(mixedSearchSequence);
        // return advancedSearchHelper;
    }

    public static Function<String, String> asNextPrivFunction(SearchModeEnum searchMode, List<Integer> disabledWords) {
        // if (SearchModeEnum.MIXED == searchMode) {
        //     throw new IllegalArgumentException("Cannot create 'next priv function' for type: " + searchMode);
        // }

        CustomKeyGenerator generator = getSearchHelper(searchMode, AdvancedSearchHelperCreationContext.builder().build())
                // .filter(sh -> SearchModeEnum.isRandomRelatedMode(sh.getSearchMode()) || SearchModeEnum.isIncDecRelatedMode(sh.getSearchMode()))
                .filter(sh -> sh instanceof CustomKeyGenerator)
                .map(CustomKeyGenerator.class::cast)
                .orElse(null);

        if (generator != null) {
            List<Integer> disabledWordsCopy = new ArrayList<>(disabledWords);
            return input -> generator.buildNextPriv(input, disabledWordsCopy);
        }

        throw new IllegalArgumentException("Cannot create 'next priv function' for search mode: " + searchMode);
    }
}
