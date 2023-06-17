package com.bearlycattable.bait.commons.wrappers;

import com.bearlycattable.bait.commons.enums.NumberFormatTypeEnum;

import lombok.Builder;

@Builder
public class PrivHeatResultWrapperDecimal implements PrivHeatResultWrapper {

    private long heatPositive;
    private long heatNegative;

    @Override
    public long getHeatPositive() {
        return heatPositive;
    }

    @Override
    public long getHeatNegative() {
        return heatNegative;
    }

    @Override
    public NumberFormatTypeEnum getType() {
        return NumberFormatTypeEnum.DECIMAL;
    }

    @Override
    public String getHeatPositiveAsString() {
        return String.valueOf(heatPositive);
    }

    @Override
    public String getHeatNegativeAsString() {
        return String.valueOf(heatNegative);
    }
}
