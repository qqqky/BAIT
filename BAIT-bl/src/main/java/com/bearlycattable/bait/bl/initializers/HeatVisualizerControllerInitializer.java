package com.bearlycattable.bait.bl.initializers;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.bearlycattable.bait.bl.controllers.HeatVisualizerController;
import com.bearlycattable.bait.commons.CssConstants;
import com.bearlycattable.bait.commons.HeatVisualizerConstants;

/**
 * Class that holds initialization logic for HeatVisualizerController
 */
public final class HeatVisualizerControllerInitializer {

    private final HeatVisualizerController controller;

    private HeatVisualizerControllerInitializer(HeatVisualizerController controller) {
        this.controller = controller;
    }

    public static HeatVisualizerControllerInitializer getInitializer(HeatVisualizerController controller) {
        return new HeatVisualizerControllerInitializer(controller);
    }

    public void initialize() {
        if (controller == null) {
            throw new IllegalStateException("Suitable controller not found at HeatVisualizerControllerInitializer#initialize");
        }

        initializeMappings();
    }

    private void initializeMappings() {
        initializeColorMappings();
        initializeSimilarityMappings();
    }

    private void initializeColorMappings() {
        for (int i = 0; i < CssConstants.COLOR_SHADES_CSS.size(); i++) {
            controller.getColorMappings().put(i, CssConstants.COLOR_SHADES_CSS.get(i));
        }
    }

    private void initializeSimilarityMappings() {
        int max = 16 * 40;
        for (int i = 0; i <= max; i++) {
            controller.getSimilarityMappings().put(i, HeatVisualizerConstants.SINGLE_POINT_VALUE.multiply(new BigDecimal(i)));
        }
    }
}
