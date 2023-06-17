package com.bearlycattable.bait.commons.serialization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.bearlycattable.bait.commons.enums.PubTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Getter;

@JsonInclude
@JsonDeserialize(using = CharacterMatchSearchResultDataDeserializerCustom.class)
public class CharacterMatchSearchResultData {

    @JsonIgnore
    @Getter
    private transient Map<String, Map<PubTypeEnum, List<String>>>  template = new HashMap<>();

    public CharacterMatchSearchResultData(Map<String, Map<PubTypeEnum, List<String>>>  template) {
        this.template = Objects.requireNonNull(template);
    }
}
