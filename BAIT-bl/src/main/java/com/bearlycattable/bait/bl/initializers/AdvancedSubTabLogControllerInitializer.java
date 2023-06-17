package com.bearlycattable.bait.bl.initializers;

import com.bearlycattable.bait.bl.controllers.AdvancedSubTabLogController;
import com.bearlycattable.bait.bl.controllers.AdvancedTabMainController;
import com.bearlycattable.bait.bl.handlersAndListeners.RightClickHandlerEnum;
import com.bearlycattable.bait.commons.enums.LogTextTypeEnum;
import com.bearlycattable.bait.utility.logUtils.LogText;

import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.text.Font;

public class AdvancedSubTabLogControllerInitializer {

    private final AdvancedSubTabLogController controller;
    private AdvancedTabMainController parentController;

    private AdvancedSubTabLogControllerInitializer() {
        throw new UnsupportedOperationException("Creation of " +  this.getClass().getName() + " directly is not allowed");
    }

    private AdvancedSubTabLogControllerInitializer(AdvancedSubTabLogController controller, AdvancedTabMainController parentController) {
        this.controller = controller;
        this.parentController = parentController;
    }

    public static AdvancedSubTabLogControllerInitializer getInitializer(AdvancedSubTabLogController controller, AdvancedTabMainController parentController) {
        return new AdvancedSubTabLogControllerInitializer(controller, parentController);
    }

    public void initialize() {
        if (controller == null || parentController == null) {
            throw new IllegalStateException("Suitable controller not found at AdvancedSubTabLogControllerInitializer#initialize");
        }

        initializeLogListView();
        initializeLogFilteringCbxes();
    }

    private void initializeLogListView() {
        controller.getAdvancedLogListView().setCellFactory(listView -> new ListCell<>() {
            @Override
            public void updateItem(LogText logText, boolean empty) {
                super.updateItem(logText, empty);
                if (empty || logText == null) {
                    setText(null);
                    setGraphic(null);
                } else if (getText() == null || !getText().equals(logText.getText())) {
                    setText(logText.getText());
                    setTextFill(logText.getColor()); //javafx.scene.paint.Color;
                    setWrapText(true);
                    setPrefWidth(controller.getAdvancedLogListView().getPrefWidth() - 20); //must be lower than prefWidth of ListView to avoid unnecessary HBar display
                    setAlignment(Pos.CENTER_LEFT);
                    setFont(Font.font("System", logText.getWeight(), logText.getSize()));
                    setUserData(parentController.isDarkModeEnabled()); //this data (of ListCell/LabeledText) will be used by event handler later
                    setOnMouseClicked(RightClickHandlerEnum.INSTANCE);
                }
            }
        });
    }

    private void initializeLogFilteringCbxes() {
        controller.getAdvancedLogCbxInfoStartOfSearch().setSelected(true);
        controller.getAdvancedLogCbxInfoStartOfSearch().setOnAction(event -> {
            controller.addOrRemoveFilterType(controller.getAdvancedLogCbxInfoStartOfSearch(), LogTextTypeEnum.START_OF_SEARCH);
            controller.filterLog();
        });
        controller.getAdvancedLogCbxInfoEndOfSearch().setSelected(true);
        controller.getAdvancedLogCbxInfoEndOfSearch().setOnAction(event -> {
            controller.addOrRemoveFilterType(controller.getAdvancedLogCbxInfoEndOfSearch(), LogTextTypeEnum.END_OF_SEARCH);
            controller.filterLog();
        });
        controller.getAdvancedLogCbxShowKeySwaps().setSelected(true);
        controller.getAdvancedLogCbxShowKeySwaps().setOnAction(event -> {
            controller.addOrRemoveFilterType(controller.getAdvancedLogCbxShowKeySwaps(), LogTextTypeEnum.KEY_SWAP);
            controller.filterLog();
        });
        controller.getAdvancedLogCbxShowPointsGained().setSelected(true);
        controller.getAdvancedLogCbxShowPointsGained().setOnAction(event -> {
            controller.addOrRemoveFilterType(controller.getAdvancedLogCbxShowPointsGained(), LogTextTypeEnum.POINTS_GAINED);
            controller.filterLog();
        });
        controller.getAdvancedLogCbxShowProgress().setSelected(true);
        controller.getAdvancedLogCbxShowProgress().setOnAction(event -> {
            controller.addOrRemoveFilterType(controller.getAdvancedLogCbxShowProgress(), LogTextTypeEnum.SEARCH_PROGRESS);
            controller.filterLog();
        });
        controller.getAdvancedLogCbxGeneralMessages().setSelected(true);
        controller.getAdvancedLogCbxGeneralMessages().setOnAction(event -> {
            controller.addOrRemoveFilterType(controller.getAdvancedLogCbxGeneralMessages(), LogTextTypeEnum.GENERAL);
            controller.filterLog();
        });
    }
}
