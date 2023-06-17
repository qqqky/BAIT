package com.bearlycattable.bait.bl.initializers;

import com.bearlycattable.bait.bl.controllers.AdvancedSubTabInstructionsController;
import com.bearlycattable.bait.bl.controllers.AdvancedTabMainController;
import com.bearlycattable.bait.bl.helpers.HeatVisualizerNumberedListHelper;

import javafx.scene.layout.VBox;

public class AdvancedSubTabInstructionsControllerInitializer {

    private final AdvancedSubTabInstructionsController controller;
    private AdvancedTabMainController mainController;

    // private final Map<Integer, String> numberedLabels = new LinkedHashMap<>();
    // private final Map<String, String> markedTooltips = new LinkedHashMap<>();

    private AdvancedSubTabInstructionsControllerInitializer(AdvancedSubTabInstructionsController controller, AdvancedTabMainController mainController) {
        this.controller = controller;
        this.mainController = mainController;
    }

    public static AdvancedSubTabInstructionsControllerInitializer getInitializer(AdvancedSubTabInstructionsController controller, AdvancedTabMainController mainController) {
        return new AdvancedSubTabInstructionsControllerInitializer(controller, mainController);
    }

    public void initialize() {
        if (controller == null || mainController == null) {
            throw new IllegalStateException("Suitable controller not found at AdvancedSubTabInstructionsControllerInitializer#initialize");
        }

        String resource = "com.bearlycattable.bait.ui.txts/advancedInstructions.txt";
        VBox content = HeatVisualizerNumberedListHelper.readFileAndInsertListToParentComponent(resource, new VBox());

        controller.getAdvancedInstructionsScrollPaneParent().setContent(content);
    }

}

