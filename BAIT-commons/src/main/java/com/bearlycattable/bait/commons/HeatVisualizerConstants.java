package com.bearlycattable.bait.commons;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.pubKeyComparison.PubComparisonResultSWrapper;

public final class HeatVisualizerConstants {

    private HeatVisualizerConstants() {}

    public static final String CURRENT_VERSION = "v1.0.5";
    public static final Locale EN_US = new Locale("en", "US");

    public static final String ZERO_STRING = "0";
    public static final long ZERO_LONG = 0L;
    public static final int ZERO_INT = 0;
    public static final char ZERO_CHARACTER = '0';
    public static final String F_STRING = "F";
    public static final String EMPTY_STRING = "";

    public static final BigDecimal MAX_SIMILARITY_MAP_INDEX = new BigDecimal("640");
    public static final BigDecimal SINGLE_POINT_VALUE = new BigDecimal("0.15625"); //0.15625*16*40=100%

    public static final BigDecimal MAX_ACCURACY = new BigDecimal(100);
    public static final Pattern PATTERN_SIMPLE_64 = Pattern.compile("[0123456789ABCDEFabcdef]{64}");
    public static final Pattern PATTERN_SIMPLE_40 = Pattern.compile("[0123456789ABCDEFabcdef]{40}");
    public static final Pattern PATTERN_SIMPLE_UP_TO_40 = Pattern.compile("^[0123456789ABCDEFabcdef]{1,40}$");
    public static final Pattern PATTERN_HEX_01 = Pattern.compile("^[0123456789ABCDEFabcdef]$");
    public static final Pattern PATTERN_HEX_08 = Pattern.compile("^[0123456789ABCDEFabcdef]{8}$");
    public static final Pattern PATTERN_HEX_01_TO_08 = Pattern.compile("^[0123456789ABCDEFabcdef]{1,8}$");
    public static final Pattern DIGITS_ONLY_MAX2 = Pattern.compile("^[\\d]{1,2}$");
    public static final Pattern DIGITS_ONLY_MAX3 = Pattern.compile("^[\\d]{1,3}$");
    public static final Pattern DIGITS_ONLY_MAX4 = Pattern.compile("^[\\d]{1,4}$");
    public static final Pattern DIGITS_ONLY_MAX6 = Pattern.compile("^[\\d]{1,6}$");
    public static final Pattern DIGITS_ONLY_MAX10 = Pattern.compile("^[\\d]{1,10}$");
    public static final long OVERFLOW_REFERENCE_8_HEX = 0x100000000L;
    public static final int OVERFLOW_REFERENCE_1_HEX = 0x10;

    //bytes from bitcoin min and max keys (inclusive)
    private final byte[] ALL_BYTES_MIN = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2};
    private final byte[] ALL_BYTES_MAX = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -2, -70, -82, -36, -26, -81, 72, -96, 59, -65, -46, 94, -116, -48, 54, 65, 64};

    public static final List<Integer> ALL_WORD_NUMBERS = Collections.unmodifiableList(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));
    public static final PubComparisonResultSWrapper EMPTY_RESULT_WRAPPER = PubComparisonResultSWrapper.empty();

    public static final List<SearchModeEnum> MIXED_SEARCH_SEQUENCE_WITH_RANDOM = Collections.unmodifiableList(Arrays.asList(SearchModeEnum.RANDOM, SearchModeEnum.ROTATION_FULL, SearchModeEnum.ROTATION_FULL_WITH_HEADER,
        SearchModeEnum.FUZZING, SearchModeEnum.INCREMENTAL_ABSOLUTE, SearchModeEnum.DECREMENTAL_ABSOLUTE, SearchModeEnum.INCREMENTAL_WORDS,
        SearchModeEnum.DECREMENTAL_WORDS, SearchModeEnum.ROTATION_INDEX_VERTICAL));

    public static final List<SearchModeEnum> MIXED_SEARCH_SEQUENCE_WITHOUT_RANDOM = Collections.unmodifiableList(Arrays.asList(SearchModeEnum.ROTATION_FULL, SearchModeEnum.ROTATION_FULL_WITH_HEADER, SearchModeEnum.FUZZING,
        SearchModeEnum.INCREMENTAL_ABSOLUTE, SearchModeEnum.DECREMENTAL_ABSOLUTE, SearchModeEnum.INCREMENTAL_WORDS,
        SearchModeEnum.DECREMENTAL_WORDS, SearchModeEnum.ROTATION_INDEX_VERTICAL));

    public static final List<SearchModeEnum> MIXED_SEARCH_SEQUENCE_WITHOUT_RANDOM_AND_FUZZING = Collections.unmodifiableList(Arrays.asList(SearchModeEnum.ROTATION_FULL, SearchModeEnum.ROTATION_FULL_WITH_HEADER,
        SearchModeEnum.INCREMENTAL_ABSOLUTE, SearchModeEnum.DECREMENTAL_ABSOLUTE, SearchModeEnum.INCREMENTAL_WORDS,
        SearchModeEnum.DECREMENTAL_WORDS, SearchModeEnum.ROTATION_INDEX_VERTICAL));

    public static final String[] HEX_ALPHABET = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

    //for safekeeping - all privs get the same 64 hex header to make a "full priv" (header+priv) internally
    public static final String PRIV_HEADER = "303E020100301006072A8648CE3D020106052B8104000A042730250201010420";

}
