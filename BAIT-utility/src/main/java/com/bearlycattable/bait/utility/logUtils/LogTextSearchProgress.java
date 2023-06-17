package com.bearlycattable.bait.utility.logUtils;

import com.bearlycattable.bait.commons.enums.LogTextTypeEnum;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class LogTextSearchProgress extends LogText {

    @Override
    public LogTextTypeEnum getType() {
        return LogTextTypeEnum.SEARCH_PROGRESS;
    }
}