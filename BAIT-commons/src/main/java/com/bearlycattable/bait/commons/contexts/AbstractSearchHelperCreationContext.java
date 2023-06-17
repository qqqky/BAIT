package com.bearlycattable.bait.commons.contexts;

import java.math.BigDecimal;
import java.util.Map;

import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public abstract class AbstractSearchHelperCreationContext {

    @Setter
    private int iterations;
    private final int accuracy;
    private final ScaleFactorEnum scaleFactor;
    private final Map<Integer, BigDecimal> similarityMappings;
}
