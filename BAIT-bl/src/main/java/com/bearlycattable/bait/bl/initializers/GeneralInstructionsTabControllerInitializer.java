package com.bearlycattable.bait.bl.initializers;

import com.bearlycattable.bait.bl.controllers.GeneralInstructionsTabController;
import com.bearlycattable.bait.bl.controllers.HeatVisualizerController;
import com.bearlycattable.bait.bl.helpers.HeatVisualizerNumberedListHelper;

import javafx.scene.layout.VBox;

public class GeneralInstructionsTabControllerInitializer {

    private final GeneralInstructionsTabController controller;
    private HeatVisualizerController mainController;

    private GeneralInstructionsTabControllerInitializer(GeneralInstructionsTabController controller, HeatVisualizerController mainController) {
        this.controller = controller;
        this.mainController = mainController;
    }

    public static GeneralInstructionsTabControllerInitializer getInitializer(GeneralInstructionsTabController controller, HeatVisualizerController mainController) {
        return new GeneralInstructionsTabControllerInitializer(controller, mainController);
    }

    public void initialize() {
        if (controller == null || mainController == null) {
            throw new IllegalStateException("Suitable controller not found at GeneralInstructionsTabControllerInitializer#initialize");
        }

        String resource = "com.bearlycattable.bait.ui.txts/generalInstructions.txt";
        VBox content = HeatVisualizerNumberedListHelper.readFileAndInsertListToParentComponent(resource, new VBox());

        controller.getInstructionsScrollPaneParent().setContent(content);
    }
}
