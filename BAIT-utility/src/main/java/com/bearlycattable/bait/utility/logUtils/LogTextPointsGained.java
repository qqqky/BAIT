package com.bearlycattable.bait.utility.logUtils;

import com.bearlycattable.bait.commons.enums.LogTextTypeEnum;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class LogTextPointsGained extends LogText {

    @Override
    public LogTextTypeEnum getType() {
        return LogTextTypeEnum.POINTS_GAINED;
    }
}