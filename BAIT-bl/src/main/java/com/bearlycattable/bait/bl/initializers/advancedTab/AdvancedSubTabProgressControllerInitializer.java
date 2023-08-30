package com.bearlycattable.bait.bl.initializers.advancedTab;

import com.bearlycattable.bait.bl.controllers.advancedTab.AdvancedSubTabProgressController;
import com.bearlycattable.bait.bl.controllers.advancedTab.AdvancedTabMainController;

public final class AdvancedSubTabProgressControllerInitializer {

    private final AdvancedSubTabProgressController controller;

    private AdvancedSubTabProgressControllerInitializer() {
        throw new UnsupportedOperationException("Creation of " + this.getClass().getName() + " directly is not allowed");
    }

    private AdvancedSubTabProgressControllerInitializer(AdvancedSubTabProgressController controller) {
        this.controller = controller;
    }

    public static void initialize(AdvancedSubTabProgressController controller, AdvancedTabMainController parentController) {
        if (controller == null || parentController == null) {
            throw new IllegalStateException("Suitable controller not found at AdvancedSubTabProgressControllerInitializer#initialize");
        }

        if (controller.isParentValid()) {
            return;
        }

        //parent controller must be set before initialization
        controller.setAdvancedProgressAccessProxy(parentController);

        new AdvancedSubTabProgressControllerInitializer(controller).init();
    }

    private void init() {
        controller.getAdvancedProgressHBoxAutomergePathParent().setDisable(true);
        addAutomergeCbxListener();
    }

    private void addAutomergeCbxListener() {
        controller.getAdvancedProgressCbxEnableAutomerge().setOnAction(event -> {
            controller.getAdvancedProgressHBoxAutomergePathParent().setDisable(!controller.getAdvancedProgressCbxEnableAutomerge().isSelected());
        });
    }
}
