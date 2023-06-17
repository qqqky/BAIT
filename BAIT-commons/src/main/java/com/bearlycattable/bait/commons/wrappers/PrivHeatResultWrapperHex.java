package com.bearlycattable.bait.commons.wrappers;

import com.bearlycattable.bait.commons.enums.NumberFormatTypeEnum;

import lombok.Builder;

@Builder
public class PrivHeatResultWrapperHex implements PrivHeatResultWrapper {

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

    public NumberFormatTypeEnum getType() {
        return NumberFormatTypeEnum.HEX;
    }

    @Override
    public String getHeatPositiveAsString() {
        return Long.toString(heatPositive, 16);
    }

    @Override
    public String getHeatNegativeAsString() {
        return Long.toString(heatNegative, 16);
    }

    // public boolean isValid() {
    //     return (heatPositive != null && !heatPositive.isEmpty())
    //             && (heatNegative != null && !heatNegative.isEmpty());
    // }
}
