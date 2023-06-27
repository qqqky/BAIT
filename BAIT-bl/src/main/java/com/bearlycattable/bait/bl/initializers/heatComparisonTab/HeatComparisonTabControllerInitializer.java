package com.bearlycattable.bait.bl.initializers.heatComparisonTab;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.bearlycattable.bait.advancedCommons.helpers.HeatVisualizerComponentHelper;
import com.bearlycattable.bait.bl.controllers.heatComparisonTab.HeatComparisonTabController;
import com.bearlycattable.bait.bl.controllers.RootController;
import com.bearlycattable.bait.bl.helpers.HeatVisualizerFormatterFactory;
import com.bearlycattable.bait.commons.Config;
import com.bearlycattable.bait.commons.enums.NumberFormatTypeEnum;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;
import com.bearlycattable.bait.commons.validators.PrivKeyValidator;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public final class HeatComparisonTabControllerInitializer {

    private static final List<NumberFormatTypeEnum> NUMBER_FORMATS = Collections.unmodifiableList(Arrays.asList(NumberFormatTypeEnum.DECIMAL, NumberFormatTypeEnum.HEX));
    private static final List<ScaleFactorEnum> SUPPORTED_SCALE_FACTORS = Collections.unmodifiableList(
            Arrays.asList(ScaleFactorEnum.DISABLED, ScaleFactorEnum.MEDIUM, ScaleFactorEnum.HIGH, ScaleFactorEnum.HIGHEST));
    private final HeatVisualizerComponentHelper componentHelper = new HeatVisualizerComponentHelper();
    private final HeatComparisonTabController controller;
    private final RootController rootController;

    private HeatComparisonTabControllerInitializer() {
        throw new UnsupportedOperationException("Creation of " + this.getClass().getName() + " directly is not allowed");
    }

    private HeatComparisonTabControllerInitializer(HeatComparisonTabController controller, RootController rootController) {
        this.controller = controller;
        this.rootController = rootController;
    }

    public static void initialize(HeatComparisonTabController controller, RootController rootController) {
        if (controller == null || rootController == null) {
            throw new IllegalStateException("Suitable controller not found at HeatComparisonTabControllerInitializer#initialize");
        }

        if (controller.isParentValid()) {
            return;
        }

        //root controller must be set before initialization
        controller.setRootController(rootController);
        new HeatComparisonTabControllerInitializer(controller, rootController).init();
    }

    private void init() {
        initializePrivHeatContainerAndLabelMappings();
        initializePrivHeatDifferenceNumericLabelMappings();

        initializePubHeatPositiveMappings();
        initializePubHeatNegativeMappings();
        initializeReferenceMappings();
        centerMappedLabels();
        addDefaultAlternatingColorsForReferenceLabels();

        initializeNumericTypeChoiceBox();
        addPrivResolutionSpinnerListener();

        initializeScaleFactorComboBox();

        controller.getComparisonTextFieldCurrentKey().setTextFormatter(HeatVisualizerFormatterFactory.getDefaultPrivateKeyFormatter());
        controller.getComparisonTextFieldCurrentKey().textProperty().addListener(listener -> controller.getComparisonLabelLengthCurrentKey().setText(Integer.toString(controller.getComparisonTextFieldCurrentKey().getLength())));
    }

    private void addDefaultAlternatingColorsForReferenceLabels() {
        for (int i = 0; i < 40; i++) {
            controller.getReferenceForUncompressedAbsoluteIndexes().get(i).getStyleClass().add(i % 2 == 0 ? "bcgLightGray" : "bcgDarkGray");
            controller.getReferenceForCompressedAbsoluteIndexes().get(i).getStyleClass().add(i % 2 == 0 ? "bcgLightGray" : "bcgDarkGray");
        }
    }

    private void centerMappedLabels() {
        controller.getPubUncompressedHeatPositiveAbsoluteIndexes().keySet().stream().forEach(key -> {
            controller.getPubUncompressedHeatPositiveAbsoluteIndexes().get(key).setAlignment(Pos.CENTER);
        });
        controller.getPubUncompressedHeatNegativeAbsoluteIndexes().keySet().stream().forEach(key -> {
            controller.getPubUncompressedHeatNegativeAbsoluteIndexes().get(key).setAlignment(Pos.CENTER);
        });

        controller.getPubCompressedHeatPositiveAbsoluteIndexes().keySet().stream().forEach(key -> {
            controller.getPubCompressedHeatPositiveAbsoluteIndexes().get(key).setAlignment(Pos.CENTER);
        });
        controller.getPubCompressedHeatNegativeAbsoluteIndexes().keySet().stream().forEach(key -> {
            controller.getPubCompressedHeatNegativeAbsoluteIndexes().get(key).setAlignment(Pos.CENTER);
        });

        controller.getReferenceForUncompressedAbsoluteIndexes().keySet().stream().forEach(key -> {
            controller.getReferenceForUncompressedAbsoluteIndexes().get(key).setAlignment(Pos.CENTER);
        });
        controller.getReferenceForCompressedAbsoluteIndexes().keySet().stream().forEach(key -> {
            controller.getReferenceForCompressedAbsoluteIndexes().get(key).setAlignment(Pos.CENTER);
        });
    }

    void addPrivResolutionSpinnerListener() {
    controller.getComparisonSpinnerResolutionPriv().getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
        int currentPrivAccuracyResolution = Integer.parseInt(newValue);
        controller.setCurrentPrivAccuracyResolution(currentPrivAccuracyResolution);

        //just don't call changePrivHeatResolution if currentPriv or referencePriv is not valid
        if (!PrivKeyValidator.isValidPK(controller.getComparisonTextFieldCurrentKey().getText())
                || controller.getComparisonTextFieldReferenceKey() == null
                || !PrivKeyValidator.isValidPK(controller.getComparisonTextFieldReferenceKey().getText())) {
            return;
        }

        controller.changePrivHeatResolution();
    });
    }

    private void initializePrivHeatContainerAndLabelMappings() {
        //init containers for privHeatPositive (as word containers)
        List<HBox> privContainersHeatPositive = componentHelper.extractDirectChildContainersOfType(controller.getContainerPrivHeatPositive(), HBox.class);
        componentHelper.putComponentListResultsToMap(privContainersHeatPositive, controller.getPrivHeatPositiveContainerMappings(), 1);

        //init indexes for privHeatPositive (absolute)
        List<Label> allPrivHeatPositiveLabels = componentHelper.extractDirectChildContainersOfTypeFromList(privContainersHeatPositive, Label.class);
        componentHelper.putControlObjectListResultsToMap(allPrivHeatPositiveLabels, controller.getPrivHeatPositiveAbsoluteIndexes(), 0);

        //init containers for privHeatNegative (as word containers)
        List<HBox> privContainersHeatNegative = componentHelper.extractDirectChildContainersOfType(controller.getContainerPrivHeatNegative(), HBox.class);
        componentHelper.putComponentListResultsToMap(privContainersHeatNegative, controller.getPrivHeatNegativeContainerMappings(), 1);

        //init indexes for privHeatNegative (absolute)
        List<Label> allPrivHeatNegativeLabels = componentHelper.extractDirectChildContainersOfTypeFromList(privContainersHeatNegative, Label.class);
        componentHelper.putControlObjectListResultsToMap(allPrivHeatNegativeLabels, controller.getPrivHeatNegativeAbsoluteIndexes(), 0);
    }

    private void initializePrivHeatDifferenceNumericLabelMappings() {
        List<Label> allPrivHeatPositiveNumericResultLabels = componentHelper.extractDirectChildControlObjectsOfType(controller.getPrivHeatPositiveNumericLabelContainer(), Label.class);
        componentHelper.putControlObjectListResultsToMap(allPrivHeatPositiveNumericResultLabels, controller.getPrivHeatPositiveNumericLabels(), 1);

        List<Label> allPrivHeatNegativeNumericResultLabels = componentHelper.extractDirectChildControlObjectsOfType(controller.getPrivHeatNegativeNumericLabelContainer(), Label.class);
        componentHelper.putControlObjectListResultsToMap(allPrivHeatNegativeNumericResultLabels, controller.getPrivHeatNegativeNumericLabels(), 1);
    }

    private void initializePubHeatPositiveMappings() {
        //for uncompressed pubs
        componentHelper.initializeComponentMappingsForPubs(controller.getContainerPubHeatPositiveUncompressed(), controller.getPubUncompressedHeatPositiveAbsoluteIndexes(), 0);
        //for compressed pubs
        componentHelper.initializeComponentMappingsForPubs(controller.getContainerPubHeatPositiveCompressed(), controller.getPubCompressedHeatPositiveAbsoluteIndexes(), 0);
    }

    private void initializePubHeatNegativeMappings() {
        //for uncompressed pubs
        componentHelper.initializeComponentMappingsForPubs(controller.getContainerPubHeatNegativeUncompressed(), controller.getPubUncompressedHeatNegativeAbsoluteIndexes(), 0);
        //for compressed pubs
        componentHelper.initializeComponentMappingsForPubs(controller.getContainerPubHeatNegativeCompressed(), controller.getPubCompressedHeatNegativeAbsoluteIndexes(), 0);
    }

    private void initializeReferenceMappings() {
        //for uncompressed
        componentHelper.initializeComponentMappingsForPubs(controller.getContainerReferenceUncompressed(), controller.getReferenceForUncompressedAbsoluteIndexes(), 0);
        //for compressed
        componentHelper.initializeComponentMappingsForPubs(controller.getContainerReferenceCompressed(), controller.getReferenceForCompressedAbsoluteIndexes(), 0);
    }

    //this throws event and triggers doChangeHeatResultFormat()
    private void initializeNumericTypeChoiceBox() {
        controller.getComparisonChoiceBoxNumberFormatType().getItems().addAll(NUMBER_FORMATS);

        //set initial number format
        controller.getComparisonChoiceBoxNumberFormatType().getSelectionModel().select(NUMBER_FORMATS.get(0));
    }

    private void initializeScaleFactorComboBox() {
        controller.getComparisonComboBoxScaleFactor().getItems().addAll(SUPPORTED_SCALE_FACTORS);
        controller.getComparisonComboBoxScaleFactor().getSelectionModel().select(Config.DEFAULT_SCALE_FACTOR);
    }

}
