package com.bearlycattable.bait.bl.controllers.aboutTheProjectTab;

import com.bearlycattable.bait.bl.controllers.AboutTheProjectTabAccessProxy;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import lombok.Getter;

public class AboutTheProjectTabController {

    @FXML
    @Getter
    private VBox aboutTheProjectVBoxListParent;
    private AboutTheProjectTabAccessProxy aboutTheProjectTabAccessProxy;

    @FXML
    void initialize() {
        System.out.println("CREATING: AboutTheProjectTabController......");
    }

    public void setAboutTheProjectTabAccessProxy(AboutTheProjectTabAccessProxy proxy) {
        this.aboutTheProjectTabAccessProxy = proxy;
    }

    public final boolean isParentValid() {
        return aboutTheProjectTabAccessProxy != null;
    }
}
