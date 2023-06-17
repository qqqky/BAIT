package com.bearlycattable.bait.utility.logUtils;

import com.bearlycattable.bait.commons.enums.LogTextTypeEnum;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class LogTextLogClear extends LogText {

    @Override
    public LogTextTypeEnum getType() {
        return LogTextTypeEnum.LOG_CLEAR;
    }
}
