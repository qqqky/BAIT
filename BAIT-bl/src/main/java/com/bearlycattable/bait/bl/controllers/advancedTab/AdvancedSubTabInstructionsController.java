package com.bearlycattable.bait.bl.controllers.advancedTab;

import java.util.Objects;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import lombok.Getter;

public class AdvancedSubTabInstructionsController {

    @FXML
    @Getter
    private ScrollPane advancedInstructionsScrollPaneParent;

    private AdvancedTabMainController parentController;

    @FXML
    void initialize() {
        System.out.println("CREATING (child): AdvancedSubTabInstructionsController......");
    }

    public void setParentController(AdvancedTabMainController parentController) {
        this.parentController = Objects.requireNonNull(parentController);
    }

    public final boolean isParentValid() {
        return parentController != null;
    }

    @FXML
    private void doGoToAdvancedSearchTab() {
        parentController.switchToChildTabX(0);
    }

}
