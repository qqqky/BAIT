package com.bearlycattable.bait.commons.interfaces;

import com.bearlycattable.bait.commons.contexts.QuickSearchContext;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.wrappers.QuickSearchTaskWrapper;

public interface QuickSearchHelper {

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
