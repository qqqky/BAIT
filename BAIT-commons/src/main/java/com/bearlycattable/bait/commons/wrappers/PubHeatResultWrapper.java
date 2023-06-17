package com.bearlycattable.bait.commons.wrappers;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PubHeatResultWrapper {

    private int heatPositive;
    private int heatNegative;

}
