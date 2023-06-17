package com.bearlycattable.bait.commons.validators;

import com.bearlycattable.bait.commons.enums.SearchModeEnum;

public class SearchHelperIterationsValidator {

    public static int validateAndGet(SearchModeEnum searchMode, int requestedIterations) {
        if (requestedIterations < 0) {
            return 0;
        }

        switch (searchMode) {
            case ROTATION_PRIV_FULL_NORMAL: //intentional fall-through
            case ROTATION_PRIV_INDEX_VERTICAL:
                return Math.min(requestedIterations, 64);
            case ROTATION_PRIV_FULL_PREFIXED:
                return Math.min(requestedIterations, 128);
            case ROTATION_PRIV_WORDS:
                return Math.min(requestedIterations, 8);
            case DECREMENTAL_ABSOLUTE: //intentional fall-through
            case DECREMENTAL_WORDS:
            case INCREMENTAL_ABSOLUTE:
            case INCREMENTAL_WORDS:
            case RANDOM:
            case RANDOM_PREFIXED_WORD:
            case RANDOM_SAME_WORD:
            // case FUZZING:
            // case MIXED:
                return requestedIterations;
            default:
                throw new UnsupportedOperationException("Search type [type=" + searchMode + "] not supported at SearchHelperIterationsValidator#validateAndGet");
        }
    }
}
