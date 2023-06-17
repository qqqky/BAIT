package com.bearlycattable.bait.commons.wrappers;

import com.bearlycattable.bait.commons.enums.NumberFormatTypeEnum;

public interface PrivHeatResultWrapper {

    long getHeatPositive();

    long getHeatNegative();

    NumberFormatTypeEnum getType();

    String getHeatPositiveAsString();

    String getHeatNegativeAsString();
}
