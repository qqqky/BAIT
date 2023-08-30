package com.bearlycattable.bait.bl.initializers.aboutTheProjectTab;

import com.bearlycattable.bait.bl.controllers.aboutTheProjectTab.AboutTheProjectTabController;
import com.bearlycattable.bait.bl.controllers.RootController;
import com.bearlycattable.bait.bl.helpers.HeatVisualizerNumberedListHelper;

public final class AboutTheProjectTabControllerInitializer {

    private final AboutTheProjectTabController controller;

    private AboutTheProjectTabControllerInitializer() {
        throw new UnsupportedOperationException("Creation of " + this.getClass().getName() + " directly is not allowed");
    }

    private AboutTheProjectTabControllerInitializer(AboutTheProjectTabController controller) {
        this.controller = controller;
    }

    public static void initialize(AboutTheProjectTabController controller, RootController rootController) {
        if (controller == null || rootController == null) {
            throw new IllegalStateException("Suitable controller not found at AboutTheProjectTabControllerInitializer#initialize");
        }

        if (controller.isParentValid()) {
            return;
        }

        //root controller must be set before initialization
        controller.setAboutTheProjectTabAccessProxy(rootController);

        new AboutTheProjectTabControllerInitializer(controller).init();
    }

    private void init() {
        String resource = "com.bearlycattable.bait.ui.txts/aboutTheProject.txt";
        HeatVisualizerNumberedListHelper.readFileAndInsertListToParentComponent(resource, controller.getAboutTheProjectVBoxListParent());
    }

}
