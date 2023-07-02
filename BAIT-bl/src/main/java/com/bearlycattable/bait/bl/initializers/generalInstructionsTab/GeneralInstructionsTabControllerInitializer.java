package com.bearlycattable.bait.bl.initializers.generalInstructionsTab;

import com.bearlycattable.bait.bl.controllers.generalInstructionsTab.GeneralInstructionsTabController;
import com.bearlycattable.bait.bl.controllers.RootController;
import com.bearlycattable.bait.bl.helpers.HeatVisualizerNumberedListHelper;

import javafx.scene.layout.VBox;

public final class GeneralInstructionsTabControllerInitializer {

    private final GeneralInstructionsTabController controller;
    private RootController rootController;

    private GeneralInstructionsTabControllerInitializer() {
        throw new UnsupportedOperationException("Creation of " + this.getClass().getName() + " directly is not allowed");
    }

    private GeneralInstructionsTabControllerInitializer(GeneralInstructionsTabController controller, RootController rootController) {
        this.controller = controller;
        this.rootController = rootController;
    }

    public static void initialize(GeneralInstructionsTabController controller, RootController rootController) {
        if (controller == null || rootController == null) {
            throw new IllegalStateException("Suitable controller not found at GeneralInstructionsTabControllerInitializer#initialize");
        }

        if (controller.isParentValid()) {
            return;
        }

        //root controller must be set before initialization
        controller.setRootController(rootController);
        new GeneralInstructionsTabControllerInitializer(controller, rootController).init();
    }

    private void init() {
        String resource = "com.bearlycattable.bait.ui.txts/generalInstructions.txt";
        VBox content = HeatVisualizerNumberedListHelper.readFileAndInsertListToParentComponent(resource, new VBox());

        controller.getInstructionsScrollPaneParent().setContent(content);
    }
}
