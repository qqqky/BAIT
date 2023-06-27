package com.bearlycattable.bait.bl.initializers.advancedTab;

import com.bearlycattable.bait.bl.controllers.advancedTab.AdvancedSubTabConfigController;
import com.bearlycattable.bait.bl.controllers.advancedTab.AdvancedTabMainController;
import com.bearlycattable.bait.bl.helpers.DarkModeHelper;

public final class AdvancedSubTabConfigControllerInitializer {

    private final AdvancedSubTabConfigController controller;
    private AdvancedTabMainController parentController;

    private AdvancedSubTabConfigControllerInitializer() {
        throw new UnsupportedOperationException("Creation of " + this.getClass().getName() + " directly is not allowed");
    }

    private AdvancedSubTabConfigControllerInitializer(AdvancedSubTabConfigController controller, AdvancedTabMainController parentController) {
        this.controller = controller;
        this.parentController = parentController;
    }

    public static void initialize(AdvancedSubTabConfigController controller, AdvancedTabMainController parentController) {
        if (controller == null || parentController == null) {
            throw new IllegalStateException("Suitable controller not found at AdvancedSubTabConfigControllerInitializer#initialize");
        }

        if (controller.isParentValid()) {
            return;
        }

        //parent controller must be set before initialization
        controller.setParentController(parentController);
        new AdvancedSubTabConfigControllerInitializer(controller, parentController).init();
    }

    private void init() {
        initializeCbxOverrideExactMatchPath();
        initializeCbxDarkMode();
    }

    private void initializeCbxOverrideExactMatchPath() {
        controller.getAdvancedConfigCbxOverrideExactMatchPath().setOnAction(event -> {
            boolean selected = controller.getAdvancedConfigCbxOverrideExactMatchPath().isSelected();
            controller.getAdvancedConfigBtnBrowseExactMatchPath().setDisable(!selected);
            controller.getAdvancedConfigTextFieldExactMatchPath().setDisable(!selected);
        });
    }

    private void initializeCbxDarkMode() {
        controller.getAdvancedConfigCbxDarkMode().setOnAction(event -> {
            DarkModeHelper.toggleDarkModeGlobal(controller.getAdvancedConfigCbxDarkMode().isSelected(), controller.getAdvancedConfigCbxDarkMode(), parentController);
        });
    }
}
