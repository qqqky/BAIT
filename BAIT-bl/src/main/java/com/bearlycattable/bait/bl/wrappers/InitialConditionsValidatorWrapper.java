package com.bearlycattable.bait.bl.wrappers;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class InitialConditionsValidatorWrapper {

    private final String error;

    public boolean isValid() {
        return error == null || error.isEmpty();
    }
}
