package com.bearlycattable.bait.bl.searchHelper;

import com.bearlycattable.bait.commons.HeatVisualizerConstants;
import com.bearlycattable.bait.commons.contexts.QuickSearchContext;

public class SearchHelperUtils {

    public static String determineInitialSeed(QuickSearchContext context) {
        String seed = context.getSeed();
        switch (context.getType()) {
            case COLLISION:
                if (seed == null || seed.isEmpty()) {
                    System.out.println("Seed not specified. Will use targetPriv as seed: " + context.getTargetPriv());
                    return context.getTargetPriv();
                }

                if (!HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(seed).matches()) {
                    System.out.println("Invalid seed. Will use targetPriv as seed: " + context.getTargetPriv());
                    return context.getTargetPriv();
                }
                return seed;
            case BLIND:
                return seed;
            default:
                throw new IllegalArgumentException("This QuickSearch type is not supported at SearchHelperUtils#determineInitialSeed [type=" + context.getType() + "]");
        }
    }
}
