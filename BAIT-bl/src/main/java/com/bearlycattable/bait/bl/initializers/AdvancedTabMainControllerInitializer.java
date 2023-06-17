package com.bearlycattable.bait.bl.initializers;

import com.bearlycattable.bait.bl.controllers.AdvancedTabMainController;
import com.bearlycattable.bait.bl.controllers.HeatVisualizerController;

public class AdvancedTabMainControllerInitializer {

    private final AdvancedTabMainController controller;
    private HeatVisualizerController mainController;

    private AdvancedTabMainControllerInitializer(AdvancedTabMainController controller, HeatVisualizerController mainController) {
        this.controller = controller;
        this.mainController = mainController;
    }

    public static AdvancedTabMainControllerInitializer getInitializer(AdvancedTabMainController controller, HeatVisualizerController mainController) {
        return new AdvancedTabMainControllerInitializer(controller, mainController);
    }

    public void initialize() {
        if (controller == null || mainController == null) {
            throw new IllegalStateException("No suitable controller found at AdvancedTabPageControllerInitializer#initialize");
        }
    }
}
