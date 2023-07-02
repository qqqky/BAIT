package com.bearlycattable.bait.bl.wrappers;

import com.bearlycattable.bait.commons.enums.RandomWordPrefixMutationTypeEnum;

import javafx.util.Pair;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PrefixedModeConfigsWrapper {

    private final String prefix;
    private final Pair<RandomWordPrefixMutationTypeEnum, String> modeSpecificConfigs;
    private final String error;

    public boolean hasValidConfig() {
        return error == null || error.isEmpty();
    }
}
