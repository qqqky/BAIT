package com.bearlycattable.bait.advancedCommons.serialization;

import java.io.File;
import java.io.IOException;

import com.bearlycattable.bait.advancedCommons.contexts.P2PKHSingleResultData;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SerializedSearchResultsReader {

    public static P2PKHSingleResultData[] deserializeExistingSearchResults(String pathToFile) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.getFactory().disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        P2PKHSingleResultData[] result = null;
        try {
            result = mapper.readValue(new File(pathToFile), P2PKHSingleResultData[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
