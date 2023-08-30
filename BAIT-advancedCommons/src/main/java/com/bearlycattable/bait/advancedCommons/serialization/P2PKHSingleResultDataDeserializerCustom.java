package com.bearlycattable.bait.advancedCommons.serialization;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.bearlycattable.bait.advancedCommons.contexts.P2PKHSingleResultData;
import com.bearlycattable.bait.commons.enums.JsonResultScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.JsonResultTypeEnum;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import javafx.util.Pair;

public class P2PKHSingleResultDataDeserializerCustom extends StdDeserializer<P2PKHSingleResultData> {

    private static final long serialVersionUID = 8650412940587951497L;

    public P2PKHSingleResultDataDeserializerCustom() {
        super((JavaType) null); //uses Object.class, should not need a cast...
    }

    @Override
    public P2PKHSingleResultData deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        String hash = node.get("hash").textValue();
        JsonNode parentResultNode = node.get("results");
        Map<JsonResultTypeEnum, Map<JsonResultScaleFactorEnum, Pair<String, Integer>>> results = new LinkedHashMap<>();

        Iterator<Map.Entry<String, JsonNode>> jsonResultTypes = parentResultNode.fields();
        while (jsonResultTypes.hasNext()) {
            Map.Entry<String, JsonNode> resultTypeEntry = jsonResultTypes.next();
            JsonResultTypeEnum resultType = readResultType(resultTypeEntry);

            Map<JsonResultScaleFactorEnum, Pair<String, Integer>> singleResultDataMap = new LinkedHashMap<>();

            Iterator<Map.Entry<String, JsonNode>> jsonScaleFactorTypes = resultTypeEntry.getValue().fields();
            while (jsonScaleFactorTypes.hasNext()) {
                Map.Entry<String, JsonNode> scaleFactorEntry = jsonScaleFactorTypes.next();
                JsonResultScaleFactorEnum resultScaleFactor = readScaleFactor(scaleFactorEntry);

                Iterator<Map.Entry<String, JsonNode>> pairData = scaleFactorEntry.getValue().fields();
                String priv = scaleFactorEntry.getValue().get("key").textValue();
                Integer i = scaleFactorEntry.getValue().get("value").intValue();
                singleResultDataMap.put(resultScaleFactor, new Pair<>(priv, i));
            }
            results.put(resultType, singleResultDataMap);
        }
        P2PKHSingleResultData myObject = new P2PKHSingleResultData(hash, results);
        return myObject;
    }

    private JsonResultTypeEnum readResultType(Map.Entry<String, JsonNode> resultTypeEntry) {
        return JsonResultTypeEnum.valueOf(resultTypeEntry.getKey());
    }

    private JsonResultScaleFactorEnum readScaleFactor(Map.Entry<String, JsonNode> scaleFactorEntry) {
        return JsonResultScaleFactorEnum.valueOf(scaleFactorEntry.getKey());
    }

    private Pair<String, Integer> makePair(Map.Entry<String, JsonNode> pairNode) {
        String priv = pairNode.getKey();
        Integer accuracy = Integer.parseInt(pairNode.getValue().textValue());
        return new Pair<>(priv, accuracy);
    }
}
