package com.bearlycattable.bait.bl.wrappers;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NotificationConfigsWrapper {

    private final int notificationTolerance;
    private final String error;

    public boolean hasValidConfig() {
        return error == null || error.isEmpty();
    }
}
