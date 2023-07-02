package com.bearlycattable.bait.bl.initializers.converterTab;

import com.bearlycattable.bait.bl.controllers.converterTab.ConverterTabController;
import com.bearlycattable.bait.bl.controllers.RootController;
import com.bearlycattable.bait.bl.helpers.HeatVisualizerFormatterFactory;

public final class ConverterTabControllerInitializer {

    private final ConverterTabController controller;
    private RootController rootController;

    private ConverterTabControllerInitializer() {
        throw new UnsupportedOperationException("Creation of " + this.getClass().getName() + " directly is not allowed");
    }

    private ConverterTabControllerInitializer(ConverterTabController controller, RootController rootController) {
        this.controller = controller;
        this.rootController = rootController;
    }

    public static void initialize(ConverterTabController controller, RootController rootController) {
        if (controller == null || rootController == null) {
            throw new IllegalStateException("Suitable controller not found at ConverterTabControllerInitializer#initialize");
        }

        if (controller.isParentValid()) {
            return;
        }

        //root controller must be set before initialization
        controller.setRootController(rootController);
        new ConverterTabControllerInitializer(controller, rootController).init();
    }

    private void init() {
        controller.getConverterConversionTextFieldUnencodedPub().setTextFormatter(HeatVisualizerFormatterFactory.getDefaultUnencodedPublicKeyFormatter());
        controller.getConverterConversionTextFieldEncodedPub().setTextFormatter(HeatVisualizerFormatterFactory.getDefaultEncodedPublicKeyFormatter());
        controller.getConverterWIFTextFieldPriv().setTextFormatter(HeatVisualizerFormatterFactory.getDefaultPrivateKeyFormatter());
    }
}
