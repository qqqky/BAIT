package com.bearlycattable.bait.bl.controllers;

import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import com.bearlycattable.bait.commons.helpers.HeatVisualizerModalHelper;
import com.bearlycattable.bait.utility.BundleUtils;
import com.bearlycattable.bait.utility.LocaleUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import lombok.Getter;

public class AdvancedSubTabConfigController {

    private static final Logger LOG = Logger.getLogger(AdvancedSubTabConfigController.class.getName());
    private final ResourceBundle rb = ResourceBundle.getBundle(BundleUtils.GLOBAL_BASE_NAME + "AdvancedSubTabConfig", LocaleUtils.APP_LANGUAGE);

    @FXML
    @Getter
    private CheckBox advancedConfigCbxOverrideExactMatchPath;
    @FXML
    @Getter
    private TextField advancedConfigTextFieldExactMatchPath;
    @FXML
    @Getter
    private Button advancedConfigBtnBrowseExactMatchPath;
    @FXML
    @Getter
    private CheckBox advancedConfigCbxDarkMode;

    private AdvancedTabMainController parentController;

    @FXML
    void initialize() {
        System.out.println("CREATING (child): AdvancedSubTabConfigController......");
    }

    public void setParentController(AdvancedTabMainController parentController) {
        this.parentController = Objects.requireNonNull(parentController);
    }

    @FXML
    private void doBrowseExactMatchPath() {
        if (!advancedConfigCbxOverrideExactMatchPath.isSelected()) {
            advancedConfigBtnBrowseExactMatchPath.setDisable(true);
            return;
        }

        HeatVisualizerModalHelper.selectTxtResourceForOpen(rb.getString("label.openResource"), advancedConfigTextFieldExactMatchPath)
                .ifPresent(absPath -> advancedConfigTextFieldExactMatchPath.setText(absPath));
    }

    @FXML
    private void doSaveConfigChanges() {
        //TODO: save config to config file. The program should read the config file and act accordingly upon launch
        LOG.info("ERROR: saving of config is not implemented yet!");
    }
}
