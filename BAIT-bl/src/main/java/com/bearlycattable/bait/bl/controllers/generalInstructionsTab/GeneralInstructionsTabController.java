package com.bearlycattable.bait.bl.controllers.generalInstructionsTab;

import com.bearlycattable.bait.bl.controllers.GeneralInstructionsTabAccessProxy;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import lombok.Getter;

/*
    IMPORTANT: for CONTROLLER to be properly injected @FXML, it MUST MATCH the following formula:
    FXMLLoader takes 'fx:id' of THE INCLUDE and appends word 'Controller' - so your injectable field MUST MATCH IT!

    Read more at FXMLLoader.java: public Object constructValue() throws IOException
    This part: if (fx_id != null) {
                String id = this.fx_id + CONTROLLER_SUFFIX;
                Object controller = fxmlLoader.getController();

                namespace.put(id, controller);
                injectFields(id, controller);
            }
     */
public class GeneralInstructionsTabController {

    @FXML
    @Getter
    private ScrollPane instructionsScrollPaneParent;

    private GeneralInstructionsTabAccessProxy generalInstructionsTabAccessProxy;

    @FXML
    void initialize() {
        System.out.println("CREATING: GeneralInstructionsTabController......");
    }

    public void setGeneralInstructionsTabAccessProxy(GeneralInstructionsTabAccessProxy proxy) {
        this.generalInstructionsTabAccessProxy = proxy;
    }

    @FXML
    private void doGoToQuickSearchTab(ActionEvent actionEvent) {
        generalInstructionsTabAccessProxy.switchToParentTabX(2);
    }

    public final boolean isParentValid() {
        return generalInstructionsTabAccessProxy != null;
    }

}
