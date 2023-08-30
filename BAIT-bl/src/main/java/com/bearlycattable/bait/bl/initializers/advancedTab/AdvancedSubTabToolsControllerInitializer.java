package com.bearlycattable.bait.bl.initializers.advancedTab;

import com.bearlycattable.bait.bl.controllers.advancedTab.AdvancedSubTabToolsController;
import com.bearlycattable.bait.bl.controllers.advancedTab.AdvancedTabMainController;

public class AdvancedSubTabToolsControllerInitializer {

    private final AdvancedSubTabToolsController controller;

    private AdvancedSubTabToolsControllerInitializer() {
        throw new UnsupportedOperationException("Creation of " + this.getClass().getName() + " directly is not allowed");
    }

    private AdvancedSubTabToolsControllerInitializer(AdvancedSubTabToolsController controller) {
        this.controller = controller;
    }

    public static void initialize(AdvancedSubTabToolsController controller, AdvancedTabMainController parentController) {
        if (controller == null || parentController == null) {
            throw new IllegalStateException("Suitable controller not found at AdvancedSubTabLogControllerInitializer#initialize");
        }

        if (controller.isParentValid()) {
            return;
        }

        //only set implementations we need
        controller.setAdvancedToolsAccessProxy(parentController);

        new AdvancedSubTabToolsControllerInitializer(controller).init();
    }

    private void init() {
        //empty
    }
}
