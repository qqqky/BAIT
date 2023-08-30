package com.bearlycattable.bait.utility;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.bearlycattable.bait.commons.CssConstants;
import com.bearlycattable.bait.commons.HeatVisualizerConstants;

public class BaitUtils {

    public static Map<Integer, BigDecimal> buildSimilarityMappings() {
        Map<Integer, BigDecimal> map = new HashMap<>();
        int max = 16 * 40;
        for (int i = 0; i <= max; i++) {
            map.put(i, HeatVisualizerConstants.SINGLE_POINT_VALUE.multiply(new BigDecimal(i)));
        }

        return map;
    }

    public static Map<Integer, String> buildColorMappings() {
        Map<Integer, String> map = new HashMap<>();
        for (int i = 0; i < CssConstants.COLOR_SHADES_CSS.size(); i++) {
            map.put(i, CssConstants.COLOR_SHADES_CSS.get(i));
        }
        return map;
    }
}
