package com.bearlycattable.bait.commons.serialization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.bearlycattable.bait.commons.enums.PubTypeEnum;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class CharacterMatchSearchResultDataDeserializerCustom extends StdDeserializer<CharacterMatchSearchResultData> {

    public CharacterMatchSearchResultDataDeserializerCustom() {
        super((JavaType) null); //uses Object.class
    }

    @Override
    public CharacterMatchSearchResultData deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        Map<String, Map<PubTypeEnum, List<String>>> results = new LinkedHashMap<>();

        Iterator<Map.Entry<String, JsonNode>> iter = node.fields();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> charsAsKey = iter.next();
            String chars = charsAsKey.getKey();
            results.put(chars, new HashMap<>());
            Iterator<Map.Entry<String, JsonNode>> iter2 = charsAsKey.getValue().fields();
            while(iter2.hasNext()) {
                Map.Entry<String, JsonNode> pubTypeAsKey = iter2.next();
                PubTypeEnum pubType = readResultType(pubTypeAsKey);
                results.get(chars).put(pubType, new ArrayList<>());
                ArrayNode ff = (ArrayNode) pubTypeAsKey.getValue();
                ff.forEach(str -> results.get(chars).get(pubType).add(str.asText()));
            }
        }

        CharacterMatchSearchResultData myObject = new CharacterMatchSearchResultData(results);
        return myObject;
    }

    private PubTypeEnum readResultType(Map.Entry<String, JsonNode> resultTypeEntry) {
        return PubTypeEnum.valueOf(resultTypeEntry.getKey());
    }
}
