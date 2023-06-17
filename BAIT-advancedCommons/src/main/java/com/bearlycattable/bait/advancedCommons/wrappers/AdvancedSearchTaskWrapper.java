package com.bearlycattable.bait.advancedCommons.wrappers;

import com.bearlycattable.bait.advancedCommons.contexts.P2PKHSingleResultData;

import javafx.concurrent.Task;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdvancedSearchTaskWrapper {

    private final Task<P2PKHSingleResultData[]> task;
    private final String error;

    public boolean hasTask() {
        return error == null && task != null;
    }

}
