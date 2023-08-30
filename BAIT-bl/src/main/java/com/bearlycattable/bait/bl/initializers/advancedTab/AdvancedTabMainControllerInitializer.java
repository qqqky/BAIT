package com.bearlycattable.bait.bl.initializers.advancedTab;

import com.bearlycattable.bait.bl.controllers.AdvancedTabAccessProxy;
import com.bearlycattable.bait.bl.controllers.advancedTab.AdvancedTabMainController;

public final class AdvancedTabMainControllerInitializer {

    private final AdvancedTabMainController controller;

    private AdvancedTabMainControllerInitializer() {
        throw new UnsupportedOperationException("Creation of " + this.getClass().getName() + " directly is not allowed");
    }

    private AdvancedTabMainControllerInitializer(AdvancedTabMainController controller) {
        this.controller = controller;
    }

    public static void initialize(AdvancedTabMainController controller, AdvancedTabAccessProxy accessProxy) {
        if (controller == null || accessProxy == null) {
            throw new IllegalStateException("No suitable controller found at AdvancedTabPageControllerInitializer#initialize");
        }

        if (controller.isParentValid()) {
            return;
        }

        //root controller must be set before initialization
        controller.setAdvancedTabAccessProxy(accessProxy);

        new AdvancedTabMainControllerInitializer(controller).init();
    }

    private void init() {
        //empty
    }
}
