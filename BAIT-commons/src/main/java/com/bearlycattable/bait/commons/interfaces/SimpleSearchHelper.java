package com.bearlycattable.bait.commons.interfaces;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.commons.contexts.QuickSearchContext;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.wrappers.PubComparisonResultWrapper;
import com.bearlycattable.bait.commons.wrappers.QuickSearchTaskWrapper;

public interface SimpleSearchHelper {

    /**
     * For QuickSearch. On the same thread (deprecated)
     *
     * @param quickSearchContext
     * @return
     */
    @NonNull
    PubComparisonResultWrapper quickSearchSameThread(QuickSearchContext quickSearchContext);

    /**
     * For QuickSearch. On another thread
     * @param quickSearchContext
     * @return
     */
    QuickSearchTaskWrapper createNewQuickSearchTask(QuickSearchContext quickSearchContext);

    ScaleFactorEnum getScaleFactor();

    SearchModeEnum getSearchMode();

    int getIterations();
}
