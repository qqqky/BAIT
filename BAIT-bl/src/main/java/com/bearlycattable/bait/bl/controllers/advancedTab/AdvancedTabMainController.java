package com.bearlycattable.bait.bl.controllers.advancedTab;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.bearlycattable.bait.advanced.providers.UnencodedAddressListReaderProvider;
import com.bearlycattable.bait.bl.contexts.TaskPreparationContext;
import com.bearlycattable.bait.bl.controllers.RootController;
import com.bearlycattable.bait.bl.helpers.AdvancedTabPageHelper;
import com.bearlycattable.bait.bl.helpers.DarkModeHelper;
import com.bearlycattable.bait.bl.initializers.advancedTab.AdvancedSubTabConfigControllerInitializer;
import com.bearlycattable.bait.bl.initializers.advancedTab.AdvancedSubTabInstructionsControllerInitializer;
import com.bearlycattable.bait.bl.initializers.advancedTab.AdvancedSubTabLogControllerInitializer;
import com.bearlycattable.bait.bl.initializers.advancedTab.AdvancedSubTabProgressControllerInitializer;
import com.bearlycattable.bait.bl.initializers.advancedTab.AdvancedSubTabResultsControllerInitializer;
import com.bearlycattable.bait.bl.initializers.advancedTab.AdvancedSubTabSearchControllerInitializer;
import com.bearlycattable.bait.advancedCommons.contexts.P2PKHSingleResultData;
import com.bearlycattable.bait.bl.initializers.advancedTab.AdvancedSubTabToolsControllerInitializer;
import com.bearlycattable.bait.commons.CssConstants;
import com.bearlycattable.bait.commons.contexts.TaskDiagnosticsModel;
import com.bearlycattable.bait.commons.dataAccessors.ThreadComponentDataAccessor;
import com.bearlycattable.bait.commons.enums.BackgroundColorEnum;
import com.bearlycattable.bait.commons.enums.JsonResultScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.JsonResultTypeEnum;
import com.bearlycattable.bait.commons.enums.LogTextTypeEnum;
import com.bearlycattable.bait.commons.enums.QuickSearchComparisonType;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.TextColorEnum;

import javafx.concurrent.Task;
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

public class AdvancedTabMainController {

    private static final Logger LOG = Logger.getLogger(AdvancedTabMainController.class.getName());
    private static final List<Integer> disabledWords = new ArrayList<>();
    @Getter
    private final UnencodedAddressListReaderProvider addressReaderProvider = ServiceLoader.load(UnencodedAddressListReaderProvider.class).findFirst().orElse(null);

    private RootController rootController;

    @Getter
    private final Map<String, Task<P2PKHSingleResultData[]>> taskMap = new HashMap<>();
    @Getter
    private final Map<String, P2PKHSingleResultData[]> taskResultsMap = new HashMap<>();

    @Getter
    //Format: parentId, childId, TaskDiagnosticsModel
    private final Map<String, Map<String, TaskDiagnosticsModel>> taskDiagnosticsTree = new HashMap<>();
    @Getter
    private volatile AdvancedTabPageHelper helper;

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

    public void initAdvancedTabPageHelper(RootController rootController) {
        if (this.helper == null) {
            this.helper = AdvancedTabPageHelper.create(Objects.requireNonNull(rootController), this);
        }
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
    }

    public void modifyAutomergeAccessInProgressSubTab(boolean enabled) {
        advancedSubTabProgressController.modifyAutomergeAccess(enabled);
    }

    public boolean isAutomergePossible() {
        return isAutomergeEnabledInProgressSubTab() && isAllCurrentTasksDone() && isMoreThanOneResultAvailable();
    }

    boolean isAutomergeEnabledInProgressSubTab() {
        return advancedSubTabProgressController.isAutomergeEnabled();
    }

    private boolean isMoreThanOneResultAvailable() {
        return taskResultsMap.keySet().size() > 1;
    }

    //TODO: another class should keep track of the tasks (also check other methods that are not needed)...
    public synchronized boolean isAllCurrentTasksDone() {
        return taskMap.keySet().stream().allMatch(key -> taskMap.get(key).isDone());
    }

    public boolean isBackgroundThreadWorking(String currentThreadNum) {
        if (!taskMap.containsKey(currentThreadNum)) {
            return false;
        }
        return !taskMap.get(currentThreadNum).isDone();
    }

    private void addRedBorder(Control component) {
        if (!component.getStyleClass().contains(CssConstants.BORDER_RED)) {
            component.getStyleClass().add(CssConstants.BORDER_RED);
        }
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

    public synchronized final Optional<ThreadComponentDataAccessor> addNewThreadProgressContainerToProgressAndResultsTab(@Nullable String parentThreadNum, @Nullable String titleMessage) {
        return advancedSubTabProgressController.addNewThreadProgressContainerToUi(parentThreadNum, titleMessage);
    }

    public synchronized final boolean removeThreadProgressContainerFromProgressAndResultsTab(String threadNum) {
        return advancedSubTabProgressController.removeThreadProgressContainerFromUi(threadNum);
    }

    public Optional<String> getAutomergePathFromProgressAndResultsTab() {
        return advancedSubTabProgressController.getAutomergePath();
    }

    public Map<Integer, BigDecimal> getSimilarityMappings() {
        return rootController.getSimilarityMappings();
    }

    public void showErrorMessageInAdvancedSearchSubTab(String error) {
        advancedSubTabSearchController.showErrorMessage(error);
    }

    public void insertErrorOrSuccessMessageInAdvancedProgressSubTab(String message, TextColorEnum color) {
        advancedSubTabProgressController.insertErrorOrSuccessMessage(message, color);
    }

    void prepareTask(TaskPreparationContext taskPreparationContext) {
        helper.prepareTask(taskPreparationContext);
    }

    public void insertThreadInfoLabelsToUi(String parentThreadId, String childThreadId, List<String> infoLabels) {
        VBox node = findChildInfoVBox(parentThreadId, childThreadId);

        node.getChildren().clear();
        infoLabels.stream()
                .forEach(labelText -> node.getChildren().add(new Label(labelText)));

        if (rootController != null) {
            DarkModeHelper.toggleDarkModeForComponent(rootController.isDarkMode(), node);
        }
    }

    public void removeThreadInfoLabelsFromUi(String parentThreadId, String childThreadId) {
        findChildInfoVBox(parentThreadId, childThreadId).getChildren().clear();
    }

    private VBox findChildInfoVBox(String parentThreadId, String childThreadId) {
        return advancedSubTabProgressController.findChildInfoVBox(parentThreadId, childThreadId);
    }

    public void disableWord(int wordNum) {
        if (!isValidWordNum(wordNum)) {
            throw new IllegalArgumentException("Words are numbered 1-8. Received: " + wordNum);
        }

        if (!disabledWords.contains(wordNum)) {
            disabledWords.add(wordNum);
        }
    }

    public void enableWord(int wordNum) {
        if (!isValidWordNum(wordNum)) {
            throw new IllegalArgumentException("Words are numbered 1-8. Received: " + wordNum);
        }

        if (disabledWords.contains(wordNum)) {
            disabledWords.remove((Integer) wordNum);
        }
    }

    private boolean isValidWordNum(int wordNum) {
        return wordNum > 0 && wordNum < 9;
    }

    public void logToUi(String message, Color color, LogTextTypeEnum type) {
        advancedSubTabLogController.log(message, color, 0, null, type);
    }

    public void logToUiBold(String message, Color color, LogTextTypeEnum type) {
        advancedSubTabLogController.log(message, color, 0, FontWeight.BOLD, type);
    }

    public void initDevDefaults() {
        advancedSubTabSearchController.initDevDefaults();
    }

    public void modifyAccessToFilterBtn(boolean enabled) {
        advancedSubTabResultsController.modifyAccessToFilterBtn(enabled);
    }

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

    public boolean isDarkModeEnabled() {
        return rootController != null && rootController.isDarkMode();
    }

    public boolean isExactMatchCheckOnlyEnabled() {
        return rootController != null && rootController.isExactMatchOnly();
    }

    public void setDarkMode(boolean enabled) {
        rootController.setDarkMode(enabled);
    }

    void setExactMatchCheckOnly(boolean enabled) {
        rootController.setExactMatchOnly(enabled);
    }

    public void refreshLogView() {
        advancedSubTabLogController.getAdvancedLogListView().refresh();
    }

    public void selectDarkModeOption(boolean select) {
        advancedSubTabConfigController.getAdvancedConfigCbxDarkMode().setSelected(select);
    }

    public boolean isVerboseMode() {
        return rootController.isVerboseMode();
    }

    public final boolean isParentValid() {
        return rootController != null;
    }
}
