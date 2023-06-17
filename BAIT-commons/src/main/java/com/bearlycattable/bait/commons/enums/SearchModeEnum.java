package com.bearlycattable.bait.commons.enums;

import java.util.Arrays;
import java.util.Optional;

public enum SearchModeEnum {
    RANDOM ("Random (default)", "RND"),
    RANDOM_PREFIXED_WORD ("Random (word-prefixed)", "RND_PREF"),
    RANDOM_SAME_WORD ("Random (same word)", "RND_SW"),
    INCREMENTAL_ABSOLUTE ("Incremental (absolute)", "INC_ABS"),
    INCREMENTAL_WORDS("Incremental (words)", "INC_WORDS"),
    DECREMENTAL_ABSOLUTE ("Decremental (absolute)", "DEC_ABS"),
    DECREMENTAL_WORDS ("Decremental (words)", "DEC_WORDS"),
    ROTATION_PRIV_FULL_NORMAL ("Rotation (normal)", "HROT"),
    ROTATION_PRIV_FULL_PREFIXED ("Rotation (prefixed)", "HROT_PREF"),
    ROTATION_PRIV_WORDS("Rotation (words)", "HROT_WORDS"),
    ROTATION_PRIV_INDEX_VERTICAL("Rotation (vertical)", "VROT"),
    FUZZING ("Fuzzing (words)", "FUZZING"),
    MIXED ("Mixed mode (recommended)", "MIXED");

    private String label;
    private String abbr; //abbreviation

    SearchModeEnum(String label, String abbr) {
        this.label = label;
        this.abbr = abbr;
    }

    public String getLabel() {
        return label;
    }

    public String getAbbr() {
        return abbr;
    }

    public static Optional<SearchModeEnum> getByLabel(String label) {
        return Arrays.stream(SearchModeEnum.values())
                .filter(enm -> enm.getLabel().equals(label))
                .findFirst();
    }

    public static boolean isFiniteMode(SearchModeEnum mode) {
        return ROTATION_PRIV_INDEX_VERTICAL == mode
                || ROTATION_PRIV_WORDS == mode
                || ROTATION_PRIV_FULL_NORMAL == mode
                || ROTATION_PRIV_FULL_PREFIXED == mode;
    }

    public static boolean isVerticalRotationMode(SearchModeEnum mode) {
        return ROTATION_PRIV_INDEX_VERTICAL == mode;
    }

    public static boolean isHorizontalRotationMode(SearchModeEnum mode) {
        return ROTATION_PRIV_WORDS == mode || ROTATION_PRIV_FULL_NORMAL == mode || ROTATION_PRIV_FULL_PREFIXED == mode;
    }

    public static boolean isRandomRelatedMode(SearchModeEnum mode) {
        return RANDOM == mode || RANDOM_SAME_WORD == mode || RANDOM_PREFIXED_WORD == mode;
    }

    public static boolean isIncDecRelatedMode(SearchModeEnum mode) {
        return INCREMENTAL_ABSOLUTE == mode || INCREMENTAL_WORDS == mode || DECREMENTAL_ABSOLUTE == mode || DECREMENTAL_WORDS == mode;
    }
}
