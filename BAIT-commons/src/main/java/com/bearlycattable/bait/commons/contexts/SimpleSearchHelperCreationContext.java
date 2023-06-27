package com.bearlycattable.bait.commons.contexts;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class SimpleSearchHelperCreationContext extends AbstractSearchHelperCreationContext {
    private final Map<Integer, BigDecimal> similarityMappings;
    private String prefix; //only used by some random modes that allow prefix

    public boolean isEmpty() {
        return Objects.isNull(getScaleFactor()) && Objects.isNull(similarityMappings) && getIterations() == 0 && getAccuracy() == 0
                && Objects.isNull(prefix);
    }
}
