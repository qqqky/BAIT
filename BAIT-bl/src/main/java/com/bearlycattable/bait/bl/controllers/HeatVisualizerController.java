package com.bearlycattable.bait.bl.controllers;

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import com.bearlycattable.bait.bl.helpers.DarkModeHelper;
import com.bearlycattable.bait.bl.initializers.AboutTheProjectTabControllerInitializer;
import com.bearlycattable.bait.bl.initializers.AdvancedTabMainControllerInitializer;
import com.bearlycattable.bait.bl.initializers.ConstructionTabControllerInitializer;
import com.bearlycattable.bait.bl.initializers.GeneralInstructionsTabControllerInitializer;
import com.bearlycattable.bait.bl.initializers.HeatComparisonTabControllerInitializer;
import com.bearlycattable.bait.bl.initializers.HeatVisualizerControllerInitializer;
import com.bearlycattable.bait.bl.initializers.QuickSearchTabControllerInitializer;
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

public class HeatVisualizerController {

    private static final Logger LOG = Logger.getLogger(HeatVisualizerController.class.getName());
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
        System.out.println("CREATING (root): HeatVisualizerController......");
        System.out.println("Original requested dimensions of the Scene: 1280x728");
        System.out.println("Monitor scaling is at " + GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration()
                .getDefaultTransform().getScaleX()*100 + "%");
        System.out.println("Current screen resolution (scaled) is: " + Screen.getPrimary().getBounds());
        System.out.println("Screen pixels are: " + Toolkit.getDefaultToolkit().getScreenSize());


        HeatVisualizerControllerInitializer.getInitializer(this).initialize();
        setThisAsParentControllerForEveryone();

        ConstructionTabControllerInitializer.getInitializer(constructionTabController, this).initialize();
        HeatComparisonTabControllerInitializer.getInitializer(heatComparisonTabController, this).initialize();
        QuickSearchTabControllerInitializer.getInitializer(quickSearchTabController, this).initialize();
        GeneralInstructionsTabControllerInitializer.getInitializer(generalInstructionsTabController, this).initialize();
        AdvancedTabMainControllerInitializer.getInitializer(advancedTabMainController, this).initialize();
        AboutTheProjectTabControllerInitializer.getInitializer(aboutTheProjectTabController, this).initialize();
        //only the 'Converter' tab doesn't have an initializer yet

        System.out.println("App language set to: " + LocaleUtils.APP_LANGUAGE);

        //dev //TODO: this is only for dev testing, delete after
        System.out.println("Initializing DEV defaults on ADVANCED_SEARCH");
        advancedTabMainController.initDevDefaults();

        //default to dark mode
        DarkModeHelper.toggleDarkModeGlobal(true, tabPaneMain, advancedTabMainController);
        advancedTabMainController.selectDarkModeOption(true);
        System.out.println("Dark mode set to: enabled");

        System.out.println("ROOT controller has been initialized successfully......");
    }

    private void setThisAsParentControllerForEveryone() {
        //tab 'Construction'
        constructionTabController.setMainController(this);

        //tab 'Heat Comparison"
        heatComparisonTabController.setMainController(this);

        //tab 'QuickSearch'
        quickSearchTabController.setMainController(this);

        //tab 'Converter'
        converterTabController.setMainController(this);

        //tab 'General instructions'
        generalInstructionsTabController.setMainController(this);

        //tab 'Advanced'
        advancedTabMainController.setMainController(this);
        advancedTabMainController.initHelper(); //this helper requires reference to mainController

        //tab 'About the project'
        aboutTheProjectTabController.setMainController(this);
    }

    void modifyPrivHeatNumberFormatChoiceBoxAccess(boolean disabled) {
        heatComparisonTabController.modifyPrivHeatNumberFormatChoiceBoxAccess(disabled);
    }

    void modifyPrivAccuracyResolutionSpinnerAccess(boolean disabled) {
        heatComparisonTabController.modifyPrivAccuracyResolutionSpinnerAccess(disabled);
    }

    void modifyAccessForShowPrivStatsButton(boolean disabled) {
        heatComparisonTabController.modifyAccessForShowPrivStatsButton(disabled);
    }

    int getNormalizedMapIndexFromComparisonResult(int resultPoints, ScaleFactorEnum scaleFactor) {
        return quickSearchTabController.getNormalizedMapIndexFromComparisonResult(resultPoints, scaleFactor);
    }

    boolean isCurrentPrivExistsInConstructionTab() {
        return constructionTabController.isCurrentPrivExists();
    }

    String getCurrentInput() {
        return constructionTabController.getCurrentInputForced();
    }

    // String getCurrentInputWordFromInputField(int wordNumber) {
    //     return constructionTabController.getCurrentInputWordFromInputField(wordNumber);
    // }
    //
    // String getCurrentInputWordFromComboBoxes(int wordNumber) {
    //     return constructionTabController.getCurrentInputWordFromComboBoxes(wordNumber);
    // }
    //
    // void setCurrentInput(String priv) {
    //     constructionTabController.setCurrentInput(priv);
    // }

    void setReferenceKeyInComparisonTab(String input, QuickSearchComparisonType type) {
        heatComparisonTabController.setReferenceKey(input, type);
    }

    void setCurrentKeyInComparisonTab(String input) {
        heatComparisonTabController.setCurrentKey(input);
    }

    void setCurrentInputForced(String priv) {
        constructionTabController.setCurrentInputForced(priv);
    }

    boolean is64HexPatternMatch(String input) {
        return HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(input).matches();
    }

    boolean is08HexPatternMatch(String input) {
        return HeatVisualizerConstants.PATTERN_SIMPLE_08.matcher(input).matches();
    }

    boolean isValidPrivPattern(String priv) {
        return priv != null && is64HexPatternMatch(priv);
    }

    boolean isValidPubPattern(String pub) {
        return pub != null && HeatVisualizerConstants.PATTERN_SIMPLE_40.matcher(pub).matches();
    }

    /*
    Words are numbered 1-8
     */
    boolean isValidWordNumber(int number) {
        return number > 0 && number < 9;
    }

    //original JavaFX style classes for TextField
    public void addDefaultCssClassesToTextField(TextField currentInputTextField) {
        currentInputTextField.getStyleClass().add("text-input");
        currentInputTextField.getStyleClass().add("text-field");
    }

    void insertSearchResultsToUiForUncompressed(PubComparisonResult result) {
        heatComparisonTabController.insertSearchResultsToUiForUncompressed(result);
    }

    void insertSearchResultsToUiForCompressed(PubComparisonResult result) {
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

    final Map<Integer, ComboBox> getPrivCompleteComboboxMappings() {
        return constructionTabController.getPrivCompleteComboboxMappings();
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

    public boolean isValidWordInComboboxesUi(int wordNum) {
       return constructionTabController.isValidWordInComboboxesUi(wordNum);
    }

    public void modifyPubAccuracyScaleFactorTextFieldAccess(boolean selected) {
        quickSearchTabController.modifyPubAccuracyScaleFactorTextFieldAccess(selected);
    }

    public TextField getTextFieldRandomWordPrefix() {
        return constructionTabController.getConstructionTextFieldRandomWordPrefix();
    }

    void setScaleFactorInComparisonTab(ScaleFactorEnum scaleFactor) {
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
