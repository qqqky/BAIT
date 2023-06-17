package com.bearlycattable.bait.bl.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.advanced.UnencodedPubListReader;
import com.bearlycattable.bait.advanced.context.AdvancedSearchHelperCreationContext;
import com.bearlycattable.bait.advanced.searchHelper.AbstractAdvancedSearchHelper;
import com.bearlycattable.bait.advanced.searchHelper.factory.AdvancedSearchHelperFactory;
import com.bearlycattable.bait.advanced.searchHelper.impl.AdvancedSearchHelperRandomPrefixedWord;
import com.bearlycattable.bait.advanced.searchHelper.impl.AdvancedSearchHelperRandomSameWord;
import com.bearlycattable.bait.advancedCommons.contexts.P2PKHSingleResultData;
import com.bearlycattable.bait.advancedCommons.helpers.HeatVisualizerComponentHelper;
import com.bearlycattable.bait.advancedCommons.helpers.P2PKHSingleResultDataHelper;
import com.bearlycattable.bait.advancedCommons.contexts.AdvancedSearchContext;
import com.bearlycattable.bait.advancedCommons.interfaces.AdvancedSearchHelper;
import com.bearlycattable.bait.advancedCommons.models.ThreadSpawnModel;
import com.bearlycattable.bait.advancedCommons.serialization.SerializedSearchResultsReader;
import com.bearlycattable.bait.advancedCommons.validators.OptionalConfigValidationResponseType;
import com.bearlycattable.bait.advancedCommons.validators.VRotationInputValidator;
import com.bearlycattable.bait.advancedCommons.validators.ValidatorResponse;
import com.bearlycattable.bait.advancedCommons.wrappers.AdvancedSearchTaskWrapper;
import com.bearlycattable.bait.bl.contexts.TaskPreparationContext;
import com.bearlycattable.bait.bl.helpers.DarkModeHelper;
import com.bearlycattable.bait.bl.helpers.HeatVisualizerFormatterFactory;
import com.bearlycattable.bait.commons.Config;
import com.bearlycattable.bait.commons.CssConstants;
import com.bearlycattable.bait.commons.HeatVisualizerConstants;
import com.bearlycattable.bait.commons.contexts.TaskDiagnosticsModel;
import com.bearlycattable.bait.commons.dataAccessors.ThreadComponentDataAccessor;
import com.bearlycattable.bait.commons.enums.JsonResultScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.LogTextTypeEnum;
import com.bearlycattable.bait.commons.enums.OutputCaseEnum;
import com.bearlycattable.bait.commons.enums.RandomWordPrefixMutationTypeEnum;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.enums.SeedMutationTypeEnum;
import com.bearlycattable.bait.commons.enums.TextColorEnum;
import com.bearlycattable.bait.commons.helpers.HeatVisualizerModalHelper;
import com.bearlycattable.bait.commons.validators.SearchHelperIterationsValidator;
import com.bearlycattable.bait.utility.AddressModifier;
import com.bearlycattable.bait.utility.BundleUtils;
import com.bearlycattable.bait.utility.LocaleUtils;
import com.bearlycattable.bait.utility.PathUtils;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableStringValue;
import javafx.concurrent.Task;
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
    private static final AddressModifier modifier = new AddressModifier(OutputCaseEnum.UPPERCASE);
    private final ResourceBundle rb = ResourceBundle.getBundle(BundleUtils.GLOBAL_BASE_NAME + "AdvancedSubTabSearch", LocaleUtils.APP_LANGUAGE);
    //format: Pair<PathToTemplate, TemplateData>
    private Pair<String, P2PKHSingleResultData[]> loadedSearchTemplateData;

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

    //iterations log option
    @FXML
    private TextField advancedSearchTextFieldLogKeyEveryXIterations;
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

    private AdvancedTabMainController parentController;
    @FXML
    @Getter
    private CheckBox advancedSearchCbxEnableSoundNotifications;
    @FXML
    @Getter
    private HBox advancedSearchHBoxNotificationToleranceParent;

    @FXML
    void initialize() {
        System.out.println("CREATING (child): AdvancedSubTabSearchController......");
    }

    public void initDevDefaults() {
        //dev
    }

    public void setParentController(AdvancedTabMainController parentController) {
        this.parentController = Objects.requireNonNull(parentController);
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

        parentController.loadAdvancedSearchResultsToUi(loadedSearchTemplateData.getKey(), P2PKHSingleResultData.toDataMap(loadedSearchTemplateData.getValue()));
        showInfoMessage("Search results have been loaded (only first 20 are displayed). Use filter to browse all possible", TextColorEnum.GREEN);
        parentController.switchToChildTabX(3);
    }

    @FXML
    private void doSelectSearchSaveTargetPath() {
        HeatVisualizerModalHelper.selectJsonResourceForSave(rb.getString("label.modalSelectSaveDestination"), advancedSearchTextFieldSaveSearchToFile).ifPresent(absPath -> {
            advancedSearchTextFieldSaveSearchToFile.setText(absPath);
        });
    }

    @FXML
    private void doImportPriv() {
        String key = parentController.importDataFromCurrentInputFieldInMainTab();

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
        parentController.exportDataToCurrentInputFieldInMainTab(key);
        showInfoMessage(rb.getString("info.exportSuccess"), TextColorEnum.GREEN);
    }

    SearchModeEnum getAdvancedSearchModeFromUi() {
        return SearchModeEnum.getByLabel(advancedSearchChoiceBoxSearchMode.getSelectionModel().getSelectedItem()).orElse(null);
    }

    @FXML
    private void doAdvancedSearch() {
        advancedBtnSearch.setDisable(true);

        if (parentController.getTaskMap().keySet().size() >= (Runtime.getRuntime().availableProcessors() - 1)) {
            showErrorMessage("Enough tasks are already running. Cannot spawn more threads");
            //TODO: also check if any of the tasks are finished already - remove them from UI?
            //maybe it is better to return threadId rather than boolean from 'spawnBackgroundSearchThread'?
            advancedBtnSearch.setDisable(false);
            return;
        }

        //must check if all 5 steps have been completed beforehand
        if (loadedSearchTemplateData == null || loadedSearchTemplateData.getValue() == null) {
            showErrorMessage(rb.getString("error.resultTemplateMustBeLoaded"));
            addRedBorder(advancedSearchTextFieldLoadSearchTemplateFromFile);
            addRedBorder(advancedBtnLoadExisting);
            advancedBtnSearch.setDisable(false);
            return;
        } else {
            removeRedBorder(advancedSearchTextFieldLoadSearchTemplateFromFile);
            removeRedBorder(advancedBtnLoadExisting);
            removeMessage();
        }

        //REMINDER: num of iterations are included while constructing the SimpleSearchHelper!
        AdvancedSearchHelper advancedSearchHelper = constructSearchHelperFromUiData();

        if (advancedSearchHelper == null) {
            appendToErrorMessage(rb.getString("error.searchHelperConstructionError"));
            advancedBtnSearch.setDisable(false);
            return;
        } else {
            enableExactMatchCheckIfEligible(advancedSearchHelper);
            removeMessage();
        }

        int logSpacing = Integer.parseInt(advancedSearchTextFieldLogKeyEveryXIterations.getText());

        //must validate seed for all modes (and generate any for random-related ones)
        String seed = buildSeed(advancedSearchHelper);
        if (seed.isEmpty()) {
            addErrorMessageAndRedBorder("Invalid seed. Must be 64 hex characters", advancedSearchTextFieldContinueFromSeed);
            return;
        }
        removeRedBorder(advancedSearchTextFieldContinueFromSeed);

        //validate save location
        String saveLocation = advancedSearchTextFieldSaveSearchToFile.getText();
        if (!saveLocation.endsWith(".json") || !PathUtils.isAccessibleToWrite(saveLocation)) {
            addErrorMessageAndRedBorder("Invalid save location. Must be a .json file, must have write access", advancedSearchTextFieldSaveSearchToFile);
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
            Optional<Map<SeedMutationTypeEnum, Object>> maybeSeedMutationConfigs = buildSeedMutationConfigs();
            if (!maybeSeedMutationConfigs.isPresent()) {
                return;
            }
            seedMutationConfigs = maybeSeedMutationConfigs.get();
        }

        Pair<RandomWordPrefixMutationTypeEnum, String> modeSpecificConfigs = null;
        String randomWordPrefix = null;

        if (advancedSearchHelper instanceof AdvancedSearchHelperRandomSameWord || advancedSearchHelper instanceof AdvancedSearchHelperRandomPrefixedWord) {
            randomWordPrefix = advancedSearchTextFieldRandomWordPrefix.getText();
            if (!randomWordPrefix.isEmpty()) {
                if (!HeatVisualizerConstants.PATTERN_SIMPLE_08_OR_LESS.matcher(randomWordPrefix).matches()) {
                    advancedBtnSearch.setDisable(false);
                    addErrorMessageAndRedBorder(rb.getString("error.randomWordPrefixInvalid"), advancedSearchTextFieldRandomWordPrefix);
                    return;
                }
                parentController.logToUi("Setting random word prefix to: " + randomWordPrefix, Color.WHEAT, LogTextTypeEnum.START_OF_SEARCH);
                RandomWordPrefixMutationTypeEnum prefixMutationType = retrieveIncOrDecForRandomWordPrefix().orElse(null);
                modeSpecificConfigs = new Pair<>(prefixMutationType, advancedSearchTextFieldRandomWordPrefixIncDecBy.getText());
            }
        }

        //handle sound notification tolerance selection
        int soundNotificationTolerance = 0;
        if (advancedSearchCbxEnableSoundNotifications.isSelected()) {
            Optional<Integer> notificationTolerance = getSoundNotificationTolerance();
            if (!notificationTolerance.isPresent()) {
                addErrorMessageAndRedBorder(rb.getString("error.notificationToleranceInvalidInput"), advancedSearchTextFieldNotificationPointTolerance);
                advancedBtnSearch.setDisable(false);
                return;
            }
            removeMessage();
            removeRedBorder(advancedSearchTextFieldNotificationPointTolerance);

            if (notificationTolerance.get() > getAdvancedSearchScaleFactorFromUi().getMaxPoints().intValue()) {
                addErrorMessageAndRedBorder(rb.getString("error.notificationToleranceExceedsMax"), advancedSearchTextFieldNotificationPointTolerance);
                advancedBtnSearch.setDisable(false);
                return;
            }

            removeMessage();
            removeRedBorder(advancedSearchTextFieldNotificationPointTolerance);
            soundNotificationTolerance = notificationTolerance.get();
        }

        //need to make a proper copy for each thread
        P2PKHSingleResultData[] deepCopy = P2PKHSingleResultDataHelper.deepCopy(loadedSearchTemplateData.getValue());

        //TODO: only for testing
        System.out.println("Creating ThreadSpawnModel");
        ThreadSpawnModel threadSpawnModel = ThreadSpawnModel.builder()
                .advancedSearchHelper(advancedSearchHelper)
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

        //confirm user choice
        if (!confirmUserChoiceForNewSearchThread(threadSpawnModel.makeLabelListForUserNotification((t,u,v) -> parentController.getHelper().buildMutatedSeed(t, u, v)))) {
            System.out.println("User did not accept the search parameters. Search will not proceed");
            advancedBtnSearch.setDisable(false);
            return;
        }
        //TODO: only for testing
        System.out.println("User accepted the search config!");

        //init caches if possible
        if (deepCopy.length < Config.MAX_CACHEABLE_ADDRESSES_IN_TEMPLATE) {
            P2PKHSingleResultDataHelper.initializeCaches(deepCopy, ScaleFactorEnum.toJsonScaleFactorEnum(Objects.requireNonNull(advancedSearchHelper).getScaleFactor()));
        } else {
            String templateCannotBeCached = "Proceeding with uncached search! (user has been warned)";
            if (parentController.isVerboseMode()) {
                LOG.info(templateCannotBeCached);
                parentController.logToUi(templateCannotBeCached, Color.DARKORANGE, LogTextTypeEnum.START_OF_SEARCH);
            }
        }

        //TODO: only for testing
        System.out.println("Will now be spawning background search thread");
        spawnBackgroundSearchThread(threadSpawnModel).ifPresent(threadNum -> {
            String message = "New parent search thread " + threadNum + " has been spawned (manually by the user)";
            if (parentController.isVerboseMode()) {
                LOG.info(message);
            }
            parentController.logToUi(message, Color.GREEN, LogTextTypeEnum.GENERAL);
            parentController.switchToChildTabX(2); //switch to 'Progress' tab
        });

        advancedBtnSearch.setDisable(false);
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

                if (!HeatVisualizerConstants.PATTERN_SIMPLE_08_OR_LESS.matcher(incDecBy).matches()) {
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
        if (parentController.isDarkModeEnabled()) {
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

    private void enableExactMatchCheckIfEligible(AdvancedSearchHelper advancedSearchHelper) {
        if (!(advancedSearchHelper instanceof AbstractAdvancedSearchHelper && advancedSearchHelper.isExactMatchCheckEnabled())) {
            LOG.info("'exact match check' option is either disabled or not eligible for this AdvancedSearchHelper. Will proceed without");
            return;
        }

        //TODO: make 'exact match' path properly configurable
        Map<String, Object> myPKHMap = UnencodedPubListReader.readUnencodedPubsListIntoMap(Config.EXACT_MATCH_ADDRESSES_LIST_PATH);
        ((AbstractAdvancedSearchHelper) advancedSearchHelper).changeExactPKHMappingsTo(myPKHMap);
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

    //REMINDER: TASKS cannot be reused by design! So remove them from the list once done
    //TODO: move this somewhere else?
    public synchronized Optional<String> spawnBackgroundSearchThread(ThreadSpawnModel threadSpawnModel) {
        //unpack
        AdvancedSearchHelper advancedSearchHelper = threadSpawnModel.getAdvancedSearchHelper();
        P2PKHSingleResultData[] data = threadSpawnModel.getDeepDataCopy();
        String saveLocation = threadSpawnModel.getSaveLocation();
        String seed = threadSpawnModel.getSeed();
        int loops = threadSpawnModel.getRemainingLoops();
        int logSpacing = threadSpawnModel.getLogSpacing();
        // Map<SeedMutationTypeEnum, String> optionalConfigs = threadSpawnModel.getOptionalConfigs();
        // List<Integer> verticalRotationIndexes = threadSpawnModel.getVerticalRotationIndexes();

        //message displayed on parent's TitlePane
        String titleMessage = "Search mode: " + advancedSearchHelper.getSearchMode() + ", iterations per loop: " + advancedSearchHelper.getIterations() + ", total loops: " + loops;

        //gui message placeholders
        String error;
        String info;
        
        //create component in UI that will display the running thread
        Optional<ThreadComponentDataAccessor> maybeAccessor = parentController.addNewThreadProgressContainerToProgressAndResultsTab(threadSpawnModel.getParentThreadId(), titleMessage);
        if (!maybeAccessor.isPresent()) {
            error = "Received wrong component handles at #spawnBackgroudSearchThread. Cannot continue.";
            if (parentController.isVerboseMode()) {
                parentController.logToUi(error, Color.RED, LogTextTypeEnum.START_OF_SEARCH);
                System.out.println(error);
            }
            return Optional.empty();
        }

        ThreadComponentDataAccessor accessor = maybeAccessor.get();

        List<Integer> disabledWordsCopy = new ArrayList<>(readDisabledWordsFromUi());
        if (parentController.isVerboseMode()) {
            info = "Disabled words at thread init are: " + disabledWordsCopy;
            parentController.logToUi(info, Color.GREEN, LogTextTypeEnum.START_OF_SEARCH);
            System.out.println(info);
        }

        String threadNum = accessor.getThreadNum();
        String parentThreadId = threadSpawnModel.getParentThreadId();

        //used later for logging
        boolean firstLoop = parentThreadId == null;

        if (parentThreadId == null) {
            parentThreadId = threadNum;
            //set parent id to follow diagnostics properly
            threadSpawnModel.setParentThreadId(parentThreadId);
            parentController.getTaskDiagnosticsTree().put(parentThreadId, new HashMap<>());
        }

        //TODO: we must track the tree of threads branching from every parent
        parentController.getTaskDiagnosticsTree().get(parentThreadId).put(threadNum, TaskDiagnosticsModel.empty());
        // parentController.getTaskDiagnosticsTree().put(threadNum, TaskDiagnosticsModel.empty());

        ObservableStringValue progressLabelValue = new SimpleStringProperty();

        AdvancedSearchContext advancedSearchContext = AdvancedSearchContext.builder()
                .dataArray(data)
                .disabledWords(disabledWordsCopy)
                .seed(seed)
                .wordPrefix(threadSpawnModel.getPrefix())
                .observableProgressLabelValue(progressLabelValue)
                .printSpacing(logSpacing)
                .progressSpacing(advancedSearchHelper.getIterations() / 1000 > 0 ? (advancedSearchHelper.getIterations() / 1000) : 1) //~0.1%
                .taskDiagnosticsModel(parentController.getTaskDiagnosticsTree().get(parentThreadId).get(threadNum))
                .pointThresholdForNotify(threadSpawnModel.getPointThresholdForNotify())
                .logConsumer((message, color, type) -> parentController.logToUi(message, color, type))
                .searchMode(advancedSearchHelper.getSearchMode())
                .parentThreadId(parentThreadId)
                .verbose(parentController.isVerboseMode())
                .build();


        //decrement remaining loops
        threadSpawnModel.setRemainingLoops(threadSpawnModel.getRemainingLoops() - 1);
        if (parentController.isVerboseMode()) {
            info = "Loops remaining: " + threadSpawnModel.getRemainingLoops();
            parentController.logToUi(info, Color.GREEN, LogTextTypeEnum.START_OF_SEARCH);
            System.out.println(info);
        }
        
        AdvancedSearchTaskWrapper taskWrapper = advancedSearchHelper.createNewAdvancedSearchTask(advancedSearchContext);
        if (!taskWrapper.hasTask()) {
            error = taskWrapper.getError();
            advancedBtnSearch.setDisable(threadSpawnModel.getRemainingLoops() > 0);
            parentController.logToUi(error, Color.RED, LogTextTypeEnum.START_OF_SEARCH);
            System.out.println(error);
            return Optional.empty();
        }
        
        Task<P2PKHSingleResultData[]> searchTask = taskWrapper.getTask();

        parentController.prepareTask(TaskPreparationContext.builder()
                .searchTask(searchTask)
                .firstLoop(firstLoop)
                .accessor(accessor)
                .taskMap(parentController.getTaskMap())
                .taskResultsMap(parentController.getTaskResultsMap())
                .saveLocation(saveLocation)
                .observableProgressLabelValue(progressLabelValue)
                .taskDiagnosticsModel(parentController.getTaskDiagnosticsTree().get(parentThreadId).get(threadNum))
                //options below are used for loops
                .advancedSubTabSearchController(this)
                .threadSpawnModel(threadSpawnModel)
                .verboseMode(parentController.isVerboseMode())
                .build());

        //since we aren't using executor service (executorService.submit(searchTask)) - must construct and run the
        //task on a separate thread manually:
        Thread searchThread = new Thread(searchTask);
        searchThread.start(); //do not call searchThread.run() or it will run on the same thread???

        return Optional.of(threadNum);
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

    private AdvancedSearchHelper constructSearchHelperFromUiData() {
        SearchModeEnum searchMode = getAdvancedSearchModeFromUi();

        String saveLocation = advancedSearchTextFieldSaveSearchToFile.getText();
        if (!saveLocation.endsWith(".json")) {
            addErrorMessageAndRedBorder(rb.getString("error.savePathNotJson"), advancedSearchTextFieldSaveSearchToFile);
            advancedBtnSearch.setDisable(false);
            return null;
        } else {
            removeRedBorder(advancedSearchTextFieldSaveSearchToFile);
        }

        if (!SearchModeEnum.isRandomRelatedMode(searchMode) && !isValidSeedInUi()) {
            addErrorMessageAndRedBorder(rb.getString("error.seedRequired"), advancedSearchTextFieldContinueFromSeed);
            advancedBtnSearch.setDisable(false);
            return null;
        } else {
            removeRedBorder(advancedSearchTextFieldContinueFromSeed);
        }

        ScaleFactorEnum scaleFactor = getAdvancedSearchScaleFactorFromUi();
        int iterations = getAdvancedSearchIterationsFromUi(Config.MAX_ITERATIONS_ADVANCED_SEARCH);

        AdvancedSearchHelperCreationContext context = AdvancedSearchHelperCreationContext.builder()
                .similarityMappings(new HashMap<>(parentController.getSimilarityMappings()))
                .iterations(SearchHelperIterationsValidator.validateAndGet(searchMode, iterations))
                .accuracy(-1)
                .scaleFactor(scaleFactor)
                .exactMatchCheckEnabled(advancedSearchCbxEnableExactMatchCheck.isSelected())
                .build();

        return AdvancedSearchHelperFactory.findRequestedSearchHelper(searchMode, context, HeatVisualizerConstants.MIXED_SEARCH_SEQUENCE_WITHOUT_RANDOM);
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
        if (parentController.isDarkModeEnabled()) {
           DarkModeHelper.toggleDarkModeForComponent(true, hbox);
        }

        advancedSearchHBoxNotificationToleranceParent.getChildren().add(hbox);
    }

    public void insertModeSpecificOptionsToUi(SearchModeEnum currentMode) {
        advancedSearchHBoxModeSpecificOptionsParent.getChildren().clear();

        switch (currentMode) {
            case RANDOM_SAME_WORD:
            case RANDOM_PREFIXED_WORD:
                insertComponentsForSomeRandomRelatedModes();
                return;
            default:
                HBox parent = new HBox();
                parent.setAlignment(Pos.CENTER);
                parent.getChildren().add(HeatVisualizerComponentHelper.createLabel(rb.getString("label.additionalOptionsUnavailable"), 16, false));
                if (parentController != null) {
                    DarkModeHelper.toggleDarkModeForComponent(parentController.isDarkModeEnabled(), parent);
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

        if (parentController != null) {
            DarkModeHelper.toggleDarkModeForComponent(parentController.isDarkModeEnabled(), parent);
        }

        advancedSearchHBoxModeSpecificOptionsParent.getChildren().add(parent);
    }
}
