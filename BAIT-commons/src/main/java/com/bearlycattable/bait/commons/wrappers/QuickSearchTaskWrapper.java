package com.bearlycattable.bait.commons.wrappers;

import com.bearlycattable.bait.commons.pubKeyComparison.PubComparisonResultSWrapper;

import javafx.concurrent.Task;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuickSearchTaskWrapper {

    private final Task<PubComparisonResultSWrapper> task;
    private final String error;

    public boolean hasTask() {
        return error == null && task != null;
    }
}
