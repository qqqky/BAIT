package com.bearlycattable.bait.bl.initializers.advancedTab;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.bearlycattable.bait.bl.controllers.advancedTab.AdvancedSubTabSearchController;
import com.bearlycattable.bait.bl.controllers.advancedTab.AdvancedTabMainController;
import com.bearlycattable.bait.bl.helpers.BaitFormatterFactory;
import com.bearlycattable.bait.commons.Config;
import com.bearlycattable.bait.commons.enums.JsonResultScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;

public final class AdvancedSubTabSearchControllerInitializer {

    private final AdvancedSubTabSearchController controller;
    private final EventHandler<ActionEvent> disabledWordsCheckboxHandler;

    private AdvancedSubTabSearchControllerInitializer() {
        throw new UnsupportedOperationException("Creation of " + this.getClass().getName() + " directly is not allowed");
    }

    private AdvancedSubTabSearchControllerInitializer(AdvancedSubTabSearchController controller) {
        this.controller = controller;
        this.disabledWordsCheckboxHandler = event -> {
            CheckBox cbx = (CheckBox) event.getSource();
            String id = cbx.getId();
            int wordNum = Integer.parseInt(id.substring(id.length() - 2));

            if (!cbx.isSelected()) {
                controller.disableWord(wordNum);
                return;
            }

            controller.enableWord(wordNum);
        };
    }

    public static void initialize(AdvancedSubTabSearchController controller, AdvancedTabMainController parentController) {
        if (controller == null || parentController == null) {
            throw new IllegalStateException("Suitable controller not found at AdvancedSubTabSearchControllerInitializer#initialize");
        }

        if (controller.isParentValid()) {
            return;
        }

        //only set implementations we need
        controller.setAdvancedSearchAccessProxy(parentController);

        new AdvancedSubTabSearchControllerInitializer(controller).init();
    }

    private void init() {
        initializeAdvancedSearchModeChoiceBox();
        initializeAdvancedScaleFactorComboBox();
        initializeIterationsField();
        addCheckboxEventHandlersForWordLocking();
        initializeSeedComponent();

        //optional configs
        initializeLoopComponent(Config.MAX_LOOPS);
        initializeSeedMutationControl();
        initializeSeedMutationTextFieldFormatters();

        //logging
        initializeLogComponent();

        //notification tolerance config
        initializeNotificationCbx();

        addDefaultValues();

        // addTextFormatterForPrefixField();
    }

    private void initializeNotificationCbx() {
        controller.getAdvancedSearchCbxEnableSoundNotifications().setOnAction(event -> {
            controller.insertNotificationToleranceConfigToUi(controller.getAdvancedSearchCbxEnableSoundNotifications().isSelected());
        });
    }

    private void initializeSeedMutationTextFieldFormatters() {
        //incDec
        controller.getAdvancedSearchTextFieldIncDecBy().setTextFormatter(BaitFormatterFactory.getDefaultWordInputFieldFormatter());

        //horizontal rotation
        controller.getAdvancedTextFieldRotateHorizontallyBy().setTextFormatter(BaitFormatterFactory.getDefaultPositiveNumberFormatter(128));
        controller.getAdvancedTextFieldRotateHorizontallyBy().focusedProperty().addListener((obs, oldVal, newVal) -> {
            //when unfocused, check if max value is not breached
            if (!newVal) {
                String current = controller.getAdvancedTextFieldRotateHorizontallyBy().getText();
                int max = findMaxRotationValueFromUi();
                if (max == -1) {
                    throw new IllegalStateException("Unexpected value received for horizontal rotation");
                }

                if (!current.isEmpty() && Integer.parseInt(current) > max) {
                    controller.getAdvancedTextFieldRotateHorizontallyBy().setText(Integer.toString(max));
                }
            }
        });

        //vertical rotation
        controller.getAdvancedTextFieldRotateVerticallyBy().setTextFormatter(BaitFormatterFactory.getDefaultPositiveNumberFormatter(16));
        controller.getAdvancedTextFieldRotateVerticallyBy().focusedProperty().addListener((obs, oldVal, newVal) -> {
            //when unfocused, check if max value is not breached
            if (!newVal) {
                String current = controller.getAdvancedTextFieldRotateVerticallyBy().getText();
                if (!current.isEmpty() && Integer.parseInt(current) > 16) {
                    controller.getAdvancedTextFieldRotateVerticallyBy().setText(Integer.toString(16));
                }
            }
        });
    }

    private int findMaxRotationValueFromUi() {
        if (!controller.getAdvancedSearchRadioOptionalRotateNormal().isDisabled() && controller.getAdvancedSearchRadioOptionalRotateNormal().isSelected()) {
            return Config.MAX_H_ROTATIONS_FULL;
        }

        if (!controller.getAdvancedSearchRadioOptionalRotatePrefixed().isDisabled() && controller.getAdvancedSearchRadioOptionalRotatePrefixed().isSelected()) {
            return Config.MAX_H_ROTATIONS_PREFIXED;
        }

        if (!controller.getAdvancedSearchRadioOptionalRotateWords().isDisabled() && controller.getAdvancedSearchRadioOptionalRotateWords().isSelected()) {
            return Config.MAX_H_ROTATIONS_WORDS;
        }

        return -1;
    }

    private void addDefaultValues() {
        controller.getAdvancedSearchTextFieldNumberOfLoops().setText(Integer.toString(Config.DEFAULT_LOOPS_ADVANCED_SEARCH));
        controller.getAdvancedSearchTextFieldLogKeyEveryXIterations().setText(Integer.toString(Config.DEFAULT_LOG_SPACING_ADVANCED_SEARCH));
    }

    private void initializeLogComponent() {
        controller.getAdvancedSearchTextFieldLogKeyEveryXIterations().setTextFormatter(BaitFormatterFactory.getDefaultPositiveNumberFormatter(Config.MAX_LOG_SPACING_ADVANCED_SEARCH));
        controller.getAdvancedSearchTextFieldLogKeyEveryXIterations().focusedProperty().addListener((obs, oldVal, newVal) -> {
            //when unfocused, check if max value is not breached
            if (!newVal) {
                String current = controller.getAdvancedSearchTextFieldLogKeyEveryXIterations().getText();
                if (!current.isEmpty() && Integer.parseInt(current) > Config.MAX_LOG_SPACING_ADVANCED_SEARCH) {
                    controller.getAdvancedSearchTextFieldLogKeyEveryXIterations().setText(Integer.toString(Config.MAX_LOG_SPACING_ADVANCED_SEARCH));
                }
            }
        });
    }

    private void initializeIterationsField() {
        controller.getAdvancedSearchTextFieldIterations().setTextFormatter(BaitFormatterFactory.getDefaultPositiveNumberFormatter(Config.MAX_ITERATIONS_ADVANCED_SEARCH));
        controller.getAdvancedSearchTextFieldIterations().focusedProperty().addListener((obs, oldVal, newVal) -> {
            //when unfocused, check if max value is not breached
            if (!newVal) {
                String current = controller.getAdvancedSearchTextFieldIterations().getText();
                if (!current.isEmpty() && Long.parseLong(current) > Config.MAX_ITERATIONS_ADVANCED_SEARCH) {
                    controller.getAdvancedSearchTextFieldIterations().setText(Integer.toString(Config.MAX_ITERATIONS_ADVANCED_SEARCH));
                }
            }
        });
    }

    private void initializeSeedMutationControl() {
        controller.getAdvancedSearchCbxLoopOptionEnableIncDec().setOnAction(event -> {
            if (CheckBox.class.isAssignableFrom(event.getSource().getClass())) {
                CheckBox cbx = (CheckBox) event.getSource();
                controller.getAdvancedOptionalMenuIncDecContainer().setDisable(!cbx.isSelected());
            }
        });

        controller.getAdvancedSearchCbxLoopOptionEnableRotation().setOnAction(event -> {
            if (CheckBox.class.isAssignableFrom(event.getSource().getClass())) {
                CheckBox cbx = (CheckBox) event.getSource();
                controller.getAdvancedOptionalMenuHRotationContainer().setDisable(!cbx.isSelected());
            }
        });

        controller.getAdvancedSearchCbxLoopOptionEnableVertical().setOnAction(event -> {
            if (CheckBox.class.isAssignableFrom(event.getSource().getClass())) {
                CheckBox cbx = (CheckBox) event.getSource();
                controller.getAdvancedOptionalMenuVRotationContainer().setDisable(!cbx.isSelected());
            }
        });
    }

    private void initializeLoopComponent(int maxLoops) {
        controller.getAdvancedSearchTextFieldNumberOfLoops().setTextFormatter(BaitFormatterFactory.getDefaultPositiveNumberFormatter(maxLoops));
        controller.getAdvancedSearchTextFieldNumberOfLoops().focusedProperty().addListener((obs, oldVal, newVal) -> {
            //when unfocused, check if max value is not breached
            if (!newVal) {
                String current = controller.getAdvancedSearchTextFieldNumberOfLoops().getText();
                if (!current.isEmpty() && Integer.parseInt(current) > maxLoops) {
                    controller.getAdvancedSearchTextFieldNumberOfLoops().setText(Integer.toString(maxLoops));
                }
            }
        });
    }

    private void initializeSeedComponent() {
        controller.getAdvancedSearchTextFieldContinueFromSeed().setTextFormatter(BaitFormatterFactory.getDefaultPrivateKeyFormatter());
        controller.getAdvancedSearchTextFieldContinueFromSeed().textProperty().addListener(listener -> controller.getAdvancedSearchLabelLengthSeed().setText(Integer.toString(controller.getAdvancedSearchTextFieldContinueFromSeed().getText().length())));
    }

    private void addCheckboxEventHandlersForWordLocking() {
        controller.getAdvancedSearchHBoxDisabledWordsParent().getChildren().stream()
                .filter(component -> CheckBox.class.isAssignableFrom(component.getClass()))
                .map(CheckBox.class::cast)
                .forEach(cbx -> cbx.setOnAction(disabledWordsCheckboxHandler));
    }

    @SuppressWarnings("unchecked")
    private void initializeAdvancedSearchModeChoiceBox() {
        controller.getAdvancedSearchChoiceBoxSearchMode().getItems().addAll(Config.SUPPORTED_ADVANCED_SEARCH_TYPES);
        controller.getAdvancedSearchChoiceBoxSearchMode().setOnAction(event -> {
            Object source = event.getSource();

            if (!ChoiceBox.class.isAssignableFrom(source.getClass())) {
                return;
            }

            String selectedItem = ((ChoiceBox<String>) event.getSource()).getSelectionModel().getSelectedItem();
            SearchModeEnum.getByLabel(selectedItem).ifPresent(currentMode -> {
                boolean randomRelatedMode = SearchModeEnum.isRandomRelatedMode(currentMode);
                controller.modifyAccessToSeedComponent(!randomRelatedMode);
                controller.getAdvancedSearchCbxLoopOptionEnableIncDec().setDisable(randomRelatedMode);
                controller.getAdvancedSearchCbxLoopOptionEnableRotation().setDisable(randomRelatedMode);
                controller.getAdvancedSearchCbxLoopOptionEnableVertical().setDisable(randomRelatedMode);

                //disable option component for all random-related modes, enable for others only if corresponding checkbox is selected
                controller.getAdvancedOptionalMenuIncDecContainer().setDisable(randomRelatedMode || !controller.getAdvancedSearchCbxLoopOptionEnableIncDec().isSelected());
                controller.getAdvancedOptionalMenuHRotationContainer().setDisable(randomRelatedMode || !controller.getAdvancedSearchCbxLoopOptionEnableRotation().isSelected());
                controller.getAdvancedOptionalMenuVRotationContainer().setDisable(randomRelatedMode || !controller.getAdvancedSearchCbxLoopOptionEnableVertical().isSelected());

                controller.insertModeSpecificOptionsToUi(currentMode);
                controller.getAdvancedBtnSearch().setDisable(false);
            });
        });
        controller.getAdvancedSearchChoiceBoxSearchMode().getSelectionModel().select(0);
    }

    private void initializeAdvancedScaleFactorComboBox() {
        controller.getAdvancedSearchComboBoxScaleFactor().getItems().addAll(Arrays.stream(JsonResultScaleFactorEnum.values()).collect(Collectors.toList()));
        controller.getAdvancedSearchComboBoxScaleFactor().getSelectionModel().select(ScaleFactorEnum.toJsonScaleFactorEnum(Config.DEFAULT_SCALE_FACTOR));
    }

}
