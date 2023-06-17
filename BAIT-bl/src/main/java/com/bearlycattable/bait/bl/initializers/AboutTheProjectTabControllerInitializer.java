package com.bearlycattable.bait.bl.initializers;

import com.bearlycattable.bait.bl.controllers.AboutTheProjectTabController;
import com.bearlycattable.bait.bl.controllers.HeatVisualizerController;
import com.bearlycattable.bait.bl.helpers.HeatVisualizerNumberedListHelper;

public class AboutTheProjectTabControllerInitializer {

    private final AboutTheProjectTabController controller;
    private HeatVisualizerController mainController;

    // private final Map<Integer, String> numberedLabels = new LinkedHashMap<>();
    // private final Map<String, String> markedTooltips = new LinkedHashMap<>();

    private AboutTheProjectTabControllerInitializer(AboutTheProjectTabController controller, HeatVisualizerController mainController) {
        this.controller = controller;
        this.mainController = mainController;
    }

    public static AboutTheProjectTabControllerInitializer getInitializer(AboutTheProjectTabController controller, HeatVisualizerController mainController) {
        return new AboutTheProjectTabControllerInitializer(controller, mainController);
    }

    public void initialize() {
        if (controller == null || mainController == null) {
            throw new IllegalStateException("Suitable controller not found at AboutTheProjectTabControllerInitializer#initialize");
        }

        String resource = "com.bearlycattable.bait.ui.txts/aboutTheProject.txt";
        HeatVisualizerNumberedListHelper.readFileAndInsertListToParentComponent(resource, controller.getAboutTheProjectVBoxListParent());
    }

}
