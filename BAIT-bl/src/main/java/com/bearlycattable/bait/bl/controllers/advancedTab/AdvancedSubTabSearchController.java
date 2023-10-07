package com.bearlycattable.bait.bl.controllers.advancedTab;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.advanced.providers.AdvancedSearchHelperProvider;
import com.bearlycattable.bait.advancedCommons.contexts.AdvancedSearchHelperCreationContext;
import com.bearlycattable.bait.advancedCommons.contexts.P2PKHSingleResultData;
import com.bearlycattable.bait.advancedCommons.helpers.DarkModeHelper;
import com.bearlycattable.bait.advancedCommons.helpers.HeatVisualizerComponentHelper;
import com.bearlycattable.bait.advancedCommons.helpers.P2PKHSingleResultDataHelper;
import com.bearlycattable.bait.advancedCommons.interfaces.AdvancedSearchHelper;
import com.bearlycattable.bait.advancedCommons.models.ThreadSpawnModel;
import com.bearlycattable.bait.advancedCommons.serialization.SerializedSearchResultsReader;
import com.bearlycattable.bait.advancedCommons.validators.OptionalConfigValidationResponseType;
import com.bearlycattable.bait.advancedCommons.validators.VRotationInputValidator;
import com.bearlycattable.bait.advancedCommons.validators.ValidatorResponse;
import com.bearlycattable.bait.bl.controllers.advancedTab.proxyInterfaces.AdvancedSearchAccessProxy;
import com.bearlycattable.bait.bl.helpers.HeatVisualizerFormatterFactory;
import com.bearlycattable.bait.bl.wrappers.InitialConditionsValidatorWrapper;
import com.bearlycattable.bait.bl.wrappers.NotificationConfigsWrapper;
import com.bearlycattable.bait.bl.wrappers.PrefixedModeConfigsWrapper;
import com.bearlycattable.bait.commons.Config;
import com.bearlycattable.bait.commons.CssConstants;
import com.bearlycattable.bait.commons.HeatVisualizerConstants;
import com.bearlycattable.bait.commons.enums.JsonResultScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.LogTextTypeEnum;
import com.bearlycattable.bait.commons.enums.RandomWordPrefixMutationTypeEnum;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.enums.SeedMutationTypeEnum;
import com.bearlycattable.bait.commons.enums.TextColorEnum;
import com.bearlycattable.bait.commons.helpers.HeatVisualizerModalHelper;
import com.bearlycattable.bait.commons.validators.SearchHelperIterationsValidator;
import com.bearlycattable.bait.utility.BaitUtils;
import com.bearlycattable.bait.utility.BundleUtils;
import com.bearlycattable.bait.utility.LocaleUtils;
import com.bearlycattable.bait.utility.PathUtils;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.util.Pair;
import lombok.Getter;

@Getter
public class AdvancedSubTabSearchController {

    private static final Logger LOG = Logger.getLogger(AdvancedSubTabSearchController.class.getName());
    private final ResourceBundle rb = ResourceBundle.getBundle(BundleUtils.GLOBAL_BASE_NAME + "AdvancedSubTabSearch", LocaleUtils.APP_LANGUAGE);
    private final AdvancedSearchHelperProvider advancedSearchHelperProvider = ServiceLoader.load(AdvancedSearchHelperProvider.class).findFirst().orElse(null);

    //format: Pair<PathToTemplate, TemplateData>
    private Pair<String, P2PKHSingleResultData[]> loadedSearchTemplateData;
    private static final List<Integer> disabledWords = new ArrayList<>();

    @FXML
    private TextField advancedSearchTextFieldLoadSearchTemplateFromFile;
    @FXML
    private Button advancedBtnBrowseImportSearchResultsFile;
    @FXML
    private Button advancedBtnLoadExisting;
    @FXML
    private Button advancedBtnShowExistingResults;
    @FXML
    private Button advancedSearchBtnBrowseWhereToSave;
    @FXML
    private TextField advancedSearchTextFieldSaveSearchToFile;

    //seed component, seed import/export
    @FXML
    private HBox advancedSearchSeedComponentParent;
    @FXML
    private TextField advancedSearchTextFieldContinueFromSeed;
    @FXML
    private Label advancedSearchLabelLengthSeed;
    @FXML
    private Button advancedSearchBtnImportSeedFromConstruction;
    @FXML
    private Button advancedBtnExportSeedToConstruction;

    //general search parameters
    @FXML
    private ChoiceBox<String> advancedSearchChoiceBoxSearchMode;
    @FXML
    private TextField advancedSearchTextFieldIterations;
    @FXML
    private ComboBox<JsonResultScaleFactorEnum> advancedSearchComboBoxScaleFactor;
    @FXML
    private CheckBox advancedSearchCbxEnableExactMatchCheck;
    @FXML
    private HBox advancedSearchHBoxDisabledWordsParent;
    //optional search parameters (will be injected - full example here: advancedSearchHBoxFullExampleForModeSpecificOptions).
    @FXML
    private HBox advancedSearchHBoxModeSpecificOptionsParent; //mode-specific configs will be injected into this component as HBoxes

    //loops config
    @FXML
    private TextField advancedSearchTextFieldNumberOfLoops;

    //seed inc/dec management
    @FXML
    private CheckBox advancedSearchCbxLoopOptionEnableIncDec;
    @FXML
    private HBox advancedOptionalMenuIncDecContainer; //enable/disable this whole component for inc/dec management
    @FXML
    private RadioButton advancedSearchRadioOptionalIncAbsolute;
    @FXML
    private RadioButton advancedSearchRadioOptionalDecAbsolute;
    @FXML
    private RadioButton advancedSearchRadioOptionalIncWords;
    @FXML
    private RadioButton advancedSearchRadioOptionalDecWords;
    @FXML
    private TextField advancedSearchTextFieldIncDecBy;

    //seed horizontal rotation management
    @FXML
    private CheckBox advancedSearchCbxLoopOptionEnableRotation;
    @FXML
    private HBox advancedOptionalMenuHRotationContainer; //enable/disable this whole component for horizontal rotation management
    @FXML
    private RadioButton advancedSearchRadioOptionalRotateNormal;
    @FXML
    private RadioButton advancedSearchRadioOptionalRotateWords;
    @FXML
    private RadioButton advancedSearchRadioOptionalRotatePrefixed;
    @FXML
    private TextField advancedTextFieldRotateHorizontallyBy;

    //seed vertical rotation management
    @FXML
    private CheckBox advancedSearchCbxLoopOptionEnableVertical;
    @FXML
    private HBox advancedOptionalMenuVRotationContainer;
    @FXML
    private TextField advancedTextFieldRotateVerticallyBy;
    @FXML
    private TextField advancedTextFieldRotateVerticallyAtIndexes;

    //notification options
    @FXML
    @Getter
    private CheckBox advancedSearchCbxEnableSoundNotifications;
    @FXML
    @Getter
    private HBox advancedSearchHBoxNotificationToleranceParent;

    @FXML
    private TextField advancedSearchTextFieldLogKeyEveryXIterations;
    @FXML
    private CheckBox advancedSearchCbxByteComparisonEnable;
    @FXML
    private Label advancedSearchErrorLabel;
    @FXML
    private Button advancedBtnSearch;

    //additional components only used when search mode is RANDOM_SAME_WORD or RANDOM_PREFIXED_WORD
    private TextField advancedSearchTextFieldRandomWordPrefix;
    private CheckBox advancedSearchCbxEnableRandomWordPrefixMutation;
    private RadioButton advancedSearchRadioRandomWordPrefixInc;
    private RadioButton advancedSearchRadioRandomWordPrefixDec;
    private TextField advancedSearchTextFieldRandomWordPrefixIncDecBy;
    private TextField advancedSearchTextFieldNotificationPointTolerance;

    private AdvancedSearchAccessProxy advancedSearchAccessProxy;

    @FXML
    void initialize() {
        System.out.println("CREATING (child, advanced): AdvancedSubTabSearchController......");
    }

    void initDevDefaults() {
        //TODO: add dev defaults
        advancedSearchTextFieldLoadSearchTemplateFromFile.setText("D:\\Projects\\TestFiles\\testDelete_first5.json");
        advancedSearchTextFieldSaveSearchToFile.setText("D:\\Projects\\TestFiles\\zzzz.json");
        advancedSearchChoiceBoxSearchMode.getSelectionModel().select("Incremental (absolute)");
        advancedSearchTextFieldIterations.setText("5");
        advancedSearchTextFieldLogKeyEveryXIterations.setText("1");
        advancedSearchTextFieldContinueFromSeed.setText("0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF");
        // System.out.println("Dev defaults are empty!");
    }

    public void setAdvancedSearchAccessProxy(AdvancedSearchAccessProxy proxy) {
        this.advancedSearchAccessProxy = Objects.requireNonNull(proxy);
    }

    @FXML
    private void doBrowseExistingResultsFilePath() {
        Optional<String> result = HeatVisualizerModalHelper.selectJsonResourceForOpen(rb.getString("label.modalSelectResourceFile"), advancedSearchTextFieldLoadSearchTemplateFromFile);
        if (result.isPresent()) {
            String absPath = result.get();
            advancedSearchTextFieldLoadSearchTemplateFromFile.setText(absPath);
            if (absPath.endsWith(".json")) {
                removeRedBorder(advancedSearchTextFieldLoadSearchTemplateFromFile);
            }
            return;
        }

        String existingPath = advancedSearchTextFieldLoadSearchTemplateFromFile.getText();
        if (existingPath.isEmpty() || !existingPath.endsWith(".json")) {
            addRedBorder(advancedSearchTextFieldLoadSearchTemplateFromFile);
            return;
        }
        removeRedBorder(advancedSearchTextFieldLoadSearchTemplateFromFile);
    }

    @FXML
    private void doLoadExistingResultsFile() {
        String path = advancedSearchTextFieldLoadSearchTemplateFromFile.getText();

        if (path.isEmpty() || !path.endsWith(".json")) {
            showErrorMessage(rb.getString("error.pathEmptyOrNotJson"));
            addRedBorder(advancedSearchTextFieldLoadSearchTemplateFromFile);
            return;
        }

        P2PKHSingleResultData[] loadedTemplate = SerializedSearchResultsReader.deserializeExistingSearchResults(path);

        if (loadedTemplate == null || loadedTemplate.length == 0) {
            showErrorMessage(rb.getString("error.templateInvalidOrEmpty"));
            addRedBorder(advancedSearchTextFieldLoadSearchTemplateFromFile);
            return;
        }
        removeRedBorder(advancedSearchTextFieldLoadSearchTemplateFromFile);
        setLoadedSearchTemplateData(path, loadedTemplate);

        advancedSearchTextFieldSaveSearchToFile.setDisable(false);
        advancedSearchBtnBrowseWhereToSave.setDisable(false);

        //ONLY parent controller should hold all search data
        // parentController.setLoadedExistingResults(P2PKHSingleResultDataHelper.deepCopy(searchData));

        //this is needed
        advancedBtnShowExistingResults.setDisable(false);
        removeRedBorder(advancedSearchTextFieldLoadSearchTemplateFromFile);
        removeRedBorder(advancedBtnLoadExisting);
        showInfoMessage(rb.getString("info.resultsLoadingSuccess"), TextColorEnum.GREEN);
        LOG.info(rb.getString("info.resultsLoadingSuccess"));
        // parentController.modifyAccessToFilterBtn(true);
    }

    private void setLoadedSearchTemplateData(@NonNull String path, @NonNull P2PKHSingleResultData[] loadedTemplate) {
        this.loadedSearchTemplateData = new Pair<>(path, loadedTemplate);
    }

    @FXML
    private void doShowExistingResultsFromFile() {
        // advancedTextFieldLoadSearchFromFile - textfield (no-op unless we have a potentially valid imported file)
        if (loadedSearchTemplateData == null || loadedSearchTemplateData.getValue() == null) {
            advancedBtnShowExistingResults.setDisable(true);
            showErrorMessage("No result template is loaded. Cannot show the results");
            return;
        }

        advancedSearchAccessProxy.loadAdvancedSearchResultsToUi(loadedSearchTemplateData.getKey(), P2PKHSingleResultDataHelper.toDataMap(loadedSearchTemplateData.getValue()));
        showInfoMessage("Search results have been loaded (only first 20 are displayed). Use filter to browse all possible", TextColorEnum.GREEN);
        advancedSearchAccessProxy.switchToChildTabX(3);
    }

    @FXML
    private void doSelectSearchSaveTargetPath() {
        HeatVisualizerModalHelper.selectJsonResourceForSave(rb.getString("label.modalSelectSaveDestination"), advancedSearchTextFieldSaveSearchToFile).ifPresent(absPath -> {
            advancedSearchTextFieldSaveSearchToFile.setText(absPath);
        });
    }

    @FXML
    private void doImportPriv() {
        String key = advancedSearchAccessProxy.importDataFromCurrentInputFieldInMainTab(this);

        if (!HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(key).matches()) {
            addRedBorder(advancedSearchTextFieldContinueFromSeed);
            showErrorMessage(rb.getString("error.importFailedInvalidKey"));
            return;
        }

        removeRedBorder(advancedSearchTextFieldContinueFromSeed);
        advancedSearchTextFieldContinueFromSeed.setText(key);
    }

    @FXML
    private void doExportPriv() {
        String key = advancedSearchTextFieldContinueFromSeed.getText();

        if (!HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(key).matches()) {
            addRedBorder(advancedSearchTextFieldContinueFromSeed);
            showErrorMessage(rb.getString("error.exportFailedInvalidKey"));
            return;
        }

        removeRedBorder(advancedSearchTextFieldContinueFromSeed);
        advancedSearchAccessProxy.exportDataToCurrentInputFieldInMainTab(key, this);
        showInfoMessage(rb.getString("info.exportSuccess"), TextColorEnum.GREEN);
    }

    SearchModeEnum getAdvancedSearchModeFromUi() {
        return SearchModeEnum.getByLabel(advancedSearchChoiceBoxSearchMode.getSelectionModel().getSelectedItem()).orElse(null);
    }

    @FXML
    private void doAdvancedSearch() {
        advancedBtnSearch.setDisable(true);

        if (!isInitialConditionsValid()) {
            advancedBtnSearch.setDisable(false);
            return;
        }

        //REMINDER: num of iterations are included while constructing the SimpleSearchHelper!
        AdvancedSearchHelper advancedSearchHelper = constructSearchHelperFromUiData().orElse(null);

        if (advancedSearchHelper == null) {
            appendToErrorMessage(rb.getString("error.searchHelperConstructionError"));
            advancedBtnSearch.setDisable(false);
            return;
        } else {
            enableExactMatchCheckIfEligible(advancedSearchHelper, Config.EXACT_MATCH_ADDRESSES_LIST_PATH);
            removeMessage();
        }

        int logSpacing = Integer.parseInt(advancedSearchTextFieldLogKeyEveryXIterations.getText());

        //must validate seed for all modes (and generate any for random-related ones)
        String seed = buildSeed(advancedSearchHelper);
        if (seed.isEmpty()) {
            addErrorMessageAndRedBorder(rb.getString("error.invalidSeed"), advancedSearchTextFieldContinueFromSeed);
            advancedBtnSearch.setDisable(false);
            return;
        }
        removeRedBorder(advancedSearchTextFieldContinueFromSeed);

        //validate save location
        String saveLocation = advancedSearchTextFieldSaveSearchToFile.getText();
        if (!saveLocation.endsWith(".json") || !PathUtils.isAccessibleToWrite(saveLocation)) {
            addErrorMessageAndRedBorder(rb.getString("error.invalidSaveLocation"), advancedSearchTextFieldSaveSearchToFile);
        }
        removeRedBorder(advancedSearchTextFieldSaveSearchToFile);

        int numberOfLoops = getNumberOfLoopsFromUi(getNumberOfLoopsFromUi(Config.MAX_LOOPS));

        if (numberOfLoops == -1) {
            advancedBtnSearch.setDisable(false);
            return;
        }

        removeMessage();
        removeRedBorder(advancedSearchTextFieldNumberOfLoops);

        List<Integer> disabledWords = readDisabledWordsFromUi();
        Map<SeedMutationTypeEnum, Object> seedMutationConfigs = null;

        boolean randomRelatedMode = SearchModeEnum.isRandomRelatedMode(advancedSearchHelper.getSearchMode());

        if (!randomRelatedMode) {
            seedMutationConfigs = buildSeedMutationConfigs().orElse(null);
        }

        Pair<RandomWordPrefixMutationTypeEnum, String> modeSpecificConfigs = null;
        String randomWordPrefix = null;

        if (SearchModeEnum.isPrefixSupported(advancedSearchHelper.getSearchMode())) {
            PrefixedModeConfigsWrapper prefixedModeConfigsWrapper = gatherModeSpecificConfigs();
            if (!prefixedModeConfigsWrapper.hasValidConfig()) {
                showErrorMessage(prefixedModeConfigsWrapper.getError());
                advancedBtnSearch.setDisable(false);
                return;
            }

            randomWordPrefix = prefixedModeConfigsWrapper.getPrefix();
            modeSpecificConfigs = prefixedModeConfigsWrapper.getModeSpecificConfigs();
        }
        removeMessage();

        //handle sound notification tolerance selection
        int soundNotificationTolerance = 0;
        if (advancedSearchCbxEnableSoundNotifications.isSelected()) {
            NotificationConfigsWrapper notificationConfigsWrapper = gatherNotificationConfigs();
            if (!notificationConfigsWrapper.hasValidConfig()) {
                showErrorMessage(notificationConfigsWrapper.getError());
                advancedBtnSearch.setDisable(false);
                return;
            }

            soundNotificationTolerance = notificationConfigsWrapper.getNotificationTolerance();
        }

        //need to make a proper copy for each thread
        P2PKHSingleResultData[] deepCopy = P2PKHSingleResultDataHelper.deepCopy(loadedSearchTemplateData.getValue());

        //TODO: only for testing
        System.out.println("Creating ThreadSpawnModel");
        ThreadSpawnModel threadSpawnModel = ThreadSpawnModel.builder()
                .advancedSearchHelper(advancedSearchHelper)
                .byteComparisonEnabled(advancedSearchCbxByteComparisonEnable.isSelected())
                .seed(seed)
                .disabledWords(disabledWords)
                .saveLocation(saveLocation)
                .logSpacing(logSpacing)
                .deepDataCopy(deepCopy)
                .totalLoopsRequested(numberOfLoops)
                .remainingLoops(numberOfLoops)
                .pointThresholdForNotify(soundNotificationTolerance)
                //seed mutation (optional)
                .seedMutationConfigs(seedMutationConfigs)
                //only for random-related modes
                .prefix(randomRelatedMode ? randomWordPrefix : null)
                .prefixMutationConfig(randomRelatedMode ? modeSpecificConfigs : null)
                .build();

        //TODO: splash screen with confirmation loading progress (because loading exact check items might take some time)

        //confirm user choice
        if (!confirmUserChoiceForNewSearchThread(threadSpawnModel.makeLabelListForUserNotification((t,u,v) -> advancedSearchAccessProxy.buildMutatedSeed(t, u, v)))) {
            if (advancedSearchAccessProxy.isVerboseMode()) {
                String msg = "User did not accept the search parameters. Search will not proceed";
                System.out.println(msg);
                advancedSearchAccessProxy.logToUi(msg, Color.DARKORANGE, LogTextTypeEnum.GENERAL);
            }
            advancedBtnSearch.setDisable(false);
            return;
        }

        //init caches if possible
        // Object o = ModifierType.STRING; ewfewf
        // initializeTemplateCaches(deepCopy, Objects.requireNonNull(advancedSearchHelper).getScaleFactor(), ModifierType.STRING);

        //TODO: only for testing
        System.out.println("Will now be spawning background search thread");

        advancedSearchAccessProxy.spawnBackgroundSearchThread(threadSpawnModel, this)
                .ifPresent(threadNum -> {
                    if (advancedSearchAccessProxy.isVerboseMode()) {
                        String message = "New parent search thread " + threadNum + " has been spawned (manually by the user)";
                        LOG.info(message);
                        advancedSearchAccessProxy.logToUi(message, Color.GREEN, LogTextTypeEnum.GENERAL);
                    }
                    advancedSearchAccessProxy.switchToChildTabX(2); //switch to 'Progress' tab
                });

        advancedBtnSearch.setDisable(false);
    }

    private boolean isInitialConditionsValid() {
        InitialConditionsValidatorWrapper initialConditionsValidatorWrapper = validateInitialConditions();
        return initialConditionsValidatorWrapper.isValid();
    }

    private InitialConditionsValidatorWrapper validateInitialConditions() {
        InitialConditionsValidatorWrapper.InitialConditionsValidatorWrapperBuilder result = InitialConditionsValidatorWrapper.builder();
        String error;
        if (!advancedSearchAccessProxy.isTaskCreationAllowed(this)) {
            error = "Enough tasks are already running. Cannot spawn more threads";
            showErrorMessage(error);
            //TODO: also check if any of the tasks are finished already - remove them from UI?
            //maybe it is better to return threadId rather than boolean from 'spawnBackgroundSearchThread'?
            advancedBtnSearch.setDisable(false);
            return result
                    .error(error)
                    .build();
        }

        //must check if all 5 steps have been completed beforehand
        if (loadedSearchTemplateData == null || loadedSearchTemplateData.getValue() == null) {
            error = rb.getString("error.resultTemplateMustBeLoaded");
            showErrorMessage(error);
            addRedBorder(advancedSearchTextFieldLoadSearchTemplateFromFile);
            addRedBorder(advancedBtnLoadExisting);
            advancedBtnSearch.setDisable(false);
            return result
                    .error(error)
                    .build();
        }

        removeRedBorder(advancedSearchTextFieldLoadSearchTemplateFromFile);
        removeRedBorder(advancedBtnLoadExisting);
        removeMessage();

        return result.build();
    }

    // private void initializeTemplateCaches(P2PKHSingleResultData[] deepCopy, ScaleFactorEnum scaleFactor, ModifierType cacheType) {
    //     if (deepCopy.length <= Config.MAX_CACHEABLE_ADDRESSES_IN_TEMPLATE) {
    //         P2PKHSingleResultDataHelper.initializeCaches(deepCopy, ScaleFactorEnum.toJsonScaleFactorEnum(scaleFactor), cacheType);
    //         if (advancedSearchAccessProxy.isVerboseMode()) {
    //             String cachedSuccessfully = rb.getString("info.allTemplatesCached");
    //             LOG.info(cachedSuccessfully);
    //             advancedSearchAccessProxy.logToUi(cachedSuccessfully, Color.GREEN, LogTextTypeEnum.START_OF_SEARCH);
    //         }
    //         return;
    //     }
    //
    //     if (advancedSearchAccessProxy.isVerboseMode()) {
    //         String templatesCannotBeCached = rb.getString("info.proceedingWithUncached");
    //         LOG.info(templatesCannotBeCached);
    //         advancedSearchAccessProxy.logToUi(templatesCannotBeCached, Color.DARKORANGE, LogTextTypeEnum.START_OF_SEARCH);
    //     }
    // }

    @NonNull
    private NotificationConfigsWrapper gatherNotificationConfigs() {
        NotificationConfigsWrapper.NotificationConfigsWrapperBuilder result = NotificationConfigsWrapper.builder();

        Optional<Integer> notificationTolerance = getSoundNotificationTolerance();
        if (!notificationTolerance.isPresent()) {
            String error = rb.getString("error.notificationToleranceInvalidInput");
            addErrorMessageAndRedBorder(error, advancedSearchTextFieldNotificationPointTolerance);
            advancedBtnSearch.setDisable(false);

            return result
                    .error(error)
                    .build();
        }

        removeMessage();
        removeRedBorder(advancedSearchTextFieldNotificationPointTolerance);

        if (notificationTolerance.get() > getAdvancedSearchScaleFactorFromUi().getMaxPoints().intValue()) {
            String error = rb.getString("error.notificationToleranceExceedsMax");
            addErrorMessageAndRedBorder(error, advancedSearchTextFieldNotificationPointTolerance);
            advancedBtnSearch.setDisable(false);
            return result
                    .error(error)
                    .build();
        }

        removeMessage();
        removeRedBorder(advancedSearchTextFieldNotificationPointTolerance);

        return result
                .notificationTolerance(notificationTolerance.get())
                .build();
    }

    @NonNull
    private PrefixedModeConfigsWrapper gatherModeSpecificConfigs() {
        PrefixedModeConfigsWrapper.PrefixedModeConfigsWrapperBuilder result = PrefixedModeConfigsWrapper.builder();

        //dev
        if (advancedSearchTextFieldRandomWordPrefix == null) {
            result.error("Text field was not available for random word prefix!");
            return result.build();
        }

        String randomWordPrefix = advancedSearchTextFieldRandomWordPrefix.getText();

        if (randomWordPrefix.isEmpty()) {
            return result.build();
        }

        if (!HeatVisualizerConstants.PATTERN_HEX_01_TO_08.matcher(randomWordPrefix).matches()) {
            String error = rb.getString("error.randomWordPrefixInvalid");
            advancedBtnSearch.setDisable(false);
            addErrorMessageAndRedBorder(error, advancedSearchTextFieldRandomWordPrefix);

            return result
                    .error(error)
                    .build();
        }

        if (advancedSearchAccessProxy.isVerboseMode()) {
            advancedSearchAccessProxy.logToUi("Setting random word prefix to: " + randomWordPrefix, Color.WHEAT, LogTextTypeEnum.GENERAL);
        }

        RandomWordPrefixMutationTypeEnum prefixMutationType = retrieveIncOrDecForRandomWordPrefix().orElse(null);
        Pair<RandomWordPrefixMutationTypeEnum, String> modeSpecificConfigs = new Pair<>(prefixMutationType, advancedSearchTextFieldRandomWordPrefixIncDecBy.getText());

        return result
                .prefix(randomWordPrefix)
                .modeSpecificConfigs(modeSpecificConfigs)
                .build();
    }

    @NonNull
    private String buildSeed(AdvancedSearchHelper advancedSearchHelper) {
        SearchModeEnum mode = advancedSearchHelper.getSearchMode();
        if (mode == null) {
            throw new IllegalStateException("Search helper didn't declare its search mode");
        }
        if (SearchModeEnum.isRandomRelatedMode(advancedSearchHelper.getSearchMode())) {
            return Stream.generate(() -> "0").limit(64).collect(Collectors.joining());
        }
        String seed = advancedSearchTextFieldContinueFromSeed.getText();
        if (!HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(seed).matches()) {
            return "";
        }
        return seed;
    }

    private Optional<Map<SeedMutationTypeEnum, Object>> buildSeedMutationConfigs() {
        Map<SeedMutationTypeEnum, Object> optionalSeedMutationConfigs = new LinkedHashMap<>();

        ValidatorResponse responseIncDec = validateAndGetIncDecOptions();
        if (OptionalConfigValidationResponseType.ABORT == responseIncDec.getResponseType()) {
            addErrorMessageAndRedBorder(responseIncDec.getErrorMessage(), advancedSearchTextFieldIncDecBy);
            advancedBtnSearch.setDisable(false);
            return Optional.empty();
        } else if (OptionalConfigValidationResponseType.SILENT_ABORT == responseIncDec.getResponseType()) {
            advancedBtnSearch.setDisable(false);
            return Optional.empty();
        }
        removeMessage();
        removeRedBorder(advancedSearchTextFieldIncDecBy);
        if (OptionalConfigValidationResponseType.CONTINUE_EMPTY != responseIncDec.getResponseType()) {
            optionalSeedMutationConfigs.put(responseIncDec.getSeedMutationType(), responseIncDec.getResponseData());
        }

        ValidatorResponse responseHRot = validateAndGetHRotationOptions();
        if (OptionalConfigValidationResponseType.ABORT == responseHRot.getResponseType()) {
            addErrorMessageAndRedBorder(responseHRot.getErrorMessage(), advancedTextFieldRotateHorizontallyBy);
            advancedBtnSearch.setDisable(false);
            return Optional.empty();
        } else if (OptionalConfigValidationResponseType.SILENT_ABORT == responseHRot.getResponseType()) {
            advancedBtnSearch.setDisable(false);
            return Optional.empty();
        }
        removeMessage();
        removeRedBorder(advancedTextFieldRotateHorizontallyBy);

        if (OptionalConfigValidationResponseType.CONTINUE_EMPTY != responseHRot.getResponseType()) {
            optionalSeedMutationConfigs.put(responseHRot.getSeedMutationType(), responseHRot.getResponseData());
        }

        ValidatorResponse responseVRot = validateAndGetVRotationOptions();
        if (OptionalConfigValidationResponseType.ABORT == responseVRot.getResponseType()) {
            addErrorMessageAndRedBorder(responseVRot.getErrorMessage(), responseVRot.getErrorTargetTextField());
            advancedBtnSearch.setDisable(false);
            return Optional.empty();
        } else if (OptionalConfigValidationResponseType.SILENT_ABORT == responseVRot.getResponseType()) {
            advancedBtnSearch.setDisable(false);
            return Optional.empty();
        }
        removeMessage();
        removeRedBorder(advancedTextFieldRotateVerticallyBy);
        removeRedBorder(advancedTextFieldRotateVerticallyAtIndexes);

        if (OptionalConfigValidationResponseType.CONTINUE_EMPTY != responseVRot.getResponseType()) {
            optionalSeedMutationConfigs.put(responseVRot.getSeedMutationType(), responseVRot.getResponseData());
        }

        return Optional.of(optionalSeedMutationConfigs);
    }

    private Optional<Integer> getSoundNotificationTolerance() {
        if (advancedSearchTextFieldNotificationPointTolerance == null) {
            return Optional.empty();
        }

        String value = advancedSearchTextFieldNotificationPointTolerance.getText();

        if (!HeatVisualizerConstants.DIGITS_ONLY_MAX4.matcher(value).matches()) {
            return Optional.empty();
        }

        return Optional.of(Integer.parseInt(value));
    }

    private ValidatorResponse validateAndGetIncDecOptions() {
        ValidatorResponse incDecResponse = ValidatorResponse.builder().responseType(OptionalConfigValidationResponseType.CONTINUE_EMPTY).build();
        if (!advancedSearchCbxLoopOptionEnableIncDec.isDisabled() && advancedSearchCbxLoopOptionEnableIncDec.isSelected()) {
            retrieveIncOrDecTypeForOptionalConfigs().ifPresent(selectedIncDecMutationType -> {
                String incDecBy = advancedSearchTextFieldIncDecBy.getText();

                if (!HeatVisualizerConstants.PATTERN_HEX_01_TO_08.matcher(incDecBy).matches()) {
                    incDecResponse.setResponseType(OptionalConfigValidationResponseType.ABORT);
                    incDecResponse.setErrorMessage(rb.getString("error.incDecMustBe8HexOrLess"));
                    return;
                }
                incDecResponse.setResponseType(OptionalConfigValidationResponseType.CONTINUE);
                incDecResponse.setSeedMutationType(selectedIncDecMutationType);
                incDecResponse.setResponseData(incDecBy);

                // optionalSeedMutationConfigs.put(selectedIncDecMutationType, incDecBy);
            });
        }
        return incDecResponse;
    }

    private ValidatorResponse validateAndGetHRotationOptions() {
        ValidatorResponse hRotResponse = ValidatorResponse.builder().responseType(OptionalConfigValidationResponseType.CONTINUE_EMPTY).build();
        if (!advancedSearchCbxLoopOptionEnableRotation.isDisabled() && advancedSearchCbxLoopOptionEnableRotation.isSelected()) {
            retrieveHorizontalMutationTypeForOptionalConfigs().ifPresent(selectedHorizontalRotationType -> {

                String rotateHBy = advancedTextFieldRotateHorizontallyBy.getText();

                if (!HeatVisualizerConstants.DIGITS_ONLY_MAX3.matcher(rotateHBy).matches()) {
                    hRotResponse.setResponseType(OptionalConfigValidationResponseType.ABORT);
                    hRotResponse.setErrorMessage(rb.getString("error.rotHMustNotExceed3"));
                    return;
                }

                hRotResponse.setResponseType(OptionalConfigValidationResponseType.CONTINUE);
                hRotResponse.setSeedMutationType(selectedHorizontalRotationType);
                hRotResponse.setResponseData(rotateHBy);
            });
        }
        return hRotResponse;
    }

    private ValidatorResponse validateAndGetVRotationOptions() {
        ValidatorResponse vRotResponse = ValidatorResponse.builder().responseType(OptionalConfigValidationResponseType.CONTINUE_EMPTY).build();
        if (!advancedSearchCbxLoopOptionEnableVertical.isDisabled() && advancedSearchCbxLoopOptionEnableVertical.isSelected()) {
            String rotateVBy = advancedTextFieldRotateVerticallyBy.getText();
            ValidatorResponse responseVRotBy = VRotationInputValidator.validateInputForVRotValue(rotateVBy);

            if (OptionalConfigValidationResponseType.ABORT == responseVRotBy.getResponseType()) {
                vRotResponse.setResponseType(OptionalConfigValidationResponseType.ABORT);
                vRotResponse.setErrorMessage(responseVRotBy.getErrorMessage());
                vRotResponse.setErrorTargetTextField(advancedTextFieldRotateVerticallyBy);
                return vRotResponse;
            } else if (OptionalConfigValidationResponseType.SILENT_ABORT == responseVRotBy.getResponseType()) {
                vRotResponse.setResponseType(OptionalConfigValidationResponseType.SILENT_ABORT);
                return vRotResponse;
            }

            String vRotIndexes = advancedTextFieldRotateVerticallyAtIndexes.getText();
            ValidatorResponse responseVRotIndexes = VRotationInputValidator.validateInputForIndexRangeText(vRotIndexes);

            if (OptionalConfigValidationResponseType.ABORT == responseVRotIndexes.getResponseType()) {
                vRotResponse.setResponseType(OptionalConfigValidationResponseType.ABORT);
                vRotResponse.setErrorMessage(responseVRotIndexes.getErrorMessage());
                vRotResponse.setErrorTargetTextField(advancedTextFieldRotateVerticallyAtIndexes);
                return vRotResponse;
            } else if (OptionalConfigValidationResponseType.SILENT_ABORT == responseVRotIndexes.getResponseType()) {
                vRotResponse.setResponseType(OptionalConfigValidationResponseType.SILENT_ABORT);
                return vRotResponse;
            }

            if (OptionalConfigValidationResponseType.CONTINUE != responseVRotIndexes.getResponseType()) {
                throw new IllegalStateException("Unexpected response at #validateAndGetVRotationOptions [responseType = " + responseVRotIndexes.getResponseType() + "]");
            }

            vRotResponse.setResponseType(OptionalConfigValidationResponseType.CONTINUE);
            vRotResponse.setSeedMutationType(SeedMutationTypeEnum.ROTATE_VERTICAL);
            vRotResponse.setResponseData(new Pair<>(responseVRotBy.getResponseData(), responseVRotIndexes.getResponseData()));
        }
        return vRotResponse;
    }

    private boolean confirmUserChoiceForNewSearchThread(List<String> labelTexts) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.getDialogPane().setPrefWidth(832);

        alert.initModality(Modality.APPLICATION_MODAL);
        alert.getDialogPane().setHeaderText(rb.getString("label.modalHeaderPleaseConfirm"));
        // alert.getDialogPane().setContentText(info);

        VBox content = new VBox();
        labelTexts.stream()
            .filter(Objects::nonNull)
            .forEach(labelString -> {
                if (labelString.startsWith("#SPACER#")) {
                    content.getChildren().add(makeSpecialNotificationLabel(labelString, TextColorEnum.RED));
                    return;
                }
                boolean isWarningLabel = labelString.startsWith("WARNING!");
                Label label = new Label(labelString);
                label.getStyleClass().add(!isWarningLabel ? TextColorEnum.DARK_GRAY.getStyleClass() : TextColorEnum.DARK_ORANGE.getStyleClass());
                label.getStyleClass().add(CssConstants.GENERAL_CONFIRM_DIALOG_LABEL);
                content.getChildren().add(label);
            });

        alert.getDialogPane().setContent(content);

        //must add our default stylesheet for styles to work on Alert
        alert.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getClassLoader().getResource("com.bearlycattable.bait.ui.css/styles.css")).toExternalForm());
        if (advancedSearchAccessProxy.isDarkModeEnabled()) {
            alert.getDialogPane().getStyleClass().add("alertDark");
        }

        Optional<ButtonType> response = alert.showAndWait();

        return response.isPresent() && ButtonType.OK == response.get();
    }

    private Label makeSpecialNotificationLabel(String notificationMessage, TextColorEnum color) {
        if (!notificationMessage.startsWith("#SPACER#")) {
            throw new IllegalArgumentException("Notification message must have the following prefix: #SPACER#");
        }

        Label label = new Label(notificationMessage.substring("#SPACER#".length()));
        label.getStyleClass().add(color.getStyleClass());
        label.getStyleClass().add(CssConstants.ERROR_INFO_MESSAGE_STYLE_CLASS);
        return label;
    }

    private Optional<SeedMutationTypeEnum> retrieveHorizontalMutationTypeForOptionalConfigs() {
        if (advancedSearchRadioOptionalRotateNormal.isSelected()) {
            return Optional.of(SeedMutationTypeEnum.ROTATE_NORMAL);
        }

        if (advancedSearchRadioOptionalRotateWords.isSelected()) {
            return Optional.of(SeedMutationTypeEnum.ROTATE_WORDS);
        }

        if (advancedSearchRadioOptionalRotatePrefixed.isSelected()) {
            return Optional.of(SeedMutationTypeEnum.ROTATE_PREFIXED);
        }

        return Optional.empty();
    }

    private Optional<SeedMutationTypeEnum> retrieveIncOrDecTypeForOptionalConfigs() {
        if (advancedSearchRadioOptionalIncAbsolute.isSelected()) {
            return Optional.of(SeedMutationTypeEnum.INCREMENT_ABSOLUTE);
        }

        if (advancedSearchRadioOptionalIncWords.isSelected()) {
            return Optional.of(SeedMutationTypeEnum.INCREMENT_WORDS);
        }

        if (advancedSearchRadioOptionalDecAbsolute.isSelected()) {
            return Optional.of(SeedMutationTypeEnum.DECREMENT_ABSOLUTE);
        }

        if (advancedSearchRadioOptionalDecWords.isSelected()) {
            return Optional.of(SeedMutationTypeEnum.DECREMENT_WORDS);
        }

        return Optional.empty();
    }

    private Optional<RandomWordPrefixMutationTypeEnum> retrieveIncOrDecForRandomWordPrefix() {
        if (!advancedSearchCbxEnableRandomWordPrefixMutation.isSelected()) {
            return Optional.empty();
        }

        if (advancedSearchRadioRandomWordPrefixInc.isSelected()) {
            return Optional.of(RandomWordPrefixMutationTypeEnum.INCREMENT);
        }

        if (advancedSearchRadioRandomWordPrefixDec.isSelected()) {
            return Optional.of(RandomWordPrefixMutationTypeEnum.DECREMENT);
        }

        return Optional.empty();
    }

    private void enableExactMatchCheckIfEligible(AdvancedSearchHelper advancedSearchHelper, String pathToUnencodedAddressList) {
        if (!advancedSearchHelper.isExactMatchCheckEnabled()) {
            LOG.info("'exact match check' option is disabled for this AdvancedSearchHelper. Will proceed without");
            return;
        }

        advancedSearchHelperProvider.updateTargetForExactMatchCheck(advancedSearchAccessProxy.readUnencodedPubsListIntoSet(pathToUnencodedAddressList), advancedSearchHelper);
    }

    private List<Integer> readDisabledWordsFromUi() {
        return advancedSearchHBoxDisabledWordsParent.getChildren().stream()
                .filter(child -> CheckBox.class.isAssignableFrom(child.getClass()))
                .map(CheckBox.class::cast)
                .filter(CheckBox::isSelected)
                .map(cbx ->  {
                    String id = cbx.getId();
                    int num = Integer.parseInt(id.substring(id.length() - 2));
                    return num > 0 && num < 9 ? num : null;
                })
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    void showInfoMessage(String message, TextColorEnum color) {
        advancedSearchErrorLabel.getStyleClass().clear();
        advancedSearchErrorLabel.getStyleClass().add(CssConstants.ERROR_INFO_MESSAGE_STYLE_CLASS);
        advancedSearchErrorLabel.getStyleClass().add(color.getStyleClass());
        advancedSearchErrorLabel.setText(message);
    }

    void showErrorMessage(String message) {
        advancedSearchErrorLabel.getStyleClass().clear();
        advancedSearchErrorLabel.getStyleClass().add(CssConstants.ERROR_INFO_MESSAGE_STYLE_CLASS);
        advancedSearchErrorLabel.getStyleClass().add(TextColorEnum.RED.getStyleClass());
        advancedSearchErrorLabel.setText(message);
    }

    void removeMessage() {
        advancedSearchErrorLabel.getStyleClass().clear();
        advancedSearchErrorLabel.getStyleClass().add(CssConstants.ERROR_INFO_MESSAGE_STYLE_CLASS);
        advancedSearchErrorLabel.setText(HeatVisualizerConstants.EMPTY_STRING);
    }

    private void addErrorMessageAndRedBorder(String message, Control component) {
        showErrorMessage(message);
        addRedBorder(component);
    }

    void appendToErrorMessage(String dataToAppend) {
        String currentError = advancedSearchErrorLabel.getText();

        advancedSearchErrorLabel.getStyleClass().clear();
        advancedSearchErrorLabel.getStyleClass().add(CssConstants.ERROR_INFO_MESSAGE_STYLE_CLASS);
        advancedSearchErrorLabel.getStyleClass().add(TextColorEnum.RED.getStyleClass());
        advancedSearchErrorLabel.setText(currentError + " " + dataToAppend);
    }

    private void addRedBorder(Control component) {
        if (!component.getStyleClass().contains(CssConstants.BORDER_RED)) {
            component.getStyleClass().add(CssConstants.BORDER_RED);
        }
    }

    private void removeRedBorder(Control component) {
        component.getStyleClass().remove(CssConstants.BORDER_RED);
    }

    private Optional<AdvancedSearchHelper> constructSearchHelperFromUiData() {
        SearchModeEnum searchMode = getAdvancedSearchModeFromUi();

        String saveLocation = advancedSearchTextFieldSaveSearchToFile.getText();
        if (!saveLocation.endsWith(".json")) {
            addErrorMessageAndRedBorder(rb.getString("error.savePathNotJson"), advancedSearchTextFieldSaveSearchToFile);
            advancedBtnSearch.setDisable(false);
            return Optional.empty();
        } else {
            removeRedBorder(advancedSearchTextFieldSaveSearchToFile);
        }

        if (!SearchModeEnum.isRandomRelatedMode(searchMode) && !isValidSeedInUi()) {
            addErrorMessageAndRedBorder(rb.getString("error.seedRequired"), advancedSearchTextFieldContinueFromSeed);
            advancedBtnSearch.setDisable(false);
            return Optional.empty();
        } else {
            removeRedBorder(advancedSearchTextFieldContinueFromSeed);
        }

        ScaleFactorEnum scaleFactor = getAdvancedSearchScaleFactorFromUi();
        int iterations = getAdvancedSearchIterationsFromUi(Config.MAX_ITERATIONS_ADVANCED_SEARCH);

        AdvancedSearchHelperCreationContext context = AdvancedSearchHelperCreationContext.builder()
                .similarityMappings(BaitUtils.buildSimilarityMappings())
                .iterations(SearchHelperIterationsValidator.validateAndGet(searchMode, iterations))
                .accuracy(-1)
                .scaleFactor(scaleFactor)
                .exactMatchCheckEnabled(advancedSearchCbxEnableExactMatchCheck.isSelected())
                .build();

        return advancedSearchHelperProvider.findAdvancedSearchHelper(searchMode, context);
    }

    private int getAdvancedSearchIterationsFromUi(int max) {
        String iterationsInput = advancedSearchTextFieldIterations.getText();
        int validatedIterations;

        if (!HeatVisualizerConstants.DIGITS_ONLY_MAX9.matcher(iterationsInput).matches()) {
            advancedSearchTextFieldIterations.setText(String.valueOf(max));
            validatedIterations = max;
        } else {
            validatedIterations = Integer.parseInt(iterationsInput);
        }

        if (SearchModeEnum.isFiniteMode(getAdvancedSearchModeFromUi())) {
            validatedIterations = SearchHelperIterationsValidator.validateAndGet(getAdvancedSearchModeFromUi(), validatedIterations);
            advancedSearchTextFieldIterations.setText(String.valueOf(validatedIterations));
        }

        return Math.min(validatedIterations, max);
    }

    private ScaleFactorEnum getAdvancedSearchScaleFactorFromUi() {
        return JsonResultScaleFactorEnum.toScaleFactorEnum(advancedSearchComboBoxScaleFactor.getSelectionModel().getSelectedItem());
    }

    private int getNumberOfLoopsFromUi(int max) {
        String loops = advancedSearchTextFieldNumberOfLoops.getText();

        if (!HeatVisualizerConstants.DIGITS_ONLY_MAX3.matcher(loops).matches()) {
            addErrorMessageAndRedBorder(rb.getString("error.loopsNumberInvalid"), advancedSearchTextFieldNumberOfLoops);
            return -1;
        }

        return Math.min(max, Integer.parseInt(loops));
    }

    private boolean isValidSeedInUi() {
        return HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(advancedSearchTextFieldContinueFromSeed.getText()).matches();
    }

    public void modifyAccessToSeedComponent(boolean enabled) {
        advancedSearchSeedComponentParent.setDisable(!enabled);
    }

    public void insertNotificationToleranceConfigToUi(boolean enable) {
        advancedSearchHBoxNotificationToleranceParent.getChildren().clear();

        if (!enable) {
            return;
        }

        HBox hbox = new HBox();
        Label specialLabelTooltip = HeatVisualizerComponentHelper.createPrettyLabelWithTooltip(rb.getString("tooltip.special.notificationToleranceExplanation"));
        hbox.getChildren().add(specialLabelTooltip);
        hbox.getChildren().add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(5, false));
        Label label = new Label(rb.getString("label.notificationTolerance"));
        label.setPrefHeight(32.0);
        hbox.getChildren().add(label);
        hbox.getChildren().add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(5, false));
        TextField tf = new TextField();
        tf.setAlignment(Pos.CENTER);
        tf.setPrefWidth(52.0);
        tf.setText("0");
        tf.setTextFormatter(HeatVisualizerFormatterFactory.getDefaultPositiveNumberFormatter(Config.MAX_SOUND_NOTIFICATION_POINT_BARRIER));
        advancedSearchTextFieldNotificationPointTolerance = tf;
        hbox.getChildren().add(tf);

        //take care of dark mode
        if (advancedSearchAccessProxy.isDarkModeEnabled()) {
           DarkModeHelper.toggleDarkModeForComponent(true, hbox);
        }

        advancedSearchHBoxNotificationToleranceParent.getChildren().add(hbox);
    }

    public void insertModeSpecificOptionsToUi(SearchModeEnum currentMode) {
        advancedSearchHBoxModeSpecificOptionsParent.getChildren().clear();

        if (currentMode == null) {
            return;
        }

        switch (currentMode) {
            case RANDOM_SAME_WORD:
            case RANDOM_PREFIXED_WORD:
                insertComponentsForSomeRandomRelatedModes();
                return;
            default:
                HBox parent = new HBox();
                parent.setAlignment(Pos.CENTER);
                parent.getChildren().add(HeatVisualizerComponentHelper.createLabel(rb.getString("label.additionalOptionsUnavailable"), 16, false));
                if (advancedSearchAccessProxy != null) {
                    DarkModeHelper.toggleDarkModeForComponent(advancedSearchAccessProxy.isDarkModeEnabled(), parent);
                }
                advancedSearchHBoxModeSpecificOptionsParent.getChildren().add(parent);
        }
    }

    private void insertComponentsForSomeRandomRelatedModes() {
        HBox parent = new HBox();
        parent.setAlignment(Pos.CENTER);

        Label lbl1 = new Label(rb.getString("label.randomWordPrefix"));
        lbl1.setAlignment(Pos.CENTER_RIGHT);

        parent.getChildren().add(lbl1);
        parent.getChildren().add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(5, true));

        TextField textField = new TextField();
        textField.setAlignment(Pos.CENTER);
        textField.setPrefWidth(104.0);
        textField.getStyleClass().add("wordPKInput"); //TODO: should depend on font metrics
        textField.setTooltip(new Tooltip(rb.getString("tooltip.randomWordPrefixExplanation")));
        textField.setTextFormatter(HeatVisualizerFormatterFactory.getDefaultWordInputFieldFormatter());
        advancedSearchTextFieldRandomWordPrefix = textField;
        parent.getChildren().add(textField);

        parent.getChildren().add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(5, true));

        CheckBox cbx = new CheckBox(rb.getString("label.cbxMutatePrefixQ"));
        parent.getChildren().add(cbx);
        parent.getChildren().add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(5, true));
        parent.getChildren().add(HeatVisualizerComponentHelper.createPrettyLabelWithTooltip(rb.getString("tooltip.special.prefixMutationExplanation")));
        parent.getChildren().add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(5, true));

        HBox incDecPrefixWrapper = new HBox();
        incDecPrefixWrapper.setAlignment(Pos.CENTER);
        incDecPrefixWrapper.setDisable(true);

        cbx.setOnAction(event -> incDecPrefixWrapper.setDisable(!cbx.isSelected()));
        advancedSearchCbxEnableRandomWordPrefixMutation = cbx;

        ToggleGroup prefixIncDec = new ToggleGroup();
        RadioButton increment = new RadioButton(rb.getString("label.radioIncrement"));
        increment.setToggleGroup(prefixIncDec);
        increment.setSelected(true);
        advancedSearchRadioRandomWordPrefixInc = increment;

        RadioButton decrement = new RadioButton(rb.getString("label.radioDecrement"));
        decrement.setToggleGroup(prefixIncDec);
        advancedSearchRadioRandomWordPrefixDec = decrement;

        incDecPrefixWrapper.getChildren().add(increment);
        incDecPrefixWrapper.getChildren().add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(5, true));
        incDecPrefixWrapper.getChildren().add(decrement);
        incDecPrefixWrapper.getChildren().add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(36, false));
        incDecPrefixWrapper.getChildren().add(new Label("by"));
        incDecPrefixWrapper.getChildren().add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(5, true));

        TextField incDecBy = new TextField();
        incDecBy.setAlignment(Pos.CENTER);
        incDecBy.setPrefWidth(104.0);
        incDecBy.getStyleClass().add("wordPKInput"); //TODO: should depend on font metrics
        incDecBy.setTextFormatter(HeatVisualizerFormatterFactory.getDefaultWordInputFieldFormatter());
        advancedSearchTextFieldRandomWordPrefixIncDecBy = incDecBy;
        incDecBy.setTooltip(new Tooltip(rb.getString("tooltip.randomWordPrefixIncDec")));

        incDecPrefixWrapper.getChildren().add(incDecBy);
        parent.getChildren().add(incDecPrefixWrapper);

        if (advancedSearchAccessProxy != null) {
            DarkModeHelper.toggleDarkModeForComponent(advancedSearchAccessProxy.isDarkModeEnabled(), parent);
        }

        advancedSearchHBoxModeSpecificOptionsParent.getChildren().add(parent);
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

    public final boolean isParentValid() {
        return advancedSearchAccessProxy != null;
    }
}
