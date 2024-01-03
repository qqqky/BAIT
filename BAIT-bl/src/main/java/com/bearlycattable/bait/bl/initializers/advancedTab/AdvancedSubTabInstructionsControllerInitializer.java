package com.bearlycattable.bait.bl.initializers.advancedTab;

import com.bearlycattable.bait.bl.controllers.advancedTab.AdvancedSubTabInstructionsController;
import com.bearlycattable.bait.bl.controllers.advancedTab.AdvancedTabMainController;
import com.bearlycattable.bait.bl.helpers.BaitNumberedListHelper;

import javafx.scene.layout.VBox;

public final class AdvancedSubTabInstructionsControllerInitializer {

    private final AdvancedSubTabInstructionsController controller;

    private AdvancedSubTabInstructionsControllerInitializer() {
        throw new UnsupportedOperationException("Creation of " + this.getClass().getName() + " directly is not allowed");
    }

    private AdvancedSubTabInstructionsControllerInitializer(AdvancedSubTabInstructionsController controller) {
        this.controller = controller;
    }

    public static void initialize(AdvancedSubTabInstructionsController controller, AdvancedTabMainController parentController) {
        if (controller == null || parentController == null) {
            throw new IllegalStateException("Suitable controller not found at AdvancedSubTabInstructionsControllerInitializer#initialize");
        }

        if (controller.isParentValid()) {
            return;
        }

        //parent controller must be set before initialization
        controller.setAdvancedInstructionsAccessProxy(parentController);

        new AdvancedSubTabInstructionsControllerInitializer(controller).init();
    }

    private void init() {
        String resource = "com.bearlycattable.bait.ui.txts/advancedInstructions.txt";
        VBox content = BaitNumberedListHelper.readFileAndInsertListToParentComponent(resource, new VBox());

        controller.getAdvancedInstructionsScrollPaneParent().setContent(content);
    }

}

