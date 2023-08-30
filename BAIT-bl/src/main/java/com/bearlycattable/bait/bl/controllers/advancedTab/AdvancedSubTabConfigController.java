package com.bearlycattable.bait.bl.controllers.advancedTab;

import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import com.bearlycattable.bait.advancedCommons.interfaces.DarkModeControl;
import com.bearlycattable.bait.bl.controllers.advancedTab.proxyInterfaces.AdvancedConfigAccessProxy;
import com.bearlycattable.bait.commons.HeatVisualizerConstants;
import com.bearlycattable.bait.commons.helpers.HeatVisualizerModalHelper;
import com.bearlycattable.bait.utility.BundleUtils;
import com.bearlycattable.bait.utility.LocaleUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import lombok.Getter;

public class AdvancedSubTabConfigController implements DarkModeControl {

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

    // private AdvancedTabMainController parentController;
    private AdvancedConfigAccessProxy advancedConfigAccessProxy;

    @FXML
    void initialize() {
        System.out.println("CREATING (child): AdvancedSubTabConfigController......");
    }

    public void setAdvancedConfigAccessProxy(AdvancedConfigAccessProxy proxy) {
        this.advancedConfigAccessProxy = Objects.requireNonNull(proxy);
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
        System.out.println("Saving of config is not implemented in current version [" + HeatVisualizerConstants.CURRENT_VERSION + "]");
    }

    public final boolean isParentValid() {
        return advancedConfigAccessProxy != null;
    }

    @Override
    public void setDarkModeEnabled(boolean enabled) {
        advancedConfigAccessProxy.setDarkModeEnabled(enabled);
    }

    @Override
    public void refreshLogView() {
        advancedConfigAccessProxy.refreshLogView();
    }
}
