package com.bearlycattable.bait.commons.wrappers;

import com.bearlycattable.bait.commons.wrappers.PubComparisonResultWrapper;

import javafx.concurrent.Task;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuickSearchTaskWrapper {

    private final Task<PubComparisonResultWrapper> task;
    private final String error;

    public boolean hasTask() {
        return error == null && task != null;
    }
}
