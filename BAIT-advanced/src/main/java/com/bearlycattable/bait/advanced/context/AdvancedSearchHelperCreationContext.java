package com.bearlycattable.bait.advanced.context;

import java.util.Objects;

import com.bearlycattable.bait.commons.contexts.AbstractSearchHelperCreationContext;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class AdvancedSearchHelperCreationContext extends AbstractSearchHelperCreationContext {

    private final boolean exactMatchCheckEnabled;
    private String prefix; //only used by some random modes that allow prefix

    public boolean isEmpty() {
        return Objects.isNull(getScaleFactor()) && Objects.isNull(getSimilarityMappings()) && getIterations() == 0 && getAccuracy() == 0
                && Objects.isNull(prefix);
    }
}
