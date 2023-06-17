package com.bearlycattable.bait.bl.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import lombok.Getter;

public class AboutTheProjectTabController {

    @FXML
    @Getter
    private VBox aboutTheProjectVBoxListParent;
    private HeatVisualizerController mainController;

    @FXML
    void initialize() {
        System.out.println("CREATING: AboutTheProjectTabController......");
    }

    void setMainController(HeatVisualizerController mainController) {
        this.mainController = mainController;
    }
}
