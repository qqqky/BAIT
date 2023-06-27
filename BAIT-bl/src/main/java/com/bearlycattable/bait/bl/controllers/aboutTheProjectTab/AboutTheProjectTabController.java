package com.bearlycattable.bait.bl.controllers.aboutTheProjectTab;

import com.bearlycattable.bait.bl.controllers.RootController;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import lombok.Getter;

public class AboutTheProjectTabController {

    @FXML
    @Getter
    private VBox aboutTheProjectVBoxListParent;
    private RootController rootController;

    @FXML
    void initialize() {
        System.out.println("CREATING: AboutTheProjectTabController......");
    }

    public void setRootController(RootController rootController) {
        this.rootController = rootController;
    }

    public final boolean isParentValid() {
        return rootController != null;
    }
}
