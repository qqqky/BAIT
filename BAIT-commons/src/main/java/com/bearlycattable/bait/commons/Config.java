package com.bearlycattable.bait.commons;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;

public class Config {

    public static final int DEFAULT_ACCURACY_QUICK_SEARCH = 55;
    public static final int DEFAULT_ITERATIONS_QUICK_SEARCH = 4096;
    public static final int DEFAULT_LOOPS_ADVANCED_SEARCH = 1;
    public static final int DEFAULT_LOG_SPACING_ADVANCED_SEARCH = 100000;
    public static final ScaleFactorEnum DEFAULT_SCALE_FACTOR = ScaleFactorEnum.MEDIUM;

    public static final int MAX_ITERATIONS_QUICK_SEARCH = 1_048_576; //0x00100000
    public static final int MAX_ITERATIONS_ADVANCED_SEARCH = 268_435_456; //0x10000000 (remove later?)

    public static final int MAX_H_ROTATIONS_WORDS = 8;
    public static final int MAX_H_ROTATIONS_FULL = 64;
    public static final int MAX_H_ROTATIONS_PREFIXED = 128;
    public static final int MAX_V_ROTATIONS = 16;
    public static final int MAX_LOOPS = 256; //0x00000100
    public static final int MAX_LOG_SPACING_ADVANCED_SEARCH = 8_388_608; //1048576 * 8
    public static final int MAX_SOUND_NOTIFICATION_POINT_BARRIER = 9999;

    public static final int MAX_CACHEABLE_ADDRESSES_IN_TEMPLATE = 5000; //do not change this value

    public static final List<SearchModeEnum> DEFAULT_FUZZING_SEARCH_SUBSEQUENCE = Arrays.asList(
            SearchModeEnum.ROTATION_WORDS, SearchModeEnum.INCREMENTAL_WORDS, SearchModeEnum.DECREMENTAL_WORDS,
            SearchModeEnum.ROTATION_INDEX_VERTICAL);

    public static final List<String> SUPPORTED_QUICKSEARCH_TYPES = Collections.unmodifiableList(Arrays.asList(
            SearchModeEnum.RANDOM.getLabel(), SearchModeEnum.RANDOM_SAME_WORD.getLabel(),
            SearchModeEnum.INCREMENTAL_ABSOLUTE.getLabel(), SearchModeEnum.INCREMENTAL_WORDS.getLabel(),
            SearchModeEnum.DECREMENTAL_ABSOLUTE.getLabel(), SearchModeEnum.DECREMENTAL_WORDS.getLabel(),
            SearchModeEnum.ROTATION_INDEX_VERTICAL.getLabel(), SearchModeEnum.ROTATION_WORDS.getLabel(),
            SearchModeEnum.ROTATION_FULL.getLabel(), SearchModeEnum.ROTATION_FULL_WITH_HEADER.getLabel()
            ));

    public static final List<String> SUPPORTED_ADVANCED_SEARCH_TYPES = Collections.unmodifiableList(Arrays.asList(
            SearchModeEnum.RANDOM.getLabel(), SearchModeEnum.RANDOM_SAME_WORD.getLabel(), SearchModeEnum.RANDOM_PREFIXED_WORD.getLabel(),
            SearchModeEnum.INCREMENTAL_ABSOLUTE.getLabel(), SearchModeEnum.INCREMENTAL_WORDS.getLabel(),
            SearchModeEnum.DECREMENTAL_ABSOLUTE.getLabel(), SearchModeEnum.DECREMENTAL_WORDS.getLabel()));

    //dev
    public static final String EXACT_MATCH_ADDRESSES_LIST_PATH = System.getProperty("user.dir") + "/BAIT-ui/app/exactMatchCheckListExample.txt";
    public static final String EXACT_MATCH_SAVE_PATH = System.getProperty("user.dir") + "/BAIT-ui/app/matches.txt";

    //prod
    // public static final String EXACT_MATCH_ADDRESSES_LIST_PATH = System.getProperty("user.dir") + "/app/exactMatchCheckListExample.txt";
    // public static final String EXACT_MATCH_SAVE_PATH = System.getProperty("user.dir") + "/app/matches.txt";
}
