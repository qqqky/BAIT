package com.bearlycattable.bait.bl.initializers;

import java.math.BigDecimal;

import com.bearlycattable.bait.bl.controllers.RootController;
import com.bearlycattable.bait.commons.CssConstants;
import com.bearlycattable.bait.commons.HeatVisualizerConstants;

/**
 * Class that holds initialization logic for RootController
 */
public final class RootControllerInitializer {

    private final RootController rootController;

    private RootControllerInitializer(RootController rootController) {
        this.rootController = rootController;
    }

    public static void initialize(RootController rootController) {
        new RootControllerInitializer(rootController).init();
    }

    private void init() {
        if (rootController == null) {
            throw new IllegalStateException("Suitable controller not found at RootControllerInitializer#initialize");
        }

        initializeMappings();
    }

    private void initializeMappings() {
        initializeColorMappings();
        initializeSimilarityMappings();
    }

    private void initializeColorMappings() {
        for (int i = 0; i < CssConstants.COLOR_SHADES_CSS.size(); i++) {
            rootController.getColorMappings().put(i, CssConstants.COLOR_SHADES_CSS.get(i));
        }
    }

    private void initializeSimilarityMappings() {
        int max = 16 * 40;
        for (int i = 0; i <= max; i++) {
            rootController.getSimilarityMappings().put(i, HeatVisualizerConstants.SINGLE_POINT_VALUE.multiply(new BigDecimal(i)));
        }
    }
}
