package com.bearlycattable.bait.advancedCommons.interfaces;

import java.util.Set;

import com.bearlycattable.bait.advancedCommons.contexts.AdvancedSearchContext;
import com.bearlycattable.bait.advancedCommons.wrappers.AdvancedSearchTaskWrapper;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;

public interface AdvancedSearchHelper {

    /**
     * For advanced searches with multiple key support. Wrapped into a Task class to be used on a separate thread.
     * @param advancedSearchContext - context that holds all required data for creation of a Task
     * @return
     */
    AdvancedSearchTaskWrapper createNewAdvancedSearchTask(AdvancedSearchContext advancedSearchContext);

    void updateTargetForExactMatchCheck(Set<String> unencodedAddresses);

    ScaleFactorEnum getScaleFactor();

    SearchModeEnum getSearchMode();

    int getIterations();

    boolean isExactMatchCheckEnabled();

    int exactMatchCheckDataLength();
}
