package com.bearlycattable.bait.bl.controllers.advancedTab;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.bearlycattable.bait.advanced.interfaceImpls.AdvancedTaskControlImpl;
import com.bearlycattable.bait.advanced.interfaces.AdvancedTaskControl;
import com.bearlycattable.bait.advanced.providers.UnencodedAddressListReaderProvider;
import com.bearlycattable.bait.advancedCommons.dataAccessors.ThreadComponentDataAccessor;
import com.bearlycattable.bait.advancedCommons.helpers.DarkModeHelper;
import com.bearlycattable.bait.advancedCommons.interfaces.AdvancedTabCommandExecutor;
import com.bearlycattable.bait.bl.controllers.RootController;
import com.bearlycattable.bait.bl.initializers.advancedTab.AdvancedSubTabConfigControllerInitializer;
import com.bearlycattable.bait.bl.initializers.advancedTab.AdvancedSubTabInstructionsControllerInitializer;
import com.bearlycattable.bait.bl.initializers.advancedTab.AdvancedSubTabLogControllerInitializer;
import com.bearlycattable.bait.bl.initializers.advancedTab.AdvancedSubTabProgressControllerInitializer;
import com.bearlycattable.bait.bl.initializers.advancedTab.AdvancedSubTabResultsControllerInitializer;
import com.bearlycattable.bait.bl.initializers.advancedTab.AdvancedSubTabSearchControllerInitializer;
import com.bearlycattable.bait.bl.initializers.advancedTab.AdvancedSubTabToolsControllerInitializer;
import com.bearlycattable.bait.advancedCommons.models.ThreadSpawnModel;
import com.bearlycattable.bait.commons.CssConstants;
import com.bearlycattable.bait.commons.enums.BackgroundColorEnum;
import com.bearlycattable.bait.commons.enums.JsonResultScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.JsonResultTypeEnum;
import com.bearlycattable.bait.commons.enums.LogTextTypeEnum;
import com.bearlycattable.bait.commons.enums.QuickSearchComparisonType;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.TextColorEnum;

import javafx.fxml.FXML;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.util.Pair;
import lombok.Getter;

/**
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

public class AdvancedTabMainController implements AdvancedTaskProxy, AdvancedTabCommandExecutor {

    private static final Logger LOG = Logger.getLogger(AdvancedTabMainController.class.getName());
    @Getter
    private final UnencodedAddressListReaderProvider addressReaderProvider = ServiceLoader.load(UnencodedAddressListReaderProvider.class).findFirst().orElse(null);

    private RootController rootController;

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

    public void setRootController(RootController rootController) {
        this.rootController = rootController;
    }

    @FXML
    void initialize() {
        System.out.println("CREATING (parent): AdvancedTabMainController");

        AdvancedSubTabSearchControllerInitializer.initialize(advancedSubTabSearchController, this);
        AdvancedSubTabProgressControllerInitializer.initialize(advancedSubTabProgressController, this);
        AdvancedSubTabResultsControllerInitializer.initialize(advancedSubTabResultsController, this);
        AdvancedSubTabInstructionsControllerInitializer.initialize(advancedSubTabInstructionsController, this);
        AdvancedSubTabLogControllerInitializer.initialize(advancedSubTabLogController, this);
        AdvancedSubTabConfigControllerInitializer.initialize(advancedSubTabConfigController, this);
        AdvancedSubTabToolsControllerInitializer.initialize(advancedSubTabToolsController, this);

        advancedTaskControl = AdvancedTaskControlImpl.getInstance();
        advancedTaskControl.initialize((AdvancedTabCommandExecutor) this);
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

    public boolean isBackgroundThreadWorking(String currentThreadNum) {
        return advancedTaskControl.isBackgroundThreadWorking(currentThreadNum);
    }

    private void addRedBorder(Control component) {
        if (!component.getStyleClass().contains(CssConstants.BORDER_RED)) {
            component.getStyleClass().add(CssConstants.BORDER_RED);
        }
    }

    @Override
    public void insertThreadInfoLabelsToUi(String parentThreadId, String childThreadId, List<String> infoLabels) {
        VBox node = findChildInfoVBox(parentThreadId, childThreadId);

        node.getChildren().clear();
        infoLabels.forEach(labelText -> node.getChildren().add(new Label(labelText)));

        if (rootController != null) {
            DarkModeHelper.toggleDarkModeForComponent(rootController.isDarkMode(), node);
        }
    }

    @Override
    public void removeThreadInfoLabelsFromUi(String parentThreadId, String childThreadId) {
        findChildInfoVBox(parentThreadId, childThreadId).getChildren().clear();
    }

    private VBox findChildInfoVBox(String parentThreadId, String childThreadId) {
        return advancedSubTabProgressController.findChildInfoVBox(parentThreadId, childThreadId);
    }

    public void exportDataToCurrentInputFieldInMainTab(String priv) {
        rootController.setCurrentInputForced(priv);
    }

    public String importDataFromCurrentInputFieldInMainTab() {
        return rootController.getCurrentInput();
    }

    public void setScaleFactorInComparisonTab(ScaleFactorEnum scaleFactor) {
        rootController.setScaleFactorInComparisonTab(scaleFactor);
    }

    public void calculateOutputsInHeatComparisonTab() {
        rootController.calculateOutputs();
    }

    public void switchToParentTabX(int index) {
        rootController.getTabPaneMain().getSelectionModel().select(index);
    }

    public void switchToChildTabX(int index) {
        advancedSearchTab.getTabPane().getSelectionModel().select(index);
    }

    public synchronized void loadAdvancedSearchResultsToUi(String pathToResultFile, Map<String, Map<JsonResultTypeEnum, Map<JsonResultScaleFactorEnum, Pair<String, Integer>>>> advancedSearchResultsAsMap) {
        advancedSubTabResultsController.loadAdvancedSearchResultsToUi(pathToResultFile, advancedSearchResultsAsMap);
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

    public Map<Integer, BigDecimal> getSimilarityMappings() {
        return rootController.getSimilarityMappings();
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

    public void modifyAccessToFilterBtn(boolean enabled) {
        advancedSubTabResultsController.modifyAccessToFilterBtn(enabled);
    }

    @Override
    public void setBackgroundColorForProgressHBox(String threadNum, String childThreadNum, BackgroundColorEnum color) {
        advancedSubTabProgressController.setBackgroundColorForProgressHBox(threadNum, childThreadNum, color);
    }

    public void setReferenceKeyInComparisonTab(String input, QuickSearchComparisonType type) {
        rootController.setReferenceKeyInComparisonTab(input, type);
    }

    public void setCurrentKeyInComparisonTab(String input) {
        rootController.setCurrentKeyInComparisonTab(input);
    }

    public void switchToComparisonTab() {
        switchToParentTabX(1);
    }

    @Override
    public boolean isDarkModeEnabled() {
        return rootController != null && rootController.isDarkMode();
    }

    public boolean isExactMatchCheckOnlyEnabled() {
        return rootController != null && rootController.isExactMatchOnly();
    }

    @Override
    public void setDarkMode(boolean enabled) {
        rootController.setDarkMode(enabled);
    }

    void setExactMatchCheckOnly(boolean enabled) {
        rootController.setExactMatchOnly(enabled);
    }

    @Override
    public void refreshLogView() {
        advancedSubTabLogController.getAdvancedLogListView().refresh();
    }

    public void selectDarkModeOption(boolean select) {
        advancedSubTabConfigController.getAdvancedConfigCbxDarkMode().setSelected(select);
    }

    @Override
    public boolean isVerboseMode() {
        return rootController.isVerboseMode();
    }

    public final boolean isParentValid() {
        return rootController != null;
    }

    @Override
    public Optional<String> spawnBackgroundSearchThread(ThreadSpawnModel threadSpawnModel, @NonNull AdvancedSubTabSearchController controller) {
        if (this.advancedSubTabSearchController != controller) {
            return Optional.empty();
        }

        return advancedTaskControl.spawnBackgroundSearchThread(threadSpawnModel);
    }

    @Override
    public boolean isTaskCreationAllowed(@NonNull AdvancedSubTabSearchController controller) {
        if (this.advancedSubTabSearchController != controller) {
            return false;
        }

        return advancedTaskControl.isTaskCreationAllowed();
    }
}
