package com.bearlycattable.bait.bl.initializers;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.bearlycattable.bait.bl.controllers.AdvancedSubTabResultsController;
import com.bearlycattable.bait.bl.controllers.AdvancedTabMainController;
import com.bearlycattable.bait.bl.helpers.HeatVisualizerFormatterFactory;
import com.bearlycattable.bait.commons.Config;
import com.bearlycattable.bait.commons.enums.JsonResultScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;

public class AdvancedSubTabResultsControllerInitializer {

    private final AdvancedSubTabResultsController controller;
    private AdvancedTabMainController parentController;

    private AdvancedSubTabResultsControllerInitializer(AdvancedSubTabResultsController controller, AdvancedTabMainController parentController) {
        this.controller = controller;
        this.parentController = parentController;
    }

    public static AdvancedSubTabResultsControllerInitializer getInitializer(AdvancedSubTabResultsController controller, AdvancedTabMainController parentController) {
        return new AdvancedSubTabResultsControllerInitializer(controller, parentController);
    }

    public void initialize() {
        if (controller == null || parentController == null) {
            throw new IllegalStateException("Suitable controller not found at AdvancedSubTabResultsControllerInitializer#initialize");
        }

        initializeResultDisplayScaleFactorComboBox();
        initializeResultFilterScaleFactorComboBox();
        addTextFormatterForFilterPubPrefixField();
        addTextFormatterForMinAccuracyField();
        addTextFormatterForMaxResultsField();
    }

    private void addTextFormatterForFilterPubPrefixField() {
        controller.getAdvancedResultsFilterPubPrefix().setTextFormatter(HeatVisualizerFormatterFactory.getDefaultUnencodedPublicKeyFormatter());
    }

    private void addTextFormatterForMinAccuracyField() {
        controller.getAdvancedResultsFilterTextFieldAccuracyMin().setTextFormatter(HeatVisualizerFormatterFactory.getDefaultPositiveNumberFormatter(100));
        controller.getAdvancedResultsFilterTextFieldAccuracyMin().focusedProperty().addListener((obs, oldVal, newVal) -> {
            //when unfocused, check if max value is not breached
            if (!newVal) {
                String current = controller.getAdvancedResultsFilterTextFieldAccuracyMin().getText();
                if (!current.isEmpty() && Integer.parseInt(current) > 100) {
                    controller.getAdvancedResultsFilterTextFieldAccuracyMin().setText(Integer.toString(100));
                }
            }
        });
    }

    private void addTextFormatterForMaxResultsField() {
        controller.getAdvancedResultsFilterTextFieldMaxResults().setTextFormatter(HeatVisualizerFormatterFactory.getDefaultPositiveNumberFormatter(999));
        controller.getAdvancedResultsFilterTextFieldMaxResults().focusedProperty().addListener((obs, oldVal, newVal) -> {
            //when unfocused, check if max value is not breached
            if (!newVal) {
                String current = controller.getAdvancedResultsFilterTextFieldAccuracyMin().getText();
                if (!current.isEmpty() && Integer.parseInt(current) > 999) {
                    controller.getAdvancedResultsFilterTextFieldAccuracyMin().setText(Integer.toString(999));
                }
            }
        });
    }

    private void initializeResultDisplayScaleFactorComboBox() {
        controller.getAdvancedResultsComboResultsScaleFactor().getItems().addAll(Arrays.stream(JsonResultScaleFactorEnum.values()).collect(Collectors.toList()));
        controller.getAdvancedResultsComboResultsScaleFactor().getSelectionModel().select(ScaleFactorEnum.toJsonScaleFactorEnum(Config.DEFAULT_SCALE_FACTOR));
    }

    private void initializeResultFilterScaleFactorComboBox() {
        controller.getAdvancedResultsComboFilterResultsScaleFactor().getItems().addAll(Arrays.stream(JsonResultScaleFactorEnum.values()).collect(Collectors.toList()));
        controller.getAdvancedResultsComboFilterResultsScaleFactor().getSelectionModel().select(ScaleFactorEnum.toJsonScaleFactorEnum(Config.DEFAULT_SCALE_FACTOR));
    }
}
