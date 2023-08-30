package com.bearlycattable.bait.bl.controllers;


import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.util.logging.Logger;

import com.bearlycattable.bait.advancedCommons.helpers.DarkModeHelper;
import com.bearlycattable.bait.bl.contexts.HeatComparisonContext;
import com.bearlycattable.bait.bl.controllers.aboutTheProjectTab.AboutTheProjectTabController;
import com.bearlycattable.bait.bl.controllers.advancedTab.AdvancedTabMainController;
import com.bearlycattable.bait.bl.controllers.constructionTab.ConstructionTabController;
import com.bearlycattable.bait.bl.controllers.converterTab.ConverterTabController;
import com.bearlycattable.bait.bl.controllers.generalInstructionsTab.GeneralInstructionsTabController;
import com.bearlycattable.bait.bl.controllers.heatComparisonTab.HeatComparisonTabController;
import com.bearlycattable.bait.bl.controllers.quickSearchTab.QuickSearchTabController;
import com.bearlycattable.bait.bl.initializers.aboutTheProjectTab.AboutTheProjectTabControllerInitializer;
import com.bearlycattable.bait.bl.initializers.advancedTab.AdvancedTabMainControllerInitializer;
import com.bearlycattable.bait.bl.initializers.constructionTab.ConstructionTabControllerInitializer;
import com.bearlycattable.bait.bl.initializers.converterTab.ConverterTabControllerInitializer;
import com.bearlycattable.bait.bl.initializers.generalInstructionsTab.GeneralInstructionsTabControllerInitializer;
import com.bearlycattable.bait.bl.initializers.heatComparisonTab.HeatComparisonTabControllerInitializer;
import com.bearlycattable.bait.bl.initializers.quickSearchTab.QuickSearchTabControllerInitializer;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;
import com.bearlycattable.bait.utility.LocaleUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.stage.Screen;
import lombok.Getter;
import lombok.Setter;

public class RootController implements ConstructionTabAccessProxy, HeatComparisonTabAccessProxy, QuickSearchTabAccessProxy, ConverterTabAccessProxy, AdvancedTabAccessProxy, GeneralInstructionsTabAccessProxy, AboutTheProjectTabAccessProxy {

    private static final Logger LOG = Logger.getLogger(RootController.class.getName());
    @Getter @Setter
    private volatile boolean darkModeEnabled = false;
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
    private TabPane tabPaneMain;
    @FXML
    private Tab tabAdvanced;

    @FXML
    private void initialize() {
        System.out.println("CREATING (root): RootController......");
        System.out.println("Original requested dimensions of the Scene: 1280x728");
        System.out.println("Monitor scaling is at " + (int) (GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration()
                .getDefaultTransform().getScaleX() * 100) + "%");
        System.out.println("Current screen resolution (scaled) is: " + Screen.getPrimary().getBounds());
        System.out.println("Screen pixels are: " + Toolkit.getDefaultToolkit().getScreenSize());

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

    @Override
    public int getNormalizedMapIndexFromComparisonResult(int resultPoints, ScaleFactorEnum scaleFactor) {
        return quickSearchTabController.getNormalizedMapIndexFromComparisonResult(resultPoints, scaleFactor);
    }

    @Override
    public String getCurrentInput() {
        return constructionTabController.getCurrentInputForced();
    }

    @Override
    public void setCurrentInputForced(String priv) {
        constructionTabController.setCurrentInputForced(priv);
    }

    @Override
    public boolean isValidWordNumber(int number) {
        return number > 0 && number < 9;
    }

    //original JavaFX style classes for TextField
    public void addDefaultCssClassesToTextField(TextField currentInputTextField) {
        currentInputTextField.getStyleClass().add("text-input");
        currentInputTextField.getStyleClass().add("text-field");
    }

    @Override
    public void switchToParentTabX(int index) {
        int size = tabPaneMain.getTabs().size();
        if (index < 0 || index > size - 1) {
            LOG.info("Tab switch error: out of bounds for index " + index);
            return;
        }
        tabPaneMain.getSelectionModel().select(index);
    }

    @Override
    public void switchToComparisonTab() {
        switchToParentTabX(1);
    }

    @Override
    public String getUnencodedPubFromConverterTab() {
       return converterTabController.getUnencodedPublicKeyFromUi();
    }

    @Override
    public void setUnencodedPubInConverterTab(String unencodedPub) {
        converterTabController.insertUnencodedPublicKeyToUi(unencodedPub);
    }

    @Override
    public void showFullHeatComparison(HeatComparisonContext heatComparisonContext) {
        heatComparisonTabController.showFullHeatComparison(heatComparisonContext);
    }

    public void modifyAccessToAdvancedTab(boolean disabled) {
        tabAdvanced.setDisable(disabled);
    }
}
