package com.bearlycattable.bait.bl.initializers.quickSearchTab;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.bearlycattable.bait.bl.controllers.RootController;
import com.bearlycattable.bait.bl.controllers.quickSearchTab.QuickSearchTabController;
import com.bearlycattable.bait.bl.helpers.HeatVisualizerFormatterFactory;
import com.bearlycattable.bait.commons.Config;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;

import javafx.scene.control.CheckBox;

public final class QuickSearchTabControllerInitializer {

    private final QuickSearchTabController controller;
    private final RootController rootController;

    private QuickSearchTabControllerInitializer() {
        throw new UnsupportedOperationException("Creation of " + this.getClass().getName() + " directly is not allowed");
    }

    private QuickSearchTabControllerInitializer(QuickSearchTabController controller, RootController rootController) {
        this.controller = controller;
        this.rootController = rootController;
    }

    public static void initialize(QuickSearchTabController controller, RootController rootController) {
        if (controller == null || rootController == null) {
            throw new IllegalStateException("Suitable controller not found at QuickSearchTabControllerInitializer#initialize");
        }

        if (controller.isParentValid()) {
            return;
        }

        //root controller must be set before initialization
        controller.setRootController(rootController);
        new QuickSearchTabControllerInitializer(controller, rootController).init();
    }

    private void init() {
        initializeSearchRelatedComponents();
        addPubAccuracyMultiplierCheckboxEventHandler();
        addTextFormatters();
        addDefaultValues();
    }

    private void addTextFormatters() {
        controller.getQuickSearchTextFieldSimilarityPercent().setTextFormatter(HeatVisualizerFormatterFactory.getDefaultPositiveNumberFormatter(100));
        controller.getQuickSearchTextFieldSimilarityPercent().focusedProperty().addListener((obs, oldVal, newVal) -> {
            //when unfocused, check if max value is not breached
            if (!newVal) {
                String current = controller.getQuickSearchTextFieldSimilarityPercent().getText();
                if (!current.isEmpty() && Integer.parseInt(current) > 100) {
                    controller.getQuickSearchTextFieldSimilarityPercent().setText("100");
                }
            }
        });
        controller.getQuickSearchTextFieldIterations().setTextFormatter(HeatVisualizerFormatterFactory.getDefaultPositiveNumberFormatter(Config.MAX_ITERATIONS_QUICK_SEARCH));
        controller.getQuickSearchTextFieldIterations().focusedProperty().addListener((obs, oldVal, newVal) -> {
            //when unfocused, check if max value is not breached
            if (!newVal) {
                String current = controller.getQuickSearchTextFieldIterations().getText();
                if (!current.isEmpty() && Integer.parseInt(current) > Config.MAX_ITERATIONS_QUICK_SEARCH) {
                    controller.getQuickSearchTextFieldIterations().setText(Integer.toString(Config.MAX_ITERATIONS_QUICK_SEARCH));
                }
            }
        });
    }

    private void addDefaultValues() {
        controller.getQuickSearchTextFieldSimilarityPercent().setText(Integer.toString(Config.DEFAULT_ACCURACY_QUICK_SEARCH));
        controller.getQuickSearchTextFieldIterations().setText(Integer.toString(Config.DEFAULT_ITERATIONS_QUICK_SEARCH));
    }

    private void initializeSearchRelatedComponents() {
        //initialize ChoiceBox for search types
        controller.getQuickSearchChoiceBoxSearchMode().getItems().addAll(Config.SUPPORTED_QUICKSEARCH_TYPES);
        controller.getQuickSearchChoiceBoxSearchMode().setOnAction(event -> modifySeedOptions());
        controller.getQuickSearchChoiceBoxSearchMode().setValue(Config.SUPPORTED_QUICKSEARCH_TYPES.get(0));
        controller.getQuickSearchChoiceBoxSearchMode().getSelectionModel().select(0);

        //initialize ComboBox for scale factor choices
        controller.getQuickSearchComboBoxScaleFactor().getItems().addAll(Arrays.stream(ScaleFactorEnum.values()).sequential().collect(Collectors.toList()));
        controller.getQuickSearchComboBoxScaleFactor().getSelectionModel().select(1);

        //initialize seed component
        controller.getQuickSearchTextFieldSeedPriv().setTextFormatter(HeatVisualizerFormatterFactory.getDefaultPrivateKeyFormatter());
        controller.getQuickSearchTextFieldSeedPriv().textProperty().addListener(listener -> controller.getQuickSearchLabelLengthSeed().setText(Integer.toString(controller.getQuickSearchTextFieldSeedPriv().getText().length())));
        controller.getQuickSearchBtnImportSeedPriv().setOnAction(event -> controller.importPKFromKeyConstructionTab(controller.getQuickSearchTextFieldSeedPriv()));

        //init self-seed cbx
        controller.getQuickSearchCbxSelfSeed().setOnAction(event -> controller.getQuickSearchSelectSeedParentComponent().setDisable(controller.getQuickSearchCbxSelfSeed().isSelected()));
    }

    private void modifySeedOptions() {
        SearchModeEnum searchMode = controller.getSearchModeFromUi();

        switch (searchMode) {
            case RANDOM: //intentional fall-through
            case RANDOM_SAME_WORD:
            case RANDOM_PREFIXED_WORD:
                //seed disabled altogether
                controller.getQuickSearchCbxSelfSeed().setDisable(true);
                controller.getQuickSearchSelectSeedParentComponent().setDisable(true);
                controller.getQuickSearchHBoxDisabledWordsParent().setDisable(true);
                break;
            case INCREMENTAL_ABSOLUTE: //intentional fall-through
            case INCREMENTAL_WORDS:
            case DECREMENTAL_ABSOLUTE:
            case DECREMENTAL_WORDS:
            case ROTATION_PRIV_FULL_NORMAL:
            case ROTATION_PRIV_FULL_PREFIXED:
            case ROTATION_PRIV_INDEX_VERTICAL:
            case ROTATION_PRIV_WORDS:
                //seed enabled, as long as search type allows it (Collision type)
                boolean blindTypeSelected = controller.getQuickSearchRadioBlindType().isSelected();
                controller.getQuickSearchCbxSelfSeed().setDisable(blindTypeSelected);
                controller.getQuickSearchSelectSeedParentComponent().setDisable((!blindTypeSelected && controller.getQuickSearchCbxSelfSeed().isSelected()) || (!controller.getQuickSearchCbxSelfSeed().isDisabled() && controller.getQuickSearchCbxSelfSeed().isSelected()));
                controller.getQuickSearchHBoxDisabledWordsParent().setDisable(SearchModeEnum.ROTATION_PRIV_FULL_NORMAL == searchMode || SearchModeEnum.ROTATION_PRIV_FULL_PREFIXED == searchMode);
                break;
            case FUZZING:
            case MIXED:
            default:
                throw new UnsupportedOperationException("This search mode is not supported in current version [searchMode=" + searchMode + "]");
        }
    }

    void addPubAccuracyMultiplierCheckboxEventHandler() {
        controller.getQuickSearchCbxOverrideScaleFactor().setOnAction(event -> {
            CheckBox chk = (CheckBox) event.getSource();
            controller.modifyPubAccuracyScaleFactorTextFieldAccess(chk.isSelected());
        });
    }
}
