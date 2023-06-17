package com.bearlycattable.bait.bl.initializers;

import com.bearlycattable.bait.bl.controllers.AdvancedSubTabProgressController;
import com.bearlycattable.bait.bl.controllers.AdvancedTabMainController;

public class AdvancedSubTabProgressControllerInitializer {

    private final AdvancedSubTabProgressController controller;
    private AdvancedTabMainController parentController;

    private AdvancedSubTabProgressControllerInitializer(AdvancedSubTabProgressController controller, AdvancedTabMainController parentController) {
        this.controller = controller;
        this.parentController = parentController;
    }

    public static AdvancedSubTabProgressControllerInitializer getInitializer(AdvancedSubTabProgressController controller, AdvancedTabMainController parentController) {
        return new AdvancedSubTabProgressControllerInitializer(controller, parentController);
    }

    public void initialize() {
        if (controller == null || parentController == null) {
            throw new IllegalStateException("Suitable controller not found at AdvancedSubTabProgressControllerInitializer#initialize");
        }

        controller.getAdvancedProgressHBoxAutomergePathParent().setDisable(true);
        addAutomergeCbxListener();
    }

    private void addAutomergeCbxListener() {
        controller.getAdvancedProgressCbxEnableAutomerge().setOnAction(event -> {
            controller.getAdvancedProgressHBoxAutomergePathParent().setDisable(!controller.getAdvancedProgressCbxEnableAutomerge().isSelected());
        });
    }
}
