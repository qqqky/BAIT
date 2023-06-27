package com.bearlycattable.bait.bl.controllers;

import java.awt.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.bearlycattable.bait.bl.controllers.aboutTheProjectTab.AboutTheProjectTabController;
import com.bearlycattable.bait.bl.controllers.advancedTab.AdvancedTabMainController;
import com.bearlycattable.bait.bl.controllers.constructionTab.ConstructionTabController;
import com.bearlycattable.bait.bl.controllers.converterTab.ConverterTabController;
import com.bearlycattable.bait.bl.controllers.generalInstructionsTab.GeneralInstructionsTabController;
import com.bearlycattable.bait.bl.controllers.heatComparisonTab.HeatComparisonTabController;
import com.bearlycattable.bait.bl.controllers.quickSearchTab.QuickSearchTabController;
import com.bearlycattable.bait.bl.helpers.DarkModeHelper;
import com.bearlycattable.bait.bl.initializers.aboutTheProjectTab.AboutTheProjectTabControllerInitializer;
import com.bearlycattable.bait.bl.initializers.advancedTab.AdvancedTabMainControllerInitializer;
import com.bearlycattable.bait.bl.initializers.constructionTab.ConstructionTabControllerInitializer;
import com.bearlycattable.bait.bl.initializers.converterTab.ConverterTabControllerInitializer;
import com.bearlycattable.bait.bl.initializers.generalInstructionsTab.GeneralInstructionsTabControllerInitializer;
import com.bearlycattable.bait.bl.initializers.heatComparisonTab.HeatComparisonTabControllerInitializer;
import com.bearlycattable.bait.bl.initializers.RootControllerInitializer;
import com.bearlycattable.bait.bl.initializers.quickSearchTab.QuickSearchTabControllerInitializer;
import com.bearlycattable.bait.commons.HeatVisualizerConstants;
import com.bearlycattable.bait.commons.enums.JsonResultScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.JsonResultTypeEnum;
import com.bearlycattable.bait.commons.enums.QuickSearchComparisonType;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;
import com.bearlycattable.bait.commons.other.PubComparisonResult;
import com.bearlycattable.bait.utility.LocaleUtils;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;

public class RootController {

    private static final Logger LOG = Logger.getLogger(RootController.class.getName());
    private final Map<Integer, BigDecimal> similarityMappings = new HashMap<>();
    private final Map<Integer, String> colorMappings = new HashMap<>();
    @Getter @Setter
    private volatile boolean darkMode = false;
    @Getter @Setter
    private volatile boolean verboseMode = false;
    @Getter @Setter
    private volatile boolean exactMatchOnly = false;

    //child controllers
    @FXML
    private ConstructionTabController constructionTabController;
    @FXML
    private HeatComparisonTabController heatComparisonTabController;
    @FXML
    private QuickSearchTabController quickSearchTabController;
    @FXML
    private ConverterTabController converterTabController;
    @FXML
    private GeneralInstructionsTabController generalInstructionsTabController;
    @FXML
    private AdvancedTabMainController advancedTabMainController;
    @FXML
    private AboutTheProjectTabController aboutTheProjectTabController;

    //the parent tab pane
    @FXML
    @Getter
    private TabPane tabPaneMain;
    @FXML
    private Tab tabAdvanced;

    @FXML
    void initialize() {
        System.out.println("CREATING (root): RootController......");
        System.out.println("Original requested dimensions of the Scene: 1280x728");
        System.out.println("Monitor scaling is at " + (int) (GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration()
                .getDefaultTransform().getScaleX() * 100) + "%");
        System.out.println("Current screen resolution (scaled) is: " + Screen.getPrimary().getBounds());
        System.out.println("Screen pixels are: " + Toolkit.getDefaultToolkit().getScreenSize());


        RootControllerInitializer.initialize(this);

        ConstructionTabControllerInitializer.initialize(constructionTabController, this);
        HeatComparisonTabControllerInitializer.initialize(heatComparisonTabController, this);
        QuickSearchTabControllerInitializer.initialize(quickSearchTabController, this);
        GeneralInstructionsTabControllerInitializer.initialize(generalInstructionsTabController, this);
        AdvancedTabMainControllerInitializer.initialize(advancedTabMainController, this);
        AboutTheProjectTabControllerInitializer.initialize(aboutTheProjectTabController, this);
        ConverterTabControllerInitializer.initialize(converterTabController, this);

        System.out.println("App language set to: " + LocaleUtils.APP_LANGUAGE);

        //dev //TODO: this is only for dev testing, delete after
        System.out.println("Initializing DEV defaults on ADVANCED_SEARCH");
        advancedTabMainController.initDevDefaults();

        //default to dark mode
        DarkModeHelper.toggleDarkModeGlobal(true, tabPaneMain, advancedTabMainController);
        advancedTabMainController.selectDarkModeOption(true); //TODO: config from file
        System.out.println("Dark mode set to: enabled");

        System.out.println("ROOT controller has been initialized successfully......");
    }

    void modifyPrivHeatNumberFormatChoiceBoxAccess(boolean disabled) {
        heatComparisonTabController.modifyPrivHeatNumberFormatChoiceBoxAccess(disabled);
    }

    void modifyPrivAccuracyResolutionSpinnerAccess(boolean disabled) {
        heatComparisonTabController.modifyPrivAccuracyResolutionSpinnerAccess(disabled);
    }

    public void modifyAccessForShowPrivStatsButton(boolean disabled) {
        heatComparisonTabController.modifyAccessForShowPrivStatsButton(disabled);
    }

    public int getNormalizedMapIndexFromComparisonResult(int resultPoints, ScaleFactorEnum scaleFactor) {
        return quickSearchTabController.getNormalizedMapIndexFromComparisonResult(resultPoints, scaleFactor);
    }

    public boolean isCurrentPrivExistsInConstructionTab() {
        return constructionTabController.isCurrentPrivPresent();
    }

    public String getCurrentInput() {
        return constructionTabController.getCurrentInputForced();
    }

    public void setReferenceKeyInComparisonTab(String input, QuickSearchComparisonType type) {
        heatComparisonTabController.setReferenceKey(input, type);
    }

    public void setCurrentKeyInComparisonTab(String input) {
        heatComparisonTabController.setCurrentKey(input);
    }

    public void setCurrentInputForced(String priv) {
        constructionTabController.setCurrentInputForced(priv);
    }

    boolean is64HexPatternMatch(String input) {
        return HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(input).matches();
    }

    public boolean is08HexPatternMatch(String input) {
        return HeatVisualizerConstants.PATTERN_SIMPLE_08.matcher(input).matches();
    }

    public boolean isValidPrivPattern(String priv) {
        return priv != null && is64HexPatternMatch(priv);
    }

    public boolean isValidPubPattern(String pub) {
        return pub != null && HeatVisualizerConstants.PATTERN_SIMPLE_40.matcher(pub).matches();
    }

    /*
    Words are numbered 1-8
     */
    public boolean isValidWordNumber(int number) {
        return number > 0 && number < 9;
    }

    //original JavaFX style classes for TextField
    public void addDefaultCssClassesToTextField(TextField currentInputTextField) {
        currentInputTextField.getStyleClass().add("text-input");
        currentInputTextField.getStyleClass().add("text-field");
    }

    public void insertSearchResultsToUiForUncompressed(PubComparisonResult result) {
        heatComparisonTabController.insertSearchResultsToUiForUncompressed(result);
    }

    public void insertSearchResultsToUiForCompressed(PubComparisonResult result) {
        heatComparisonTabController.insertSearchResultsToUiForCompressed(result);
    }

    ScaleFactorEnum getPubAccuracyScaleFactorFromQuickSearchTab() {
        return quickSearchTabController.getPubAccuracyScaleFactorFromUi();
    }

    final void setProgrammaticChange(boolean programmaticChange) {
        constructionTabController.setProgrammaticChange(programmaticChange);
    }

    final Map<Integer, HBox> getPrivWordContainerMappings() {
        return constructionTabController.getPrivWordComboBoxParentContainerMappings();
    }

    @SuppressWarnings("rawtypes")
    final Map<Integer, ComboBox> getPrivCompleteComboBoxMappings() {
        return constructionTabController.getPrivCompleteComboBoxMappings();
    }

    public Map<Integer, BigDecimal> getSimilarityMappings() {
        return similarityMappings;
    }

    public Map<Integer, String> getColorMappings() {
        return colorMappings;
    }

    public void modifyWordComboBoxAndTextFieldAccess(int wordNum, boolean selected) {
        constructionTabController.modifyWordComboBoxAndTextFieldAccess(wordNum, selected);
    }

    public boolean isValidWordInComboBoxesUi(int wordNum) {
       return constructionTabController.isValidWordInComboBoxesUi(wordNum);
    }

    public void modifyPubAccuracyScaleFactorTextFieldAccess(boolean selected) {
        quickSearchTabController.modifyPubAccuracyScaleFactorTextFieldAccess(selected);
    }

    public TextField getTextFieldRandomWordPrefix() {
        return constructionTabController.getConstructionTextFieldRandomWordPrefix();
    }

    public void setScaleFactorInComparisonTab(ScaleFactorEnum scaleFactor) {
        heatComparisonTabController.setScaleFactor(scaleFactor);
    }

    public boolean isBackgroundThreadWorking(String currentThreadNum) {
        return advancedTabMainController.isBackgroundThreadWorking(currentThreadNum);
    }

    public void loadAdvancedSearchResultsToUi(String pathToResultFile, Map<String, Map<JsonResultTypeEnum, Map<JsonResultScaleFactorEnum, Pair<String, Integer>>>> mapForResults) {
        advancedTabMainController.loadAdvancedSearchResultsToUi(pathToResultFile, mapForResults);
    }

    public void switchToTab(int i) {
        int size = tabPaneMain.getTabs().size();
        if (i < 0 || i > size - 1) {
            LOG.info("Tab switch error: out of bounds for index " + i);
            return;
        }
        tabPaneMain.getSelectionModel().select(i);
    }

    public void switchToComparisonTab() {
        switchToTab(1);
    }

    public String getUnencodedPubFromConverterTab() {
       return converterTabController.getUnencodedPublicKeyFromUi();
    }

    public void setUnencodedPubInConverterTab(String unencodedPub) {
        converterTabController.insertUnencodedPublicKeyToUi(unencodedPub);
    }

    public void calculateOutputs() {
        heatComparisonTabController.calculateOutputs();
    }

    public void modifyAccessToAdvancedTab(boolean disabled) {
        tabAdvanced.setDisable(disabled);
    }
}
