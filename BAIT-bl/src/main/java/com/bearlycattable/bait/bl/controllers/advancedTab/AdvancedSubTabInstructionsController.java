package com.bearlycattable.bait.bl.controllers.advancedTab;

import java.util.Objects;

import com.bearlycattable.bait.bl.controllers.advancedTab.proxyInterfaces.AdvancedInstructionsAccessProxy;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import lombok.Getter;

public class AdvancedSubTabInstructionsController {

    @FXML
    @Getter
    private ScrollPane advancedInstructionsScrollPaneParent;

    private AdvancedInstructionsAccessProxy advancedInstructionsAccessProxy;

    @FXML
    void initialize() {
        System.out.println("CREATING (child, advanced): AdvancedSubTabInstructionsController......");
    }

    public void setAdvancedInstructionsAccessProxy(AdvancedInstructionsAccessProxy proxy) {
        this.advancedInstructionsAccessProxy = Objects.requireNonNull(proxy);
    }

    public final boolean isParentValid() {
        return advancedInstructionsAccessProxy != null;
    }

    @FXML
    private void doGoToAdvancedSearchTab() {
        advancedInstructionsAccessProxy.switchToChildTabX(0);
    }

}
