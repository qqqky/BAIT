package com.bearlycattable.bait.bl.initializers.constructionTab;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.bearlycattable.bait.advancedCommons.helpers.HeatVisualizerComponentHelper;
import com.bearlycattable.bait.bl.controllers.RootController;
import com.bearlycattable.bait.bl.controllers.constructionTab.ConstructionTabController;
import com.bearlycattable.bait.bl.helpers.HeatVisualizerFormatterFactory;
import com.bearlycattable.bait.commons.CssConstants;
import com.bearlycattable.bait.commons.HeatVisualizerConstants;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public final class ConstructionTabControllerInitializer {

    private static final List<String> HEX_ALPHABET = Collections.unmodifiableList(Arrays.stream(HeatVisualizerConstants.HEX_ALPHABET).sequential().collect(Collectors.toList()));

    private final ConstructionTabController controller;
    private RootController rootController;
    private final HeatVisualizerComponentHelper componentHelper = new HeatVisualizerComponentHelper();

    private final EventHandler<ActionEvent> checkBoxWordConstructionEventHandler = event -> {
        CheckBox chk = (CheckBox) event.getSource();
        String id = chk.getId();
        int wordNum = Integer.parseInt(id.substring(id.length() - 1));
        if (rootController.isValidWordInComboBoxesUi(wordNum)) {
            rootController.modifyWordComboBoxAndTextFieldAccess(wordNum, chk.isSelected());
            return;
        }
        chk.setSelected(false); //else unselect
    };

    private final ChangeListener<String> privInputFieldChangeListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            int wordNumber = parseWordNumberFromObservable(observable);
            if (newValue.length() > 8) {
                newValue = newValue.substring(0, 8);
            }
            controller.setPrivWordComboBoxesInUi(newValue, wordNumber);
        }

        private int parseWordNumberFromObservable(ObservableValue<? extends String> observable) {
            String s = observable.toString(); //no other convenient way
            int from = s.indexOf(CssConstants.INPUT_FIELD_ID_REFERENCE) + CssConstants.INPUT_FIELD_ID_REFERENCE.length();
            return Integer.parseInt(s.substring(from, from + 2));
        }
    };

    private ConstructionTabControllerInitializer() {
        throw new UnsupportedOperationException("Creation of " + this.getClass().getName() + " directly is not allowed");
    }

    private ConstructionTabControllerInitializer(ConstructionTabController controller, RootController rootController) {
        this.controller = controller;
        this.rootController = rootController;
    }

    public static void initialize(ConstructionTabController controller, RootController rootController) {
        if (controller == null || rootController == null) {
            throw new IllegalStateException("Suitable controller not found at ConstructionTabControllerInitializer#initialize");
        }

        if (controller.isParentValid()) {
            return;
        }

        //root controller must be set before initialization
        controller.setRootController(rootController);
        new ConstructionTabControllerInitializer(controller, rootController).init();
    }

    private void init() {
        initializePrivComboBoxMappings();
        addTextFormatterToRandomWordPrefixTextField();
        controller.getConstructionHBoxRandomPrefixParent().setDisable(false);

        initializeTextFieldLengthListeners();
        addPrivWordCheckboxEventHandlers();
        addPrivInputTextFieldListeners();
    }

    private void initializePrivComboBoxMappings() {
        //initialize mappings for priv combobox groups (as hex word containers, index range 1-8)
        List<VBox> mainChildren = componentHelper.extractDirectChildContainersOfType(controller.getConstructionHBoxParentForComboAndCheckBoxes(), VBox.class);

        List<HBox> privWordComboAndCheckBoxHolderParents = mainChildren.stream()
                .map(vbox -> componentHelper.extractDirectChildContainersOfType(vbox, HBox.class))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        List<HBox> privWordComboBoxParents = privWordComboAndCheckBoxHolderParents.stream()
                .map(hbox -> componentHelper.extractDirectChildContainersOfType(hbox, HBox.class))
                .flatMap(Collection::stream)
                .map(hbox -> {
                    if (hbox.getChildren().stream()
                            .mapToInt(node -> ComboBox.class.isAssignableFrom(node.getClass()) ? 1 : 0)
                            .reduce(0, Integer::sum) == 8) {
                        return hbox;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .sorted((hbox1, hbox2) -> compareNumericIds(hbox1, hbox2, "privInputWord"))
                .collect(Collectors.toList());

        componentHelper.putComponentListResultsToMap(privWordComboBoxParents, controller.getPrivWordComboBoxParentContainerMappings(), 1);

        //initialize mappings for all checkboxes (absolute, index range 1-8)
        List<CheckBox> privWordCheckboxes = privWordComboAndCheckBoxHolderParents.stream()
                .map(hbox -> componentHelper.extractDirectChildControlObjectsOfType(hbox, CheckBox.class))
                .flatMap(Collection::stream)
                .sorted((cbx1, cbx2) -> compareNumericIds(cbx1, cbx2, "checkboxWord"))
                .collect(Collectors.toList());

        componentHelper.putControlObjectListResultsToMap(privWordCheckboxes, controller.getPrivWordCheckboxMappings(), 1);
        privWordCheckboxes.forEach(cbx -> cbx.setSelected(false));


        //initialize mappings for hex word containers (absolute, index range 1-8)
        List<TextField> hexWordTextFields = componentHelper.extractDirectChildControlObjectsOfType(controller.getConstructionHBoxCurrentInputParent(), TextField.class);
        componentHelper.putControlObjectListResultsToMap(hexWordTextFields, controller.getInputFieldWordMappings(), 1);
        addTextFormattersToInputFields(controller.getInputFieldWordMappings());

        //initialize mappings for all priv comboboxes (absolute, index range 0-63)
        List<ComboBox> comboBoxes = componentHelper.extractDirectChildContainersOfTypeFromList(privWordComboBoxParents, ComboBox.class);
            comboBoxes.sort((cmb1, cmb2) -> compareNumericIds(cmb1, cmb2, "comboIndex"));
        componentHelper.putControlObjectListResultsToMap(comboBoxes, controller.getPrivCompleteComboBoxMappings(), 0);
        comboBoxes.forEach(comboBox -> comboBox.getItems().addAll(HEX_ALPHABET));
    }

    private int compareNumericIds(Node node1, Node node2, String idPrefix) {
        if (idPrefix == null || idPrefix.isEmpty()) {
            throw new IllegalArgumentException("Id prefix cannot be null or empty if we want to sort by it!");
        }

        int one = Integer.parseInt(node1.getId().substring(idPrefix.length()));
        int two = Integer.parseInt(node2.getId().substring(idPrefix.length()));

        return Integer.compare(one, two);
    }

    private void addTextFormattersToInputFields(Map<Integer, TextField> inputFieldWordMappings) {
        inputFieldWordMappings.keySet()
                .forEach(key -> inputFieldWordMappings.get(key).setTextFormatter(HeatVisualizerFormatterFactory.getDefaultWordInputFieldFormatter()));
    }

    private void addTextFormatterToRandomWordPrefixTextField() {
        controller.getConstructionTextFieldRandomWordPrefix().setTextFormatter(HeatVisualizerFormatterFactory.getDefaultWordInputFieldFormatter());
    }

    private void initializeTextFieldLengthListeners() {
        controller.getConstructionTextFieldCEPub().textProperty().addListener(listener -> controller.getConstructionLabelCEPubLength().setText(Integer.toString(controller.getConstructionTextFieldCEPub().getLength())));
        controller.getConstructionTextFieldCUPub().textProperty().addListener(listener -> controller.getConstructionLabelCUPubLength().setText(Integer.toString(controller.getConstructionTextFieldCUPub().getLength())));
        controller.getConstructionTextFieldUUPub().textProperty().addListener(listener -> controller.getConstructionLabelUUPubLength().setText(Integer.toString(controller.getConstructionTextFieldUUPub().getLength())));
        controller.getConstructionTextFieldUEPub().textProperty().addListener(listener -> controller.getConstructionLabelUEPubLength().setText(Integer.toString(controller.getConstructionTextFieldUEPub().getLength())));
    }

    private void addPrivWordCheckboxEventHandlers() {
        controller.getPrivWordCheckboxMappings().values()
                .forEach(checkBox -> {
                    checkBox.setOnAction(checkBoxWordConstructionEventHandler);
                    checkBox.setDisable(true); //all checkboxes are disabled by default
        });
    }

    private void addPrivInputTextFieldListeners() {
        controller.getInputFieldWordMappings().keySet()
                .forEach(key -> controller.getInputFieldWordMappings().get(key).textProperty().addListener(privInputFieldChangeListener));
    }
}
