package com.bearlycattable.bait.bl.searchHelper.context;

import com.bearlycattable.bait.bl.controllers.quickSearchTab.QuickSearchTabController;
import com.bearlycattable.bait.commons.wrappers.PubComparisonResultWrapper;

import javafx.concurrent.Task;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class QuickSearchThreadContext {

    private final String threadId;
    private final Task<PubComparisonResultWrapper> searchTask;
    private final QuickSearchTabController controller;
    private final int accuracy;
    private final boolean verboseMode;
    @Setter
    private volatile boolean prepared;
}
