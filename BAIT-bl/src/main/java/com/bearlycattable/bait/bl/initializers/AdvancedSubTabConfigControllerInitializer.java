package com.bearlycattable.bait.bl.initializers;

import com.bearlycattable.bait.bl.controllers.AdvancedSubTabConfigController;
import com.bearlycattable.bait.bl.controllers.AdvancedTabMainController;
import com.bearlycattable.bait.bl.helpers.DarkModeHelper;

public class AdvancedSubTabConfigControllerInitializer {

    private final AdvancedSubTabConfigController controller;
    private AdvancedTabMainController parentController;

    private AdvancedSubTabConfigControllerInitializer() {
        throw new UnsupportedOperationException("Creation of " +  this.getClass().getName() + " directly is not allowed");
    }

    private AdvancedSubTabConfigControllerInitializer(AdvancedSubTabConfigController controller, AdvancedTabMainController parentController) {
        this.controller = controller;
        this.parentController = parentController;
    }

    public static AdvancedSubTabConfigControllerInitializer getInitializer(AdvancedSubTabConfigController controller, AdvancedTabMainController parentController) {
        return new AdvancedSubTabConfigControllerInitializer(controller, parentController);
    }

    public void initialize() {
        if (controller == null || parentController == null) {
            throw new IllegalStateException("Suitable controller not found at AdvancedSubTabConfigControllerInitializer#initialize");
        }

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
