package com.bearlycattable.bait.bl.controllers.advancedTab;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Logger;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.bearlycattable.bait.advanced.interfaceImpls.AdvancedTaskControlImpl;
import com.bearlycattable.bait.advanced.interfaces.AdvancedTaskControl;
import com.bearlycattable.bait.advanced.providers.UnencodedAddressListReaderProvider;
import com.bearlycattable.bait.advancedCommons.contexts.P2PKHSingleResultData;
import com.bearlycattable.bait.advancedCommons.dataAccessors.SeedMutationConfigDataAccessor;
import com.bearlycattable.bait.advancedCommons.dataAccessors.ThreadComponentDataAccessor;
import com.bearlycattable.bait.advancedCommons.helpers.DarkModeHelper;
import com.bearlycattable.bait.advancedCommons.interfaces.AdvancedTaskControlAccessProxy;
import com.bearlycattable.bait.advancedCommons.interfaces.DarkModeControl;
import com.bearlycattable.bait.advancedCommons.models.ThreadSpawnModel;
import com.bearlycattable.bait.bl.contexts.HeatComparisonContext;
import com.bearlycattable.bait.bl.controllers.AdvancedTabAccessProxy;
import com.bearlycattable.bait.bl.controllers.advancedTab.proxyInterfaces.AdvancedConfigAccessProxy;
import com.bearlycattable.bait.bl.controllers.advancedTab.proxyInterfaces.AdvancedInstructionsAccessProxy;
import com.bearlycattable.bait.bl.controllers.advancedTab.proxyInterfaces.AdvancedLogAccessProxy;
import com.bearlycattable.bait.bl.controllers.advancedTab.proxyInterfaces.AdvancedProgressAccessProxy;
import com.bearlycattable.bait.bl.controllers.advancedTab.proxyInterfaces.AdvancedResultsAccessProxy;
import com.bearlycattable.bait.bl.controllers.advancedTab.proxyInterfaces.AdvancedSearchAccessProxy;
import com.bearlycattable.bait.bl.controllers.advancedTab.proxyInterfaces.AdvancedToolsAccessProxy;
import com.bearlycattable.bait.bl.initializers.advancedTab.AdvancedSubTabConfigControllerInitializer;
import com.bearlycattable.bait.bl.initializers.advancedTab.AdvancedSubTabInstructionsControllerInitializer;
import com.bearlycattable.bait.bl.initializers.advancedTab.AdvancedSubTabLogControllerInitializer;
import com.bearlycattable.bait.bl.initializers.advancedTab.AdvancedSubTabProgressControllerInitializer;
import com.bearlycattable.bait.bl.initializers.advancedTab.AdvancedSubTabResultsControllerInitializer;
import com.bearlycattable.bait.bl.initializers.advancedTab.AdvancedSubTabSearchControllerInitializer;
import com.bearlycattable.bait.bl.initializers.advancedTab.AdvancedSubTabToolsControllerInitializer;
import com.bearlycattable.bait.commons.enums.BackgroundColorEnum;
import com.bearlycattable.bait.commons.enums.JsonResultScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.JsonResultTypeEnum;
import com.bearlycattable.bait.commons.enums.LogTextTypeEnum;
import com.bearlycattable.bait.commons.enums.TextColorEnum;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.util.Pair;
import lombok.Getter;

/**
 * //TODO: don't need this, right?
 * Dev notes:
 * 1. Do not use more than 10K entries for multiple search (GC error while initializing cache)!
 *      can make it use the non-cached version of the search, but it is too slow.
 * 2. The 'exactMatchCheck' map can hold any number of entries up to ~Integer.MAX_VALUE, but maybe ~1.5mil
 *      is enough?
 * 3. LOG at SF1 rules (recommended):
 *      For avg results of <56 - do not log the 'key swap' at all (or it just spams console too much)
 *      For avg results of <60 - log every 5K
 *      For avg results of 60+ - can log every 10K or more (when avg points are ~1500 SF1)
 */

public class AdvancedTabMainController implements AdvancedToolsAccessProxy, AdvancedSearchAccessProxy, AdvancedResultsAccessProxy,
        AdvancedProgressAccessProxy, AdvancedConfigAccessProxy, AdvancedInstructionsAccessProxy, AdvancedLogAccessProxy, AdvancedTaskControlAccessProxy, DarkModeControl {

    private static final Logger LOG = Logger.getLogger(AdvancedTabMainController.class.getName());
    @Getter
    private final UnencodedAddressListReaderProvider addressReaderProvider = ServiceLoader.load(UnencodedAddressListReaderProvider.class).findFirst().orElse(null);

    private AdvancedTabAccessProxy advancedTabAccessProxy;
    @Getter
    private volatile AdvancedTaskControl advancedTaskControl;

    //child controllers
    @FXML
    private AdvancedSubTabSearchController advancedSubTabSearchController;
    @FXML
    private AdvancedSubTabToolsController advancedSubTabToolsController;
    @FXML
    private AdvancedSubTabProgressController advancedSubTabProgressController;
    @FXML
    private AdvancedSubTabResultsController advancedSubTabResultsController;
    @FXML
    private AdvancedSubTabLogController advancedSubTabLogController;
    @FXML
    private AdvancedSubTabInstructionsController advancedSubTabInstructionsController;
    @FXML
    private AdvancedSubTabConfigController advancedSubTabConfigController;

    @FXML
    private Tab advancedSearchTab;

    public void setAdvancedTabAccessProxy(AdvancedTabAccessProxy proxy) {
        this.advancedTabAccessProxy = proxy;
    }

    @FXML
    void initialize() {
        System.out.println("CREATING (parent, advanced): AdvancedTabMainController");

        AdvancedSubTabSearchControllerInitializer.initialize(advancedSubTabSearchController, this);
        AdvancedSubTabProgressControllerInitializer.initialize(advancedSubTabProgressController, this);
        AdvancedSubTabResultsControllerInitializer.initialize(advancedSubTabResultsController, this);
        AdvancedSubTabInstructionsControllerInitializer.initialize(advancedSubTabInstructionsController, this);
        AdvancedSubTabLogControllerInitializer.initialize(advancedSubTabLogController, this);
        AdvancedSubTabConfigControllerInitializer.initialize(advancedSubTabConfigController, this);
        AdvancedSubTabToolsControllerInitializer.initialize(advancedSubTabToolsController, this);

        advancedTaskControl = AdvancedTaskControlImpl.getInstance();
        advancedTaskControl.initialize((AdvancedTaskControlAccessProxy) this);
    }

    public void modifyAutomergeAccessInProgressSubTab(boolean enabled) {
        advancedSubTabProgressController.modifyAutomergeAccess(enabled);
    }

    public boolean isAutomergePossible() {
        return isAutomergeEnabledInProgressSubTab() && advancedTaskControl.isAllCurrentTasksDone() && advancedTaskControl.isMoreThanOneResultAvailable();
    }

    boolean isAutomergeEnabledInProgressSubTab() {
        return advancedSubTabProgressController.isAutomergeEnabled();
    }

    @Override
    public boolean isBackgroundThreadWorking(String currentThreadNum) {
        return advancedTaskControl.isBackgroundThreadWorking(currentThreadNum);
    }

    public Optional<String> getOverriddenExactMatchCheckPathToAddresses() {
        CheckBox cbxEmo = advancedSubTabConfigController.getAdvancedConfigCbxOverrideExactMatchPath();
        if (cbxEmo.isDisabled() || !cbxEmo.isSelected()) {
            return Optional.empty();
        }

        return Optional.of(advancedSubTabConfigController.getAdvancedConfigTextFieldExactMatchPath().getText());
    }

    // private void addRedBorder(Control component) {
    //     if (!component.getStyleClass().contains(CssConstants.BORDER_RED)) {
    //         component.getStyleClass().add(CssConstants.BORDER_RED);
    //     }
    // }

    @Override
    public void insertThreadInfoLabelsToUi(String parentThreadId, String childThreadId, List<String> infoLabels) {
        VBox node = findChildInfoVBox(parentThreadId, childThreadId);

        node.getChildren().clear();
        infoLabels.forEach(labelText -> node.getChildren().add(new Label(labelText)));

        if (advancedTabAccessProxy != null) {
            DarkModeHelper.toggleDarkModeForComponent(advancedTabAccessProxy.isDarkModeEnabled(), node);
        }
    }

    @Override
    public void removeThreadInfoLabelsFromUi(String parentThreadId, String childThreadId) {
        findChildInfoVBox(parentThreadId, childThreadId).getChildren().clear();
    }

    private VBox findChildInfoVBox(String parentThreadId, String childThreadId) {
        return advancedSubTabProgressController.findChildInfoVBox(parentThreadId, childThreadId);
    }

    @Override
    public void exportDataToCurrentInputFieldInMainTab(String priv, AdvancedSubTabSearchController caller) {
        if (caller != this.advancedSubTabSearchController) {
            throw new IllegalCallerException("Wrong caller at AdvancedTabMainController#exportDataToCurrentInputFieldInMainTab");
        }
        advancedTabAccessProxy.setCurrentInputForced(priv);
    }

    @Override
    public String buildMutatedSeed(@NonNull String seed, List<Integer> disabledWords, SeedMutationConfigDataAccessor accessor) {
        return advancedTaskControl.buildMutatedSeed(seed, disabledWords, accessor);
    }

    @Override
    @NonNull
    public Set<String> readUnencodedPubsListIntoSet(String pathToUnencodedAddressesFile) {
        return addressReaderProvider.readUnencodedPubsListIntoSet(pathToUnencodedAddressesFile);
    }

    @Override
    public String importDataFromCurrentInputFieldInMainTab(AdvancedSubTabSearchController caller) {
        if (caller != this.advancedSubTabSearchController) {
            throw new IllegalCallerException("Wrong caller at AdvancedTabMainController#importDataFromCurrentInputFieldInMainTab");
        }
        return advancedTabAccessProxy.getCurrentInput();
    }

    @Override
    public void showFullHeatComparison(HeatComparisonContext heatComparisonContext) {
        advancedTabAccessProxy.showFullHeatComparison(heatComparisonContext);
    }

    public void switchToParentTabX(int index) {
        advancedTabAccessProxy.switchToParentTabX(index);
    }

    @Override
    public void switchToChildTabX(int index) {
        advancedSearchTab.getTabPane().getSelectionModel().select(index);
    }

    @Override
    public synchronized final Optional<ThreadComponentDataAccessor> addNewThreadProgressContainerToProgressAndResultsTab(@Nullable String parentThreadNum, @Nullable String titleMessage) {
        return advancedSubTabProgressController.addNewThreadProgressContainerToUi(parentThreadNum, titleMessage);
    }

    @Override
    public synchronized final boolean removeThreadProgressContainerFromProgressAndResultsTab(String threadNum) {
        return advancedSubTabProgressController.removeThreadProgressContainerFromUi(threadNum);
    }

    @Override
    public Optional<String> getAutomergePathFromProgressAndResultsTab() {
        return advancedSubTabProgressController.getAutomergePath();
    }

    @Override
    public void showErrorMessageInAdvancedSearchSubTab(String error) {
        advancedSubTabSearchController.showErrorMessage(error);
    }

    @Override
    public void insertErrorOrSuccessMessageInAdvancedProgressSubTab(String message, TextColorEnum color) {
        advancedSubTabProgressController.insertErrorOrSuccessMessage(message, color);
    }

    @Override
    public void disableAdvancedSearchBtn(boolean disable) {
        advancedSubTabSearchController.getAdvancedBtnSearch().setDisable(disable);
    }

    @Override
    public void logToUi(String message, Color color, LogTextTypeEnum type) {
        advancedSubTabLogController.log(message, color, 0, null, type);
    }

    @Override
    public void logToUiBold(String message, Color color, LogTextTypeEnum type) {
        advancedSubTabLogController.log(message, color, 0, FontWeight.BOLD, type);
    }

    public void initDevDefaults() {
        advancedSubTabSearchController.initDevDefaults();
    }

    @Override
    public void setBackgroundColorForProgressHBox(String threadNum, String childThreadNum, BackgroundColorEnum color) {
        advancedSubTabProgressController.setBackgroundColorForProgressHBox(threadNum, childThreadNum, color);
    }

    @Override
    public void switchToComparisonTab() {
        switchToParentTabX(1);
    }

    @Override
    public boolean isDarkModeEnabled() {
        return advancedTabAccessProxy != null && advancedTabAccessProxy.isDarkModeEnabled();
    }

    @Override
    public void setDarkMode(boolean enabled) {
        advancedSubTabConfigController.setDarkMode(true);
        advancedSubTabLogController.getAdvancedLogListView().refresh();
    }

    @Override
    public void setDarkModeFlag(boolean enabled) {
        advancedTabAccessProxy.setDarkModeFlag(enabled);
    }

    // @Override
    // public void refreshLogView() {
    //     advancedSubTabLogController.getAdvancedLogListView().refresh();
    // }

    @Override
    public boolean isVerboseMode() {
        return advancedTabAccessProxy.isVerboseMode();
    }

    public final boolean isParentValid() {
        return advancedTabAccessProxy != null;
    }

    @Override
    public Optional<String> spawnBackgroundSearchThread(ThreadSpawnModel threadSpawnModel, @NonNull AdvancedSubTabSearchController caller) {
        if (this.advancedSubTabSearchController == null || this.advancedSubTabSearchController != caller) {
            //TODO: maybe some error here is needed?
            return Optional.empty();
        }

        return advancedTaskControl.spawnBackgroundSearchThread(threadSpawnModel);
    }

    @Override
    public boolean isTaskCreationAllowed(@NonNull AdvancedSubTabSearchController caller) {
        if (this.advancedSubTabSearchController == null || this.advancedSubTabSearchController != caller) {
            return false;
        }
        return advancedTaskControl.isTaskCreationAllowed();
    }

    @Override
    public int readAndTestFile(String pathToUnencodedAddresses, AdvancedSubTabToolsController caller) {
        if (caller != this.advancedSubTabToolsController) {
            throw new IllegalCallerException("Wrong caller at AdvancedTabMainController#readAndTestFile");
        }
        return addressReaderProvider.readAndTestFile(pathToUnencodedAddresses);
    }

    @Override
    @NonNull
    public P2PKHSingleResultData[] createTemplateFromFile(String pathToUnencodedAddressesFile, int max, AdvancedSubTabToolsController caller) {
        if (caller != this.advancedSubTabToolsController) {
            throw new IllegalCallerException("Wrong caller at AdvancedTabMainController#createTemplateFromFile");
        }
        return addressReaderProvider.createTemplateFromFile(pathToUnencodedAddressesFile, max);
    }

    @Override
    public Optional<P2PKHSingleResultData[]> createTemplateFromStringList(List<String> unencodedAddresses, int max, AdvancedSubTabToolsController caller) {
        if (caller != this.advancedSubTabToolsController) {
            throw new IllegalCallerException("Wrong caller at AdvancedTabMainController#createTemplateFromStringList");
        }
        return addressReaderProvider.createTemplateFromStringList(unencodedAddresses, max);
    }

    @Override
    public void loadAdvancedSearchResultsToUi(String pathToResultFile, Map<String, Map<JsonResultTypeEnum, Map<JsonResultScaleFactorEnum, Pair<String, Integer>>>> advancedSearchResultsAsMap) {
        advancedSubTabResultsController.loadAdvancedSearchResultsToUi(pathToResultFile, advancedSearchResultsAsMap);
    }
}
