package com.bearlycattable.bait.bl.helpers;

import java.awt.Toolkit;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.bearlycattable.bait.advancedCommons.helpers.P2PKHSingleResultDataHelper;
import com.bearlycattable.bait.bl.contexts.TaskPreparationContext;
import com.bearlycattable.bait.bl.controllers.AdvancedTabMainController;
import com.bearlycattable.bait.bl.controllers.HeatVisualizerController;
import com.bearlycattable.bait.commons.Config;
import com.bearlycattable.bait.advancedCommons.contexts.P2PKHSingleResultData;
import com.bearlycattable.bait.commons.contexts.TaskDiagnosticsModel;
import com.bearlycattable.bait.advancedCommons.models.ThreadSpawnModel;
import com.bearlycattable.bait.commons.dataAccessors.SeedMutationConfigDataAccessor;
import com.bearlycattable.bait.commons.dataAccessors.ThreadComponentDataAccessor;
import com.bearlycattable.bait.commons.enums.BackgroundColorEnum;
import com.bearlycattable.bait.commons.enums.LogTextTypeEnum;
import com.bearlycattable.bait.commons.enums.OutputCaseEnum;
import com.bearlycattable.bait.commons.enums.RandomWordPrefixMutationTypeEnum;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.enums.SeedMutationTypeEnum;
import com.bearlycattable.bait.commons.enums.TextColorEnum;
import com.bearlycattable.bait.utility.AddressModifier;
import com.bearlycattable.bait.utility.BundleUtils;
import com.bearlycattable.bait.utility.LocaleUtils;
import com.bearlycattable.bait.utility.PathUtils;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.util.Pair;

public class AdvancedTabPageHelper {

    private static final Logger LOG = Logger.getLogger(AdvancedTabPageHelper.class.getName());
    private final ResourceBundle rb = ResourceBundle.getBundle(BundleUtils.GLOBAL_BASE_NAME + "AdvancedTabPageHelper", LocaleUtils.APP_LANGUAGE);

    private final AddressModifier modifier = new AddressModifier(OutputCaseEnum.UPPERCASE);
    private final AdvancedTabMainController advancedTabMainController;
    private final HeatVisualizerController mainController;

    private AdvancedTabPageHelper(HeatVisualizerController mainController, AdvancedTabMainController controller) {
        this.advancedTabMainController = Objects.requireNonNull(controller);
        this.mainController = Objects.requireNonNull(mainController);
    }

    private AdvancedTabPageHelper() {
        throw new IllegalStateException("Creation not allowed");
    }

    public static AdvancedTabPageHelper create(HeatVisualizerController mainController, AdvancedTabMainController controller) {
        return new AdvancedTabPageHelper(mainController, controller);
    }

    public void prepareTask(TaskPreparationContext context) {
        ThreadComponentDataAccessor accessor = context.getAccessor();
        Task<P2PKHSingleResultData[]> searchTask = context.getSearchTask();

        //bind progress bar and progress label
        accessor.getProgressBar().progressProperty().bind(searchTask.progressProperty());
        accessor.getProgressLabel().textProperty().bind(context.getObservableProgressLabelValue());

        String threadNum = accessor.getThreadNum();

        //prepare buttons
        accessor.getStopThreadButton().setOnAction(event -> showStopThreadConfirmationDialog(threadNum, accessor));
        accessor.getShowHideInfoButton().setDisable(false);
        List<String> infoForCurrentLoopList = context.buildDetailedRunInfoForUi();

        accessor.getShowHideInfoButton().setOnAction(event -> {
            Button btn = (Button)event.getSource();
            String currentText = btn.getText();
            String parentThreadId = context.getThreadSpawnModel().getParentThreadId();

            if (currentText.equals(rb.getString("label.showInfo"))) {
                advancedTabMainController.insertThreadInfoLabelsToUi(parentThreadId, threadNum, infoForCurrentLoopList);
                btn.setText(rb.getString("label.hideInfo"));
            } else {
                advancedTabMainController.removeThreadInfoLabelsFromUi(parentThreadId, threadNum);
                btn.setText(rb.getString("label.showInfo"));
            }
        });

        //keep track of existing threads
        advancedTabMainController.getTaskMap().put(threadNum, searchTask);

        //enable Automerge option if possible
        enableAutomergeOption();

        //log detailed 'Start of search' info message before the first loop only
        if (context.isFirstLoop()) {
            advancedTabMainController.logToUi(context.buildDetailedRunInfoForUi().stream().collect(Collectors.joining(System.lineSeparator())), Color.WHEAT, LogTextTypeEnum.START_OF_SEARCH);
        }

        searchTask.setOnRunning(event -> doOnRunning(context));
        searchTask.setOnSucceeded(event -> doOnSucceeded(context, event));
        searchTask.setOnCancelled(event -> doOnCancelled(context));
        searchTask.setOnFailed(event -> doOnFailed(context));
    }

    private void doOnRunning(TaskPreparationContext context) {
        ThreadComponentDataAccessor accessor = context.getAccessor();
        LOG.info("Thread with id: " + accessor.getThreadNum() + " is now RUNNING.");

        //debug
        // LOG.info(accessor.buildDebugInfo());

        accessor.getStopThreadButton().setDisable(false);
        advancedTabMainController.setBackgroundColorForProgressHBox(accessor.getParentThreadId(), accessor.getThreadNum(), BackgroundColorEnum.BURLY_WOOD);
    }

    private void doOnSucceeded(TaskPreparationContext context, WorkerStateEvent workerStateEvent) {
        ThreadComponentDataAccessor accessor = context.getAccessor();
        Map<String, P2PKHSingleResultData[]> taskResultsMap = context.getTaskResultsMap();
        Map<String, Task<P2PKHSingleResultData[]>> taskMap = context.getTaskMap();

        String saveLocation = context.getSaveLocation();

        P2PKHSingleResultData[] result = (P2PKHSingleResultData[]) workerStateEvent.getSource().getValue();
        String threadNum = context.getAccessor().getThreadNum();
        taskResultsMap.put(threadNum, result);

        if (mainController.isVerboseMode()) {
            LOG.info("Result has been obtained from thread: " + threadNum);
            //debug
            // LOG.info(accessor.buildDebugInfo());
        }
        advancedTabMainController.setBackgroundColorForProgressHBox(accessor.getParentThreadId(), accessor.getThreadNum(), BackgroundColorEnum.LIGHT_GREEN);

        if (!taskMap.containsKey(threadNum)) {
            throw new IllegalStateException("Task map does not contain the specified thread id: " + threadNum);
        }

        accessor.getStopThreadButton().setDisable(true);
        accessor.getStopThreadButton().setOnAction(event -> LOG.info("Thread is not running"));

        accessor.getRemoveButton().setDisable(false);

        //1. firstly, regardless of config, we save normally to the originally provided location
        // searchData = result; -- searchData is not loaded here
        boolean savedNormally = P2PKHSingleResultDataHelper.serializeAndSave(saveLocation, result, "[saved normally after obtaining search results]");
        if (!savedNormally) {
            advancedTabMainController.logToUiBold("Results of thread " + threadNum + " could not be saved!", Color.RED, LogTextTypeEnum.END_OF_SEARCH);
        }

        //2. remove task from the map if automerge is disabled
        if (advancedTabMainController.isAutomergePossible()) {
            Optional<String> automergePath = advancedTabMainController.getAutomergePathFromProgressAndResultsTab();

            if (!automergePath.isPresent()) {
                advancedTabMainController.showErrorMessageInAdvancedSearchSubTab(rb.getString("error.automergePathInvalid"));
            }

            automergePath.ifPresent(mergeLocation -> {
                if (!PathUtils.isAccessibleToReadWrite(mergeLocation)) {
                   advancedTabMainController.insertErrorOrSuccessMessageInAdvancedProgressSubTab(rb.getString("error.automergePathInaccessible"), TextColorEnum.RED);
                   return;
                }

                P2PKHSingleResultData[] mergedResults = mergeAllExistingTaskResults();
                if (mergedResults == null) { //if not all tasks are done (done means any state other than 'NEW')
                    advancedTabMainController.insertErrorOrSuccessMessageInAdvancedProgressSubTab(rb.getString("error.notAllTasksDone"), TextColorEnum.RED);
                    return;
                }

                boolean automergeSucceeded = P2PKHSingleResultDataHelper.serializeAndSave(mergeLocation, mergedResults, "[saved after auto-merge]");
                if (automergeSucceeded) {
                    advancedTabMainController.insertErrorOrSuccessMessageInAdvancedProgressSubTab(MessageFormat.format(rb.getString("info.automergeSuccess"), mergeLocation), TextColorEnum.GREEN);
                    advancedTabMainController.getTaskResultsMap().clear();
                    disableAutomergeOption();
                } else {
                    advancedTabMainController.insertErrorOrSuccessMessageInAdvancedProgressSubTab(rb.getString("Data and path were valid for automerge, but operation did not succeed. Reason unknown"), TextColorEnum.RED);
                }
                Toolkit.getDefaultToolkit().beep();
            });
        }
        taskMap.remove(threadNum);

        //NOTE: remaining loops are decremented inside 'spawnBackgroundSearchThread'
        if (context.getThreadSpawnModel().getRemainingLoops() < 1) {
            processEndOfSearchInfo(context.getThreadSpawnModel().getParentThreadId());
            return;
        }

        ThreadSpawnModel model = context.getThreadSpawnModel();
        if (model.getParentThreadId() == null) {
            model.setParentThreadId(threadNum);
        }

        //3. mutate seed based on requested options
        model.getSeedMutationConfigsAsOptional().ifPresent(seedMutationConfigs -> {
            if (seedMutationConfigs.isEmpty()) {
                return;
            }

            String seedBefore = model.getSeed(); //save old seed
            //apply the new seed
            model.setSeed(buildMutatedSeed(model.getSeed(), model.getDisabledWords(), new SeedMutationConfigDataAccessor(seedMutationConfigs)));

            if (mainController.isVerboseMode()) {
                advancedTabMainController.logToUi("Seed before mutation [threadNum=" + threadNum + "]: " + seedBefore, Color.WHEAT, LogTextTypeEnum.START_OF_SEARCH);
                advancedTabMainController.logToUi("Seed after mutation [threadNum=" + threadNum + "]: " + model.getSeed(), Color.WHEAT, LogTextTypeEnum.START_OF_SEARCH);
            }
        });

        //4. mutate random word prefix if mode is RANDOM_SAME_WORD
        if (SearchModeEnum.isRandomRelatedMode(model.getAdvancedSearchHelper().getSearchMode())) {
            String currentRandomWordPrefix = model.getPrefix();
            if (currentRandomWordPrefix != null && !currentRandomWordPrefix.isEmpty()) {
                model.getPrefixMutationType().ifPresent(prefixMutationType -> {
                    String prefixMutationHexValue = model.getPrefixMutationConfig().getValue();
                    String newPrefix = buildNewPrefix(currentRandomWordPrefix, prefixMutationType, prefixMutationHexValue);
                    model.setPrefix(newPrefix);
                    advancedTabMainController.logToUi(rb.getString("info.newRandomWordPrefixChanged") + newPrefix, Color.WHEAT, LogTextTypeEnum.START_OF_SEARCH);
                });
            }
        }

        //spawn child thread using updated model
        context.getAdvancedSubTabSearchController().spawnBackgroundSearchThread(context.getThreadSpawnModel()).ifPresent(threadId -> {
            advancedTabMainController.logToUi(MessageFormat.format(rb.getString("info.childThreadSpawned"), threadId, threadNum), Color.GREEN, LogTextTypeEnum.GENERAL);
        });
    }

    private void processEndOfSearchInfo(String parentThreadId) {
        AtomicInteger count = new AtomicInteger();
        advancedTabMainController.getTaskDiagnosticsTree().get(parentThreadId).entrySet()
                .stream()
                .forEach((Map.Entry<String, TaskDiagnosticsModel> entry) -> count.addAndGet(entry.getValue().getTotalNumOfResultsFound()));
        String totalCombinedResultsMessage = rb.getString("info.totalNumOfResultsInAllLoops") + count.get();
        LOG.info(totalCombinedResultsMessage);
        advancedTabMainController.logToUiBold(totalCombinedResultsMessage, Color.GREEN, LogTextTypeEnum.END_OF_SEARCH);

        advancedTabMainController.getTaskDiagnosticsTree().get(parentThreadId).clear();
        advancedTabMainController.getTaskDiagnosticsTree().remove(parentThreadId);
    }

    private void doOnCancelled(TaskPreparationContext context) {
        //when thread is cancelled, the returned value is null immediately
        //(P2PKHSingleResultData[]) event.getSource().getValue() == null...

        Task<P2PKHSingleResultData[]> searchTask = context.getSearchTask();
        ThreadComponentDataAccessor accessor = context.getAccessor();
        String threadNum = accessor.getThreadNum();

        //diagnostics:
        String message = searchTask.getMessage();
        String iteration = message.substring(0, message.indexOf(":"));
        String lastPriv = message.substring(message.indexOf(":") + 1);
        String errorMessage = MessageFormat.format(rb.getString("error.threadCancelledAtIteration"), threadNum, iteration, searchTask.getProgress() * 100)
                + System.lineSeparator()
                + rb.getString("error.lastCheckedPkWas") + lastPriv
                + System.lineSeparator()
                + rb.getString("error.resultsNotSavedClickRemove");
        LOG.info(errorMessage);

        accessor.getRemoveButton().setDisable(false);
        accessor.getStopThreadButton().setDisable(true);
        accessor.getStopThreadButton().setOnAction(event -> LOG.info("Thread is not running"));
        accessor.getShowHideInfoButton().setOnAction(event -> showHideThreadError(event, threadNum, context, errorMessage));

        if (context.isVerboseMode()) {
            LOG.info(accessor.buildDebugInfo());
        }

        advancedTabMainController.setBackgroundColorForProgressHBox(accessor.getParentThreadId(), accessor.getThreadNum(), BackgroundColorEnum.GRAY);

        context.getTaskMap().remove(threadNum);
    }

    private void doOnFailed(TaskPreparationContext context) {
        ThreadComponentDataAccessor accessor = context.getAccessor();
        String threadNum = accessor.getThreadNum();

        String message = context.getSearchTask().getMessage();
        String iteration = message.substring(0, message.indexOf(":"));
        String lastPriv = message.substring(message.indexOf(":") + 1);

        String errorMessage = MessageFormat.format(rb.getString("error.threadFailedAtIteration"), threadNum, iteration, context.getSearchTask().getProgress() * 100)
                + System.lineSeparator()
                + rb.getString("error.lastCheckedPkWas") + lastPriv
                + System.lineSeparator()
                + rb.getString("error.resultsNotSavedClickRemove");
        if (context.isVerboseMode()) {
            LOG.info(errorMessage);
        }
        advancedTabMainController.logToUi(errorMessage, Color.RED, LogTextTypeEnum.GENERAL);

        accessor.getRemoveButton().setDisable(false);
        accessor.getStopThreadButton().setDisable(true);
        accessor.getStopThreadButton().setOnAction(event -> LOG.info("Thread is not running"));
        accessor.getShowHideInfoButton().setOnAction(event -> showHideThreadError(event, threadNum, context, errorMessage));

        advancedTabMainController.setBackgroundColorForProgressHBox(accessor.getParentThreadId(), accessor.getThreadNum(), BackgroundColorEnum.INDIAN_RED);

        context.getTaskMap().remove(context.getAccessor().getThreadNum());
    }

    private void showHideThreadError(ActionEvent event, String threadNum, TaskPreparationContext context, String errorMessage) {
        Button btn = (Button)event.getSource();
        String currentText = btn.getText();
        String parentThreadId = context.getThreadSpawnModel().getParentThreadId();

        if (currentText.equals(rb.getString("label.showInfo"))) {
            advancedTabMainController.insertThreadInfoLabelsToUi(parentThreadId, threadNum, Arrays.stream(errorMessage.split(System.lineSeparator())).collect(Collectors.toList()));
            btn.setText(rb.getString("label.hideInfo"));
        } else {
            advancedTabMainController.removeThreadInfoLabelsFromUi(parentThreadId, threadNum);
            btn.setText(rb.getString("label.showInfo"));
        }
    }

    /**
     * Mutates seed according to selected options and returns it
     * @param seed
     * @param disabledWords
     * @param accessor
     * @return
     */
    public synchronized String buildMutatedSeed(String seed, List<Integer> disabledWords, SeedMutationConfigDataAccessor accessor) {
        String result = seed;

        for (SeedMutationTypeEnum mutationType : accessor.getMutationList()) {
            switch (mutationType.getMutationGroup()) {
                case INC_DEC:
                    String incDecValue = accessor.getIncDecValue().orElse(null);
                    if (incDecValue != null) {
                        result = mutateSeedForType(mutationType, result, incDecValue, disabledWords, null);
                    }
                    break;
                case ROTATE_H:
                    String rotateHValue = accessor.getHRotationValue().orElse(null);
                    if (rotateHValue != null) {
                        result = mutateSeedForType(mutationType, result, rotateHValue, disabledWords, null);
                    }
                    break;
                case ROTATE_V:
                    Pair<String, List<Integer>> rotateVPair = accessor.getVRotationPair().orElse(null);
                    if (rotateVPair != null) {
                        result = mutateSeedForType(mutationType, result, rotateVPair.getKey(), disabledWords, rotateVPair.getValue());
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Wrong seed mutation type group detected: " + mutationType.getMutationGroup());
            }
        }

        return result;
    }

    private synchronized String mutateSeedForType(SeedMutationTypeEnum type, String seed, String value, List<Integer> disabledWords, List<Integer> verticalRotationIndexes) {
        switch (type) {
            case INCREMENT_ABSOLUTE:
                return modifier.incrementPrivAbsoluteBy(seed,  Long.parseLong(value, 16), disabledWords);
            case INCREMENT_WORDS:
                return modifier.incrementWordsBy(seed, Long.parseLong(value, 16), disabledWords);
            case DECREMENT_ABSOLUTE:
                return modifier.decrementPrivAbsoluteBy(seed,  Long.parseLong(value, 16), disabledWords);
            case DECREMENT_WORDS:
                return modifier.decrementWordsBy(seed, Long.parseLong(value, 16), disabledWords);
            case ROTATE_NORMAL:
                return modifier.rotateAddressLeftBy(seed, Integer.parseInt(value), false);
            case ROTATE_PREFIXED:
                return modifier.rotateAddressLeftBy(seed, Integer.parseInt(value), true);
            case ROTATE_WORDS:
                return modifier.rotateAllWordsBy(seed, Integer.parseInt(value), disabledWords);
            case ROTATE_VERTICAL:
                int rotationAmount = Integer.parseInt(value);
                if (rotationAmount < 0 || rotationAmount > Config.MAX_V_ROTATIONS) {
                    throw new IllegalArgumentException("Vertical rotation amount must be between 1 and 16 at #mutateSeedForType [received=" + rotationAmount + "]");
                }
                String result = seed;
                for (Integer index : verticalRotationIndexes) {
                    result = modifier.rotateSelectedIndexVerticallyBy(result, disabledWords, index, value);
                }
                return result;
            default:
                throw new IllegalArgumentException("This seed mutation type is not supported at #mutateSeedForType [type=" + type + "]");
        }
    }

    private String buildNewPrefix(String currentRandomWordPrefix, RandomWordPrefixMutationTypeEnum prefixMutationType, String prefixMutationHexValue) {
        Objects.requireNonNull(prefixMutationType);

        switch (prefixMutationType) {
            case INCREMENT:
                return modifier.incrementHexStringBy(currentRandomWordPrefix, Long.parseLong(prefixMutationHexValue, 16));
            case DECREMENT:
                return modifier.decrementHexStringBy(currentRandomWordPrefix, Long.parseLong(prefixMutationHexValue, 16));
            default:
                throw new IllegalArgumentException("This prefix mutation type is not supported [type=" + prefixMutationType + "]");
        }
    }

    private synchronized P2PKHSingleResultData[] mergeAllExistingTaskResults() {
        if (!advancedTabMainController.isAllCurrentTasksDone()) {
            return null;
        }

        Map<String, P2PKHSingleResultData[]> softCopy = advancedTabMainController.getTaskResultsMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1));

        List<String> keys = new ArrayList<>(softCopy.keySet());
        int size = keys.size();
        P2PKHSingleResultData[] parent = null;
        P2PKHSingleResultData[] additional;

        for (int i = 0; i < size; i++) {
            if (i == 0) {
                parent = softCopy.get(keys.get(i));
            } else {
                additional = softCopy.get(keys.get(i));
                parent = P2PKHSingleResultData.merge(parent, additional);
            }
        }

        advancedTabMainController.getTaskMap().clear();
        return parent;
    }

    private void showStopThreadConfirmationDialog(String threadNum, ThreadComponentDataAccessor accessor) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.initModality(Modality.APPLICATION_MODAL);
        alert.getDialogPane().setHeaderText(rb.getString("label.warningThreadWillBeCancelled"));
        alert.getDialogPane().setContentText(rb.getString("label.doYouReallyWantToStop") + System.lineSeparator() + rb.getString("label.resultsWillNotBeSaved") + System.lineSeparator());

        //must add our default stylesheet for styles to work on Alert
        boolean darkModeEnabled = advancedTabMainController.isDarkModeEnabled();
        if (darkModeEnabled) {
            alert.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getClassLoader().getResource("com.bearlycattable.bait.ui.css/styles.css")).toExternalForm());
            alert.getDialogPane().getStyleClass().add("alertDark");
        }

        alert.showAndWait().ifPresent(result -> {
            if (ButtonType.OK == result) {
                if (advancedTabMainController.getTaskMap().get(threadNum) == null) {
                    //This happens if we have dialog open when search finishes and then click 'OK'
                    advancedTabMainController.removeThreadProgressContainerFromProgressAndResultsTab(threadNum);
                    return;
                }
                advancedTabMainController.getTaskMap().get(threadNum).cancel();
                advancedTabMainController.getTaskMap().remove(threadNum);
                accessor.getStopThreadButton().setDisable(true);
                accessor.getRemoveButton().setDisable(false);
            }
        });
    }

    private synchronized void disableAutomergeOption() {
        advancedTabMainController.modifyAutomergeAccessInProgressSubTab(false);
    }

    private synchronized void enableAutomergeOption() {
        advancedTabMainController.modifyAutomergeAccessInProgressSubTab(advancedTabMainController.getTaskMap().size() > 1);
    }
}
