package com.bearlycattable.bait.bl.initializers;

import com.bearlycattable.bait.bl.controllers.ConverterTabController;
import com.bearlycattable.bait.bl.controllers.HeatVisualizerController;

public class ConverterTabControllerInitializer {

    private final ConverterTabController controller;
    private HeatVisualizerController mainController;

    private ConverterTabControllerInitializer(ConverterTabController controller, HeatVisualizerController mainController) {
        this.controller = controller;
        this.mainController = mainController;
    }

    public static ConverterTabControllerInitializer getInitializer(ConverterTabController controller, HeatVisualizerController mainController) {
        return new ConverterTabControllerInitializer(controller, mainController);
    }

    public void initialize() {
        if (controller == null || mainController == null) {
            throw new IllegalStateException("Suitable controller not found at ConverterTabControllerInitializer#initialize");
        }
        //do init listeners, etc...
    }
}
