package com.bearlycattable.bait.advanced.interfaceImpls;

import java.awt.Toolkit;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.advanced.interfaces.AdvancedTaskControl;
import com.bearlycattable.bait.advancedCommons.contexts.AdvancedSearchContext;
import com.bearlycattable.bait.advancedCommons.contexts.P2PKHSingleResultData;
import com.bearlycattable.bait.advancedCommons.contexts.TaskPreparationContext;
import com.bearlycattable.bait.advancedCommons.helpers.P2PKHSingleResultDataHelper;
import com.bearlycattable.bait.advancedCommons.interfaces.AdvancedTaskControlAccessProxy;
import com.bearlycattable.bait.advancedCommons.models.ThreadSpawnModel;
import com.bearlycattable.bait.advancedCommons.wrappers.AdvancedSearchTaskWrapper;
import com.bearlycattable.bait.commons.Config;
import com.bearlycattable.bait.commons.contexts.TaskDiagnosticsModel;
import com.bearlycattable.bait.advancedCommons.dataAccessors.SeedMutationConfigDataAccessor;
import com.bearlycattable.bait.advancedCommons.dataAccessors.ThreadComponentDataAccessor;
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

import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.util.Pair;

public class AdvancedTaskControlImpl implements AdvancedTaskControl {

    private static final Logger LOG = Logger.getLogger(AdvancedTaskControlImpl.class.getName());
    private final ResourceBundle rb = ResourceBundle.getBundle(BundleUtils.GLOBAL_BASE_NAME + "AdvancedTaskControlImpl", LocaleUtils.APP_LANGUAGE);

    private final AddressModifier modifier = new AddressModifier(OutputCaseEnum.UPPERCASE);

    private final ConcurrentMap<String, Task<P2PKHSingleResultData[]>> taskMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, P2PKHSingleResultData[]> taskResultsMap = new ConcurrentHashMap<>();
    //Format: parentId, childId, TaskDiagnosticsModel
    private final ConcurrentMap<String, Map<String, TaskDiagnosticsModel>> taskDiagnosticsTree = new ConcurrentHashMap<>();

    private static volatile AdvancedTaskControlAccessProxy advancedTaskControlAccessProxy;
    private static volatile AdvancedTaskControl instance;
    private static volatile boolean initialized = false;

    public static AdvancedTaskControl getInstance() {
        AdvancedTaskControl localRef = instance;
        if (localRef == null) {
            synchronized (AdvancedTaskControlImpl.class) {
                localRef = instance;
                if (instance == null) {
                    instance = localRef = new AdvancedTaskControlImpl();
                }
            }
        }
        return localRef;
    }

    @Override
    public void initialize(Object args) {
        if (!(args instanceof AdvancedTaskControlAccessProxy)) {
            return;
        }

        if (initialized) {
            return;
        }

        advancedTaskControlAccessProxy = (AdvancedTaskControlAccessProxy) args;
        initialized = true;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    private AdvancedTaskControlImpl() {}

    @NonNull
    public Task<P2PKHSingleResultData[]> prepareCurrentTask(TaskPreparationContext context) {
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
            Button btn = (Button) event.getSource();
            String currentText = btn.getText();
            String parentThreadId = context.getThreadSpawnModel().getParentThreadId();

            if (currentText.equals(rb.getString("label.showInfo"))) {
                advancedTaskControlAccessProxy.insertThreadInfoLabelsToUi(parentThreadId, threadNum, infoForCurrentLoopList);
                btn.setText(rb.getString("label.hideInfo"));
            } else {
                advancedTaskControlAccessProxy.removeThreadInfoLabelsFromUi(parentThreadId, threadNum);
                btn.setText(rb.getString("label.showInfo"));
            }
        });

        //keep track of existing threads
        taskMap.put(threadNum, searchTask);

        //enable Automerge option if possible
        enableAutomergeOptionIfEligible();

        //log detailed 'Start of search' info message before the first loop only
        if (context.isFirstLoop()) {
            advancedTaskControlAccessProxy.logToUi("Start-of-search info: " + System.lineSeparator() + context.buildDetailedRunInfoForUi().stream().collect(Collectors.joining(System.lineSeparator())), Color.WHEAT, LogTextTypeEnum.START_OF_SEARCH);
        }

        searchTask.setOnRunning(event -> doOnRunning(context));
        searchTask.setOnSucceeded(event -> doOnSucceeded(context, event));
        searchTask.setOnCancelled(event -> doOnCancelled(context));
        searchTask.setOnFailed(event -> doOnFailed(context));

        logTaskPreparedMessage(context);

        return searchTask;
    }

    private void logTaskPreparedMessage(TaskPreparationContext context) {
        if (!advancedTaskControlAccessProxy.isVerboseMode()) {
            return;
        }

        String msg = "Task has been prepared for parent: " + context.getThreadSpawnModel().getParentThreadId();
        advancedTaskControlAccessProxy.logToUi(msg, Color.GREEN, LogTextTypeEnum.START_OF_SEARCH);
        System.out.println(msg);
    }

    private void doOnRunning(TaskPreparationContext context) {
        ThreadComponentDataAccessor accessor = context.getAccessor();
        LOG.info("Thread with id: " + accessor.getThreadNum() + " is now RUNNING.");

        logDebugInfoForAccessor(accessor, "Thread component info at 'doOnRunning'");

        accessor.getStopThreadButton().setDisable(false);
        advancedTaskControlAccessProxy.setBackgroundColorForProgressHBox(accessor.getParentThreadId(), accessor.getThreadNum(), BackgroundColorEnum.BURLY_WOOD);
    }

    private void logDebugInfoForAccessor(ThreadComponentDataAccessor accessor, String message) {
        if (!advancedTaskControlAccessProxy.isVerboseMode()) {
            LOG.info(accessor.buildDebugInfo(message));
        }
    }

    private void doOnSucceeded(TaskPreparationContext context, WorkerStateEvent workerStateEvent) {
        ThreadComponentDataAccessor accessor = context.getAccessor();
        Map<String, P2PKHSingleResultData[]> taskResultsMap = context.getTaskResultsMap();
        Map<String, Task<P2PKHSingleResultData[]>> taskMap = context.getTaskMap();

        String saveLocation = context.getSaveLocation();

        P2PKHSingleResultData[] result = (P2PKHSingleResultData[]) workerStateEvent.getSource().getValue();
        String threadNum = context.getAccessor().getThreadNum();
        taskResultsMap.put(threadNum, result);

        logDebugInfoForAccessor(accessor, "Thread component info at 'doOnSucceeded' (result obtained from thread " + threadNum + ")");

        advancedTaskControlAccessProxy.setBackgroundColorForProgressHBox(accessor.getParentThreadId(), accessor.getThreadNum(), BackgroundColorEnum.LIGHT_GREEN);

        if (!taskMap.containsKey(threadNum)) {
            throw new IllegalStateException("Task map does not contain the specified thread id: " + threadNum);
        }

        accessor.getStopThreadButton().setDisable(true);
        accessor.getStopThreadButton().setOnAction(event -> LOG.info("Thread is not running"));

        accessor.getRemoveButton().setDisable(false);

        //1. firstly, regardless of config, we save normally to the originally provided location
        // searchData = result; -- searchData is not loaded here
        boolean savedNormally = P2PKHSingleResultDataHelper.serializeAndSave(saveLocation, result);
        if (savedNormally) {
            String messageForUser = "[saved normally after obtaining search results]";
            LOG.info("Results should have been saved to: " + saveLocation + System.lineSeparator() + "Additional message: " + messageForUser);
            advancedTaskControlAccessProxy.logToUiBold("Results should have been saved to: " + saveLocation + System.lineSeparator() + "Additional message: " + messageForUser, Color.GREEN, LogTextTypeEnum.END_OF_SEARCH);
        } else {
            LOG.info("Results of thread " + threadNum + " could not be saved!");
            advancedTaskControlAccessProxy.logToUiBold("Results of thread " + threadNum + " could not be saved!", Color.RED, LogTextTypeEnum.END_OF_SEARCH);
        }


        //2. remove task from the map if automerge is disabled
        if (advancedTaskControlAccessProxy.isAutomergePossible()) {
            Optional<String> automergePath = advancedTaskControlAccessProxy.getAutomergePathFromProgressAndResultsTab();

            if (!automergePath.isPresent()) {
                advancedTaskControlAccessProxy.showErrorMessageInAdvancedSearchSubTab(rb.getString("error.automergePathInvalid"));
            }

            automergePath.ifPresent(mergeLocation -> {
                if (!PathUtils.isAccessibleToReadWrite(mergeLocation)) {
                    advancedTaskControlAccessProxy.insertErrorOrSuccessMessageInAdvancedProgressSubTab(rb.getString("error.automergePathInaccessible"), TextColorEnum.RED);
                   return;
                }

                P2PKHSingleResultData[] mergedResults = mergeAllExistingTaskResults();
                if (mergedResults == null) { //if not all tasks are done (done means any state other than 'NEW')
                    advancedTaskControlAccessProxy.insertErrorOrSuccessMessageInAdvancedProgressSubTab(rb.getString("error.notAllTasksDone"), TextColorEnum.RED);
                    return;
                }

                boolean automergeSucceeded = P2PKHSingleResultDataHelper.serializeAndSave(mergeLocation, mergedResults);
                if (automergeSucceeded) {
                    String messageForUser = "[saved after auto-merge]";
                    LOG.info("Results should have been saved to: " + saveLocation + System.lineSeparator() + "Additional message: " + messageForUser);
                    advancedTaskControlAccessProxy.logToUiBold("Results should have been saved to: " + saveLocation + System.lineSeparator() + "Additional message: " + messageForUser, Color.GREEN, LogTextTypeEnum.END_OF_SEARCH);
                    advancedTaskControlAccessProxy.insertErrorOrSuccessMessageInAdvancedProgressSubTab(MessageFormat.format(rb.getString("info.automergeSuccess"), mergeLocation), TextColorEnum.GREEN);
                    taskResultsMap.clear();
                    disableAutomergeOption();
                } else {
                    LOG.info("Data and path were valid for automerge, but operation did not succeed. Reason unknown");
                    advancedTaskControlAccessProxy.logToUiBold("Data and path were valid for automerge, but operation did not succeed. Reason unknown", Color.RED, LogTextTypeEnum.END_OF_SEARCH);
                    advancedTaskControlAccessProxy.insertErrorOrSuccessMessageInAdvancedProgressSubTab(rb.getString("Data and path were valid for automerge, but operation did not succeed. Reason unknown"), TextColorEnum.RED);
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

            if (advancedTaskControlAccessProxy.isVerboseMode()) {
                advancedTaskControlAccessProxy.logToUi("Seed before mutation [threadNum=" + threadNum + "]: " + seedBefore, Color.WHEAT, LogTextTypeEnum.START_OF_SEARCH);
                advancedTaskControlAccessProxy.logToUi("Seed after mutation [threadNum=" + threadNum + "]: " + model.getSeed(), Color.WHEAT, LogTextTypeEnum.START_OF_SEARCH);
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
                    advancedTaskControlAccessProxy.logToUi(rb.getString("info.newRandomWordPrefixChanged") + newPrefix, Color.WHEAT, LogTextTypeEnum.START_OF_SEARCH);
                });
            }
        }

        //spawn child thread using updated model
        spawnBackgroundSearchThread(context.getThreadSpawnModel()).ifPresent(threadId -> {
            advancedTaskControlAccessProxy.logToUi(MessageFormat.format(rb.getString("info.childThreadSpawned"), threadId, threadNum), Color.GREEN, LogTextTypeEnum.GENERAL);
        });
    }

    private void processEndOfSearchInfo(String parentThreadId) {
        AtomicInteger count = new AtomicInteger();
        taskDiagnosticsTree.get(parentThreadId)
                .forEach((key, value) -> count.addAndGet(value.getTotalNumOfResultsFound()));
        String totalCombinedResultsMessage = rb.getString("info.totalNumOfResultsInAllLoops") + count.get();
        LOG.info(totalCombinedResultsMessage);
        advancedTaskControlAccessProxy.logToUiBold(totalCombinedResultsMessage, Color.GREEN, LogTextTypeEnum.END_OF_SEARCH);

        taskDiagnosticsTree.get(parentThreadId).clear();
        taskDiagnosticsTree.remove(parentThreadId);
    }

    private void doOnCancelled(TaskPreparationContext context) {
        //when thread is cancelled, the returned value is null immediately
        //(P2PKHSingleResultData[]) event.getSource().getValue() == null...

        ThreadComponentDataAccessor accessor = context.getAccessor();
        String threadNum = accessor.getThreadNum();

        String errorMessage = buildErrorMessageOnCancelled(context);
        logErrorMessage(errorMessage);

        accessor.getRemoveButton().setDisable(false);
        accessor.getStopThreadButton().setDisable(true);
        accessor.getStopThreadButton().setOnAction(event -> LOG.info("Thread is not running"));
        accessor.getShowHideInfoButton().setOnAction(event -> showHideThreadError(event, threadNum, context, errorMessage));

        logDebugInfoForAccessor(accessor, "Thread component info at 'doOnCancelled'");

        advancedTaskControlAccessProxy.setBackgroundColorForProgressHBox(accessor.getParentThreadId(), accessor.getThreadNum(), BackgroundColorEnum.GRAY);

        context.getTaskMap().remove(threadNum);
    }

    private void doOnFailed(TaskPreparationContext context) {
        ThreadComponentDataAccessor accessor = context.getAccessor();
        String threadNum = accessor.getThreadNum();

        logDebugInfoForAccessor(accessor, "Thread component info at 'doOnFailed'");

        String errorMessage = buildErrorMessageOnFailed(context);
        logErrorMessage(errorMessage);

        accessor.getRemoveButton().setDisable(false);
        accessor.getStopThreadButton().setDisable(true);
        accessor.getStopThreadButton().setOnAction(event -> LOG.info("Thread is not running"));
        accessor.getShowHideInfoButton().setOnAction(event -> showHideThreadError(event, threadNum, context, errorMessage));

        advancedTaskControlAccessProxy.setBackgroundColorForProgressHBox(accessor.getParentThreadId(), accessor.getThreadNum(), BackgroundColorEnum.INDIAN_RED);

        context.getTaskMap().remove(context.getAccessor().getThreadNum());
    }

    private String buildErrorMessageOnFailed(TaskPreparationContext context) {
        String message = context.getSearchTask().getMessage();
        String iteration = message.substring(0, message.indexOf(":"));
        String lastKey = message.substring(message.indexOf(":") + 1);

        return MessageFormat.format(rb.getString("error.threadFailedAtIteration"), context.getAccessor().getThreadNum(), iteration, context.getSearchTask().getProgress() * 100)
                + System.lineSeparator()
                + rb.getString("error.lastCheckedPkWas") + lastKey
                + System.lineSeparator()
                + rb.getString("error.resultsNotSavedClickRemove");
    }

    private String buildErrorMessageOnCancelled(TaskPreparationContext context) {
        String message = context.getSearchTask().getMessage();
        String iteration = message.substring(0, message.indexOf(":"));
        String lastKey = message.substring(message.indexOf(":") + 1);

        return MessageFormat.format(rb.getString("error.threadCancelledAtIteration"), context.getAccessor().getThreadNum(), iteration, context.getSearchTask().getProgress() * 100)
                + System.lineSeparator()
                + rb.getString("error.lastCheckedPkWas") + lastKey
                + System.lineSeparator()
                + rb.getString("error.resultsNotSavedClickRemove");
    }

    private void logErrorMessage(String errorMessage) {
        LOG.info(errorMessage);
        advancedTaskControlAccessProxy.logToUi(errorMessage, Color.RED, LogTextTypeEnum.END_OF_SEARCH);
    }

    private void showHideThreadError(ActionEvent event, String threadNum, TaskPreparationContext context, String errorMessage) {
        Button btn = (Button)event.getSource();
        String currentText = btn.getText();
        String parentThreadId = context.getThreadSpawnModel().getParentThreadId();

        if (currentText.equals(rb.getString("label.showInfo"))) {
            advancedTaskControlAccessProxy.insertThreadInfoLabelsToUi(parentThreadId, threadNum, Arrays.stream(errorMessage.split(System.lineSeparator())).collect(Collectors.toList()));
            btn.setText(rb.getString("label.hideInfo"));
        } else {
            advancedTaskControlAccessProxy.removeThreadInfoLabelsFromUi(parentThreadId, threadNum);
            btn.setText(rb.getString("label.showInfo"));
        }
    }

    @Override
    @NonNull
    public synchronized String buildMutatedSeed(@NonNull String seed, List<Integer> disabledWords, SeedMutationConfigDataAccessor accessor) {
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

    @Override
    public synchronized Optional<String> spawnBackgroundSearchThread(ThreadSpawnModel threadSpawnModel) {
        //create component in UI that will display the running thread
        Optional<ThreadComponentDataAccessor> accessor = createComponentInUi(threadSpawnModel);
        if (!accessor.isPresent()) {
            return Optional.empty();
        }

        String threadNum = accessor.get().getThreadNum();
        String parentThreadId = threadSpawnModel.getParentThreadId();
        boolean firstLoop = parentThreadId == null; //used later for logging

        if (firstLoop) {
            parentThreadId = elevateToParent(threadNum, threadSpawnModel);
        }

        //TODO: we must track the tree of threads branching from every parent
        taskDiagnosticsTree.get(parentThreadId).put(threadNum, TaskDiagnosticsModel.empty());
        AdvancedSearchContext advancedSearchContext = buildAdvancedSearchContext(threadSpawnModel, parentThreadId, threadNum);

        decrementRemainingLoops(threadSpawnModel);

        //create task and preparation context
        Optional<Task<P2PKHSingleResultData[]>> preparedTask = createNewAdvancedSearchTask(advancedSearchContext, threadSpawnModel)
                .map(taskWrapper -> createTaskPreparationContext(taskWrapper.getTask(), firstLoop, accessor.get(), threadSpawnModel, advancedSearchContext))
                .map(this::prepareCurrentTask);

        if (!preparedTask.isPresent()) {
            return Optional.empty();
        }

        //Could be using executor service (executorService.submit(searchTask)),
        //but, we launch the prepared task on a separate thread manually:
        Thread searchThread = new Thread(preparedTask.get());
        searchThread.setDaemon(true);
        searchThread.start(); //do not call searchThread.run()

        return Optional.of(threadNum);
    }

    private TaskPreparationContext createTaskPreparationContext(Task<P2PKHSingleResultData[]> searchTask, boolean firstLoop, ThreadComponentDataAccessor accessor, ThreadSpawnModel threadSpawnModel, AdvancedSearchContext advancedSearchContext) {
        return TaskPreparationContext.builder()
                .searchTask(searchTask)
                .firstLoop(firstLoop)
                .accessor(accessor)
                .taskMap(taskMap)
                .taskResultsMap(taskResultsMap)
                .saveLocation(threadSpawnModel.getSaveLocation())
                .observableProgressLabelValue(advancedSearchContext.getObservableProgressLabelValue())
                .taskDiagnosticsModel(advancedSearchContext.getTaskDiagnosticsModel())
                //options below are used for loops
                .threadSpawnModel(threadSpawnModel)
                .verboseMode(advancedTaskControlAccessProxy.isVerboseMode())
                .build();
    }

    private Optional<AdvancedSearchTaskWrapper> createNewAdvancedSearchTask(AdvancedSearchContext advancedSearchContext, ThreadSpawnModel threadSpawnModel) {
        AdvancedSearchTaskWrapper taskWrapper = threadSpawnModel.getAdvancedSearchHelper().createNewAdvancedSearchTask(advancedSearchContext);
        String message;

        if (!taskWrapper.hasTask()) {
            message = taskWrapper.getError();
            advancedTaskControlAccessProxy.disableAdvancedSearchBtn(threadSpawnModel.getRemainingLoops() > 0);
            advancedTaskControlAccessProxy.logToUi(message, Color.RED, LogTextTypeEnum.START_OF_SEARCH);
            System.out.println(message);
            return Optional.empty();
        }

        if (advancedTaskControlAccessProxy.isVerboseMode()) {
            message = "Task (unprepared) has been created for parent thread: " + advancedSearchContext.getParentThreadId();
            advancedTaskControlAccessProxy.logToUi(message, Color.GREEN, LogTextTypeEnum.GENERAL);
            System.out.println(message);
        }

        return Optional.of(taskWrapper);
    }

    private void decrementRemainingLoops(ThreadSpawnModel threadSpawnModel) {
        threadSpawnModel.setRemainingLoops(threadSpawnModel.getRemainingLoops() - 1);
        if (!advancedTaskControlAccessProxy.isVerboseMode()) {
            return;
        }

        String msg = "Loops remaining: " + threadSpawnModel.getRemainingLoops();
        advancedTaskControlAccessProxy.logToUi(msg, Color.GREEN, LogTextTypeEnum.START_OF_SEARCH);
        System.out.println(msg);
    }

    private Optional<ThreadComponentDataAccessor> createComponentInUi(ThreadSpawnModel threadSpawnModel) {
        //message displayed on parent's (Accordion's) TitledPane
        String titleMessage = "Search mode: " + threadSpawnModel.getAdvancedSearchHelper().getSearchMode() + ", iterations per loop: " + threadSpawnModel.getAdvancedSearchHelper().getIterations() + ", total loops: " + threadSpawnModel.getRemainingLoops();

        Optional<ThreadComponentDataAccessor> maybeAccessor = advancedTaskControlAccessProxy.addNewThreadProgressContainerToProgressAndResultsTab(threadSpawnModel.getParentThreadId(), titleMessage);

        if (maybeAccessor.isPresent()) {
            return maybeAccessor;
        }

        if (advancedTaskControlAccessProxy.isVerboseMode()) {
            String error = "Received wrong component handles at #createComponentInUi. Cannot continue.";
            advancedTaskControlAccessProxy.logToUi(error, Color.RED, LogTextTypeEnum.START_OF_SEARCH);
            System.out.println(error);
        }
        return Optional.empty();
    }

    private String elevateToParent(String threadNum, ThreadSpawnModel threadSpawnModel) {
        threadSpawnModel.setParentThreadId(threadNum); //set parent id to follow diagnostics properly
        taskDiagnosticsTree.put(threadNum, new HashMap<>());
        return threadNum;
    }

    private AdvancedSearchContext buildAdvancedSearchContext(ThreadSpawnModel threadSpawnModel, String parentThreadId, String currentThreadId) {
        return AdvancedSearchContext.builder()
                .dataArray(threadSpawnModel.getDeepDataCopy())
                .disabledWords(threadSpawnModel.getDisabledWords())
                .seed(threadSpawnModel.getSeed())
                .wordPrefix(threadSpawnModel.getPrefix())
                .observableProgressLabelValue(new SimpleStringProperty())
                .printSpacing(threadSpawnModel.getLogSpacing())
                .progressSpacing(threadSpawnModel.getAdvancedSearchHelper().getIterations() / 1000 > 0 ? (threadSpawnModel.getAdvancedSearchHelper().getIterations() / 1000) : 1) //~0.1%
                .taskDiagnosticsModel(taskDiagnosticsTree.get(parentThreadId).get(currentThreadId))
                .pointThresholdForNotify(threadSpawnModel.getPointThresholdForNotify())
                .logConsumer(advancedTaskControlAccessProxy::logToUi)
                .searchMode(threadSpawnModel.getAdvancedSearchHelper().getSearchMode())
                .parentThreadId(parentThreadId)
                .verbose(advancedTaskControlAccessProxy.isVerboseMode())
                .build();
    }

    @Override
    public boolean isMoreThanOneResultAvailable() {
        return taskResultsMap.keySet().size() > 1;
    }

    @Override
    public boolean isBackgroundThreadWorking(String currentThreadNum) {
        if (!taskMap.containsKey(currentThreadNum)) {
            return false;
        }
        return !taskMap.get(currentThreadNum).isDone();
    }

    @Override
    public boolean isAllCurrentTasksDone() {
        return taskMap.keySet().stream().allMatch(key -> taskMap.get(key).isDone());
    }

    @Override
    public synchronized boolean isTaskCreationAllowed() {
        return taskMap.keySet().size() < (Runtime.getRuntime().availableProcessors() - 1);
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
        if (!isAllCurrentTasksDone()) {
            return null;
        }

        Map<String, P2PKHSingleResultData[]> softCopy = taskResultsMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1));

        List<String> keys = new ArrayList<>(softCopy.keySet());
        int size = keys.size();
        P2PKHSingleResultData[] parent = null;
        P2PKHSingleResultData[] additional;

        for (int i = 0; i < size; i++) {
            if (i == 0) {
                parent = softCopy.get(keys.get(i));
            } else {
                additional = softCopy.get(keys.get(i));
                parent = P2PKHSingleResultDataHelper.merge(parent, additional);
            }
        }

        taskMap.clear();
        return parent;
    }

    private void showStopThreadConfirmationDialog(String threadNum, ThreadComponentDataAccessor accessor) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.initModality(Modality.APPLICATION_MODAL);
        alert.getDialogPane().setHeaderText(rb.getString("label.warningThreadWillBeCancelled"));
        alert.getDialogPane().setContentText(rb.getString("label.doYouReallyWantToStop") + System.lineSeparator() + rb.getString("label.resultsWillNotBeSaved") + System.lineSeparator());

        //must add our default stylesheet for styles to work on Alert
        boolean darkModeEnabled = advancedTaskControlAccessProxy.isDarkModeEnabled();
        if (darkModeEnabled) {
            alert.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getClassLoader().getResource("com.bearlycattable.bait.ui.css/styles.css")).toExternalForm());
            alert.getDialogPane().getStyleClass().add("alertDark");
        }

        alert.showAndWait().ifPresent(result -> {
            if (ButtonType.OK == result) {
                if (taskMap.get(threadNum) == null) {
                    //This happens if we have dialog open when search finishes and then click 'OK'
                    advancedTaskControlAccessProxy.removeThreadProgressContainerFromProgressAndResultsTab(threadNum);
                    return;
                }
                taskMap.get(threadNum).cancel();
                taskMap.remove(threadNum);
                accessor.getStopThreadButton().setDisable(true);
                accessor.getRemoveButton().setDisable(false);
            }
        });
    }

    private synchronized void disableAutomergeOption() {
        advancedTaskControlAccessProxy.modifyAutomergeAccessInProgressSubTab(false);
    }

    //TODO: task tracking someday
    private synchronized void enableAutomergeOptionIfEligible() {
        advancedTaskControlAccessProxy.modifyAutomergeAccessInProgressSubTab(taskMap.size() > 1);
    }
}
