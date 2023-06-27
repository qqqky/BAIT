package com.bearlycattable.bait.bl.initializers.advancedTab;

import com.bearlycattable.bait.bl.controllers.advancedTab.AdvancedTabMainController;
import com.bearlycattable.bait.bl.controllers.RootController;
import com.bearlycattable.bait.bl.initializers.aboutTheProjectTab.AboutTheProjectTabControllerInitializer;

public final class AdvancedTabMainControllerInitializer {

    private final AdvancedTabMainController controller;
    private RootController rootController;

    private AdvancedTabMainControllerInitializer() {
        throw new UnsupportedOperationException("Creation of " + this.getClass().getName() + " directly is not allowed");
    }

    private AdvancedTabMainControllerInitializer(AdvancedTabMainController controller, RootController rootController) {
        this.controller = controller;
        this.rootController = rootController;
    }

    public static void initialize(AdvancedTabMainController controller, RootController rootController) {
        if (controller == null || rootController == null) {
            throw new IllegalStateException("No suitable controller found at AdvancedTabPageControllerInitializer#initialize");
        }

        if (controller.isParentValid()) {
            return;
        }

        //root controller must be set before initialization
        controller.setRootController(rootController);

        //advanced tab's helper must be initialized first (only for advanced tab)
        controller.initAdvancedTabPageHelper(rootController);
        new AdvancedTabMainControllerInitializer(controller, rootController).init();
    }

    private void init() {
        //empty
    }
}
