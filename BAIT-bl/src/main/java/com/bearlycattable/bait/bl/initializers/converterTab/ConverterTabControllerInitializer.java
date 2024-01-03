package com.bearlycattable.bait.bl.initializers.converterTab;

import com.bearlycattable.bait.bl.controllers.converterTab.ConverterTabController;
import com.bearlycattable.bait.bl.controllers.RootController;
import com.bearlycattable.bait.bl.helpers.BaitFormatterFactory;

public final class ConverterTabControllerInitializer {

    private final ConverterTabController controller;

    private ConverterTabControllerInitializer() {
        throw new UnsupportedOperationException("Creation of " + this.getClass().getName() + " directly is not allowed");
    }

    private ConverterTabControllerInitializer(ConverterTabController controller) {
        this.controller = controller;
    }

    public static void initialize(ConverterTabController controller, RootController rootController) {
        if (controller == null || rootController == null) {
            throw new IllegalStateException("Suitable controller not found at ConverterTabControllerInitializer#initialize");
        }

        if (controller.isParentValid()) {
            return;
        }

        //root controller must be set before initialization
        controller.setConverterTabAccessProxy(rootController);

        new ConverterTabControllerInitializer(controller).init();
    }

    private void init() {
        controller.getConverterConversionTextFieldUnencodedPub().setTextFormatter(BaitFormatterFactory.getDefaultUnencodedPublicKeyFormatter());
        controller.getConverterConversionTextFieldEncodedPub().setTextFormatter(BaitFormatterFactory.getDefaultEncodedPublicKeyFormatter());
        controller.getConverterWIFTextFieldPriv().setTextFormatter(BaitFormatterFactory.getDefaultPrivateKeyFormatter());
    }
}
