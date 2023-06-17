package com.bearlycattable.bait.bl.controllers;

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
    // @FXML
    // private Button instructionsBtnGoToQuickSearchTab;
    private HeatVisualizerController mainController;

    @FXML
    void initialize() {
        System.out.println("CREATING: GeneralInstructionsTabController......");
    }

    void setMainController(HeatVisualizerController mainController) {
        this.mainController = mainController;
    }

    // public void addGeneralInstructions(Map<Integer, String> itemsForComponents) {
    //     itemsForComponents.keySet().stream().forEach(key -> {
    //         String value = key + ". " + itemsForComponents.get(key);
    //
    //         HBox hbox = new HBox();
    //         hbox.getChildren().add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(5, true));
    //         Label label = new Label();
    //         label.setText(value);
    //         label.setStyle("-fx-font-size:18;");
    //         label.setWrapText(true);
    //         hbox.getChildren().add(label);
    //         instructionsVBoxListParent.getChildren().add(hbox);
    //     });
    // }

    @FXML
    private void doGoToQuickSearchTab(ActionEvent actionEvent) {
        mainController.switchToTab(2);
    }

}
