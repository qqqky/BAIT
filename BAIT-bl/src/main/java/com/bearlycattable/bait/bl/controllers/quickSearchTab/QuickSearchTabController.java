package com.bearlycattable.bait.bl.controllers.quickSearchTab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.advancedCommons.helpers.HeatVisualizerComponentHelper;
import com.bearlycattable.bait.bl.controllers.RootController;
import com.bearlycattable.bait.advancedCommons.helpers.DarkModeHelper;
import com.bearlycattable.bait.bl.helpers.HeatVisualizerFormatterFactory;
import com.bearlycattable.bait.bl.helpers.QuickSearchTaskHelper;
import com.bearlycattable.bait.commons.CssConstants;
import com.bearlycattable.bait.commons.validators.SearchHelperIterationsValidator;
import com.bearlycattable.bait.bl.searchHelper.context.QuickSearchThreadContext;
import com.bearlycattable.bait.commons.contexts.SimpleSearchHelperCreationContext;
import com.bearlycattable.bait.bl.searchHelper.factory.SimpleSearchHelperFactory;
import com.bearlycattable.bait.commons.Config;
import com.bearlycattable.bait.commons.HeatVisualizerConstants;
import com.bearlycattable.bait.commons.enums.QuickSearchComparisonType;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.enums.TextColorEnum;
import com.bearlycattable.bait.commons.helpers.HeatVisualizerHelper;
import com.bearlycattable.bait.commons.contexts.QuickSearchContext;
import com.bearlycattable.bait.commons.interfaces.SimpleSearchHelper;
import com.bearlycattable.bait.commons.wrappers.PubComparisonResultWrapper;
import com.bearlycattable.bait.commons.wrappers.QuickSearchTaskWrapper;
import com.bearlycattable.bait.utility.BundleUtils;
import com.bearlycattable.bait.utility.LocaleUtils;
import com.bearlycattable.bait.utility.RandomAddressGenerator;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import lombok.Getter;

@Getter
public class QuickSearchTabController {

    private static final Logger LOG = Logger.getLogger(QuickSearchTabController.class.getName());
    private final ResourceBundle rb = ResourceBundle.getBundle(BundleUtils.GLOBAL_BASE_NAME + "QuickSearchTab", LocaleUtils.APP_LANGUAGE);

    @FXML
    private HBox quickSearchHBoxTargetKeySelection;
    @FXML
    private HBox quickSearchSelectSeedParentComponent;
    @FXML
    private HBox advancedOptionalMenuIncDecContainer;
    @FXML
    private RadioButton quickSearchRadioCollisionType;
    @FXML
    private RadioButton quickSearchRadioBlindType;

    //target key selection
    private HBox targetSelectionParent;
    private TextField quickSearchTextFieldTargetKey;
    private Label quickSearchLabelTargetLength;

    @FXML
    private TextField quickSearchTextFieldSimilarityPercent;
    @FXML
    private ChoiceBox<String> quickSearchChoiceBoxSearchMode;
    @FXML
    private TextField quickSearchTextFieldIterations;
    @FXML
    private ComboBox<ScaleFactorEnum> quickSearchComboBoxScaleFactor;
    @FXML
    private CheckBox quickSearchCbxOverrideScaleFactor;
    @FXML
    private CheckBox quickSearchCbxSelfSeed;
    @FXML
    private TextField quickSearchTextFieldSeedPriv;
    @FXML
    private Label quickSearchLabelLengthSeed;
    @FXML
    private Button quickSearchBtnImportSeedPriv;
    @FXML
    private Label quickSearchLabelErrorSuccessMessage;
    @FXML
    private ProgressBar quickSearchProgressBar;
    @FXML
    private Button quickSearchBtnSearch;
    @FXML
    private CheckBox quickSearchCbxMoveToHeatComparisonWhenSearchDone;

    private final HeatVisualizerHelper helper = new HeatVisualizerHelper();
    private final RandomAddressGenerator generator = RandomAddressGenerator.getSecureGenerator(64);
    private final QuickSearchTaskHelper quickSearchTaskHelper = new QuickSearchTaskHelper();
    @Getter
    private final Map<String, Task<PubComparisonResultWrapper>> taskMap = new HashMap<>(); //current tasks
    private RootController rootController;
    // private volatile String currentSearchResultPK;
    @FXML
    private HBox quickSearchHBoxDisabledWordsParent;

    public void setRootController(RootController rootController) {
        this.rootController = rootController;
    }

    @FXML
    void initialize() {
        System.out.println("CREATING: QuickSearchTabController......");

        quickSearchRadioCollisionType.setOnAction(event -> {
            insertTargetChoiceForCollisionSearch();
            modifySelfSeedAvailability();
            removeErrorMessage();
        });

        quickSearchRadioBlindType.setOnAction(event -> {
            insertTargetChoiceForBlindSearch();
            quickSearchCbxSelfSeed.setDisable(true);
            quickSearchSelectSeedParentComponent.setDisable(SearchModeEnum.isRandomRelatedMode(getSearchModeFromUi()));
            removeErrorMessage();
        });

        quickSearchRadioCollisionType.fire();
    }

    private void modifySelfSeedAvailability() {
        SearchModeEnum searchMode = getSearchModeFromUi();

        if (searchMode == null) {
            return;
        }

        switch (searchMode) {
            case RANDOM:
            case RANDOM_SAME_WORD:
            case RANDOM_PREFIXED_WORD:
                quickSearchCbxSelfSeed.setDisable(true);
                break;
            default:
                quickSearchCbxSelfSeed.setDisable(false);
        }
    }

    private void insertTargetChoiceForBlindSearch() {
        quickSearchHBoxTargetKeySelection.getChildren().clear();

        HBox parent = new HBox();
        parent.setAlignment(Pos.CENTER);
        parent.setPrefWidth(1226.0);
        targetSelectionParent = parent;

        //children
        ObservableList<Node> children = parent.getChildren();
        children.add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(65, false));

        Label label01 = new Label(rb.getString("label.publicKeyOfUnknownType"));
        label01.setAlignment(Pos.CENTER_LEFT);
        children.add(label01);
        children.add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(5, false));

        TextField textField = createTextFieldForTargetKey(440.0, HeatVisualizerFormatterFactory.getDefaultUnencodedPublicKeyFormatter());
        quickSearchTextFieldTargetKey = textField;
        textField.textProperty().addListener(listener -> quickSearchLabelTargetLength.setText(Integer.toString(quickSearchTextFieldTargetKey.getText().length())));
        children.add(textField);
        children.add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(5, false));

        HBox labelLengthContainer = HeatVisualizerComponentHelper.createHBoxWithLengthLabel("quickSearchLabelTargetLength");
        quickSearchLabelTargetLength = labelLengthContainer.getChildren().stream()
                .filter(node -> "quickSearchLabelTargetLength".equals(node.getId()))
                .map(Label.class::cast)
                .findAny().orElse(null);
        children.add(labelLengthContainer);
        children.add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(5, false));

        Button importPriv = new Button(rb.getString("label.importFromConverter"));
        importPriv.setOnAction(event -> importPubUnencodedFromConverterTab());
        importPriv.setTooltip(new Tooltip(rb.getString("tooltip.importFromConverterExplanation")));
        children.add(importPriv);
        children.add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(5, false));

        Button exportPriv = new Button(rb.getString("label.exportToConverter"));
        exportPriv.setOnAction(event -> exportPubUnencodedToConverterTab());
        exportPriv.setTooltip(new Tooltip(rb.getString("tooltip.exportToConverterExplanation")));
        children.add(exportPriv);

        if (rootController != null) {
            DarkModeHelper.toggleDarkModeForComponent(rootController.isDarkMode(), parent);
        }

        quickSearchHBoxTargetKeySelection.getChildren().add(parent);
    }

    private void exportPubUnencodedToConverterTab() {
        String key = quickSearchTextFieldTargetKey.getText().trim();

        if (key.isEmpty()) {
            insertErrorMessage("Nothing to export");
        }

        if (!HeatVisualizerConstants.PATTERN_SIMPLE_40.matcher(key).matches()) {
            insertInfoMessage("Key has been exported, but it is not valid", TextColorEnum.GREEN);
        } else {
            insertInfoMessage("Key: " + key + " has been exported to 'Converter' tab", TextColorEnum.GREEN);
        }

        removeErrorMessage();
        rootController.setUnencodedPubInConverterTab(key);
    }

    private void importPubUnencodedFromConverterTab() {
        String unencodedPub = rootController.getUnencodedPubFromConverterTab();

        if (unencodedPub.trim().isEmpty()) {
            insertErrorMessage("Nothing to import");
        }

        removeErrorMessage();
        quickSearchTextFieldTargetKey.setText(unencodedPub);
    }

    private void insertTargetChoiceForCollisionSearch() {
        quickSearchHBoxTargetKeySelection.getChildren().clear();

        HBox parent = new HBox();
        parent.setAlignment(Pos.CENTER);
        parent.setPrefWidth(1226.0);
        targetSelectionParent = parent;

        //children
        ObservableList<Node> children = parent.getChildren();
        children.add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(5, false));

        Label label01 = new Label(rb.getString("label.privateKey"));
        label01.setPrefWidth(120.0);
        label01.setAlignment(Pos.CENTER_LEFT);
        children.add(label01);
        children.add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(5, false));

        TextField textField = createTextFieldForTargetKey(694.0, HeatVisualizerFormatterFactory.getDefaultPrivateKeyFormatter());
        quickSearchTextFieldTargetKey = textField;
        textField.textProperty().addListener(listener -> quickSearchLabelTargetLength.setText(Integer.toString(quickSearchTextFieldTargetKey.getText().length())));

        children.add(textField);
        children.add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(5, false));

        HBox labelLengthContainer = HeatVisualizerComponentHelper.createHBoxWithLengthLabel("quickSearchLabelTargetLength");
        quickSearchLabelTargetLength = labelLengthContainer.getChildren().stream()
                .filter(node -> "quickSearchLabelTargetLength".equals(node.getId()))
                .map(Label.class::cast)
                .findAny().orElse(null);
        children.add(labelLengthContainer);
        children.add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(5, false));

        Button importPriv = new Button(rb.getString("label.importFromConstruction"));
        importPriv.setTooltip(new Tooltip(rb.getString("tooltip.importFromConstructionExplanation")));
        importPriv.setOnAction(event -> importPKFromKeyConstructionTab(quickSearchTextFieldTargetKey));
        children.add(importPriv);
        children.add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(5, false));

        if (rootController != null) {
            DarkModeHelper.toggleDarkModeForComponent(rootController.isDarkMode(), parent);
        }

        quickSearchHBoxTargetKeySelection.getChildren().add(parent);
    }

    private TextField createTextFieldForTargetKey(double textFieldWidth, TextFormatter<String> formatter) {
        TextField textField = new TextField();
        textField.setPrefWidth(textFieldWidth);
        textField.setTextFormatter(formatter);
        return textField;
    }

    /**
     * Imports key from construction tab's 'current input' field into the specified text field
     * @param component
     */
    public void importPKFromKeyConstructionTab(TextInputControl component) {
        String key = importPrivFromMainTab();

        if (!HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(key).matches()) {
           insertErrorMessageAndRedBorder(rb.getString("error.importFailedInvalidKey"), component);
           return;
        }

        insertInfoMessage(rb.getString("info.importSuccessful"), TextColorEnum.GREEN);
        removeRedBorder(component);
        component.setText(key);
    }

    private String importPrivFromMainTab() {
        return rootController.getCurrentInput();
    }

    @FXML
    private void doQuickSearch() {
        quickSearchBtnSearch.setDisable(true);
        QuickSearchComparisonType searchType = getQuickSearchTypeFromUi();

        if (!isTargetValidForSearchType(searchType)) {
            quickSearchTextFieldTargetKey.getStyleClass().add(CssConstants.BORDER_RED);
            insertErrorMessage(rb.getString("error.invalidTarget"));
            quickSearchBtnSearch.setDisable(false);
            return;
        }

        quickSearchTextFieldTargetKey.getStyleClass().remove(CssConstants.BORDER_RED);

        int accuracy = getAccuracyFieldFromUi();
        int iterations = getIterationsFieldFromUi(Config.MAX_ITERATIONS_QUICK_SEARCH);
        SearchModeEnum searchMode = getSearchModeFromUi();

        if (searchMode == null) {
            insertErrorMessage(rb.getString("error.searchModeNotFound"));
            quickSearchBtnSearch.setDisable(false);
            return;
        }

        if (!isSeedValidForSearchMode(searchMode)) {
            quickSearchTextFieldSeedPriv.getStyleClass().add(CssConstants.BORDER_RED);
            insertErrorMessage(rb.getString("error.invalidSeed"));
            quickSearchBtnSearch.setDisable(false);
            return;
        }

        quickSearchTextFieldSeedPriv.getStyleClass().remove(CssConstants.BORDER_RED);
        String runMsg = "Accuracy requested: " + accuracy + ", iterations: " + iterations + ", mode: " + searchMode + ", disabled words: " + (quickSearchHBoxDisabledWordsParent.isDisabled() ? new ArrayList<>() : readDisabledWordsFromUi());
        LOG.info(runMsg);

        SimpleSearchHelperCreationContext context = SimpleSearchHelperCreationContext.builder()
                .similarityMappings(rootController.getSimilarityMappings())
                .iterations(SearchHelperIterationsValidator.validateAndGet(searchMode, iterations))
                .accuracy(accuracy)
                .scaleFactor(getPubAccuracyScaleFactorFromUi())
                .build();

        SimpleSearchHelper simpleSearchHelper = SimpleSearchHelperFactory.findRequestedSearchHelper(searchMode, context, HeatVisualizerConstants.MIXED_SEARCH_SEQUENCE_WITHOUT_RANDOM);

        performSearchParallel(simpleSearchHelper, accuracy);
    }

    public void showQuickSearchResults(@NonNull PubComparisonResultWrapper highest, int forAccuracy) {
        if (!highest.equalsEmpty()) {
            insertInfoMessage(rb.getString("info.searchCompletedForAccuracy") + forAccuracy
                    + System.lineSeparator()
                    + rb.getString("info.bestResultAndAccuracyFound") + highest.getCommonPriv() + " (" + rootController
                    .getSimilarityMappings().get(helper.recalculateIndexForSimilarityMappings(highest.getHighestPoints(), getPubAccuracyScaleFactorFromUi()))  + ")"
                    + System.lineSeparator()
                    + rb.getString("info.resultsInHeatComparisonTab"), TextColorEnum.GREEN);
            displaySearchResults(highest);
            if (quickSearchCbxMoveToHeatComparisonWhenSearchDone.isSelected()) {
                rootController.switchToComparisonTab();
            }
            return;
        }

        insertErrorMessage(rb.getString("error.noSearchResults"));
    }

    private boolean isSeedValidForSearchMode(SearchModeEnum searchMode) {
        if (searchMode == null) {
            return false;
        }

        switch (searchMode) {
            case RANDOM:
            case RANDOM_SAME_WORD:
            case RANDOM_PREFIXED_WORD:
                return true;
            case INCREMENTAL_ABSOLUTE:
            case INCREMENTAL_WORDS:
            case DECREMENTAL_ABSOLUTE:
            case DECREMENTAL_WORDS:
            case ROTATION_PRIV_FULL_NORMAL:
            case ROTATION_PRIV_FULL_PREFIXED:
            case ROTATION_PRIV_INDEX_VERTICAL:
            case ROTATION_PRIV_WORDS:
            case FUZZING:
                if (!quickSearchCbxSelfSeed.isSelected() || quickSearchCbxSelfSeed.isDisabled()) {
                    return HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(quickSearchTextFieldSeedPriv.getText()).matches();
                }

                QuickSearchComparisonType searchType = getQuickSearchTypeFromUi();
                if (QuickSearchComparisonType.COLLISION != searchType) {
                    throw new IllegalStateException("Self seeding is not allowed here.");
                }
                return HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(quickSearchTextFieldTargetKey.getText()).matches();
            default:
                throw new IllegalArgumentException("Selected search mode is not yet supported [searchMode="+searchMode+"]");
        }
    }

    private synchronized Optional<String> spawnBackgroundQuickSearchThread(QuickSearchThreadContext context) {
        quickSearchTaskHelper.prepareTask(context);

        if (!context.isPrepared()) {
            insertErrorMessage(rb.getString("error.taskPreparedIncorrectly"));
            return Optional.empty();
        }

        if (taskMap.size() != 1) {
            insertErrorMessage(rb.getString("error.taskAlreadyRunning"));
            return Optional.empty();
        }

        Thread searchThread = new Thread(context.getSearchTask());
        searchThread.start();

        return Optional.of(context.getThreadId());
    }

    private boolean isTargetValidForSearchType(QuickSearchComparisonType searchType) {
        if (searchType == null) {
           return false;
        }

        String key = quickSearchTextFieldTargetKey.getText();
        switch (searchType) {
            case COLLISION:
                return HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(key).matches();
            case BLIND:
                return HeatVisualizerConstants.PATTERN_SIMPLE_40.matcher(key).matches();
            default:
                throw new IllegalArgumentException("Search type is not supported");
        }
    }

    private List<Integer> readDisabledWordsFromUi() {
        return quickSearchHBoxDisabledWordsParent.getChildren().stream()
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

    private void insertErrorMessage(String message) {
        quickSearchLabelErrorSuccessMessage.getStyleClass().clear();
        quickSearchLabelErrorSuccessMessage.getStyleClass().add(CssConstants.ERROR_INFO_MESSAGE_STYLE_CLASS);
        quickSearchLabelErrorSuccessMessage.getStyleClass().add(TextColorEnum.RED.getStyleClass());
        quickSearchLabelErrorSuccessMessage.setText(message);
    }

    private void insertInfoMessage(String message, TextColorEnum color) {
        quickSearchLabelErrorSuccessMessage.getStyleClass().clear();
        quickSearchLabelErrorSuccessMessage.getStyleClass().add(CssConstants.ERROR_INFO_MESSAGE_STYLE_CLASS);
        quickSearchLabelErrorSuccessMessage.getStyleClass().add(color.getStyleClass());
        quickSearchLabelErrorSuccessMessage.setText(message);
    }

    private void removeErrorMessage() {
        quickSearchLabelErrorSuccessMessage.getStyleClass().clear();
        quickSearchLabelErrorSuccessMessage.getStyleClass().add(CssConstants.ERROR_INFO_MESSAGE_STYLE_CLASS);
        quickSearchLabelErrorSuccessMessage.setText(HeatVisualizerConstants.EMPTY_STRING);
    }

    private QuickSearchComparisonType getQuickSearchTypeFromUi() {
        if (quickSearchRadioCollisionType.isSelected()) {
            return QuickSearchComparisonType.COLLISION;
        } else if (quickSearchRadioBlindType.isSelected()) {
            return QuickSearchComparisonType.BLIND;
        } else {
            throw new IllegalStateException("None of the types is selected");
        }
    }

    private void performSearchParallel(SimpleSearchHelper simpleSearchHelper, int accuracy) {
        String seed = determineInitialSeed(simpleSearchHelper.getSearchMode());
        String targetKey = quickSearchTextFieldTargetKey.getText();
        ArrayList<Integer> disabledWords = quickSearchHBoxDisabledWordsParent.isDisabled() ? new ArrayList<>() : new ArrayList<>(readDisabledWordsFromUi());

        if (targetKey.isEmpty()) {
            insertErrorMessage(rb.getString("error.targetEmpty"));
            return;
        }

        QuickSearchComparisonType type = determineCurrentQuickSearchType();

        if (type == null) {
            return;
        }

        if (!isTargetKeyIsValidForType(targetKey, type)) {
            return;
        }

        QuickSearchContext quickSearchContext = QuickSearchContext.builder()
                .type(type)
                .targetPriv(QuickSearchComparisonType.COLLISION == type ? targetKey : null)
                .targetPub(QuickSearchComparisonType.COLLISION == type ? null : targetKey)
                .seed(seed)
                .disabledWords(disabledWords)
                .searchMode(simpleSearchHelper.getSearchMode())
                .iterations(simpleSearchHelper.getIterations())
                .accuracy(accuracy)
                .verbose(rootController.isVerboseMode())
                .printSpacing(rootController.isVerboseMode() ? 1 : 0)
                .currentHighestResult(PubComparisonResultWrapper.empty())
                .build();

        QuickSearchTaskWrapper wrapper = simpleSearchHelper.createNewQuickSearchTask(quickSearchContext);
        if (!wrapper.hasTask()) {
            insertErrorMessage(wrapper.getError());
            return;
        }

        QuickSearchThreadContext taskContext = QuickSearchThreadContext.builder()
                .searchTask(wrapper.getTask())
                .controller(this)
                .threadId(generator.generateHexString(8).toLowerCase(Locale.ROOT))
                .accuracy(accuracy)
                .build();

        spawnBackgroundQuickSearchThread(taskContext).ifPresent(threadId -> {
            insertInfoMessage("QuickSearch thread [id=" + threadId + "] has been started.", TextColorEnum.GREEN);
        });
    }

    private boolean isTargetKeyIsValidForType(@NonNull String targetKey, @NonNull QuickSearchComparisonType type) {
        switch (type) {
            case COLLISION:
                if (!HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(targetKey).matches()) {
                    insertErrorMessage(rb.getString("error.invalidTargetPkForThisSearchType") + QuickSearchComparisonType.COLLISION);
                    return false;
                }
                break;
            case BLIND:
                if (!HeatVisualizerConstants.PATTERN_SIMPLE_40.matcher(targetKey).matches()) {
                    insertErrorMessage(rb.getString("error.invalidTargetPkhForThisSearchType") + QuickSearchComparisonType.BLIND);
                    return false;
                }
                break;
            default:
                throw new IllegalArgumentException("Requested search type is not implemented [searchType=" + determineCurrentQuickSearchType() + "]");
        }

        return true;
    }

    private String determineInitialSeed(SearchModeEnum searchMode) {
        if (quickSearchCbxSelfSeed.isSelected() && !quickSearchCbxSelfSeed.isDisabled()) {
            return quickSearchTextFieldTargetKey.getText();
        }

        if (SearchModeEnum.RANDOM == searchMode || SearchModeEnum.RANDOM_SAME_WORD == searchMode) {
            return quickSearchTextFieldTargetKey.getText();
        }

        return quickSearchTextFieldSeedPriv.getText();
    }

    private QuickSearchComparisonType determineCurrentQuickSearchType() {
        if (quickSearchRadioBlindType.isSelected() && quickSearchRadioCollisionType.isSelected() ||
                !quickSearchRadioBlindType.isSelected() && !quickSearchRadioCollisionType.isSelected()) {
            throw new IllegalStateException("Either none or both search types are selected. This should never happen");
        }

        if (quickSearchRadioCollisionType.isSelected()) {
            return QuickSearchComparisonType.COLLISION;
        }

        return QuickSearchComparisonType.BLIND;
    }

    private void displaySearchResults(PubComparisonResultWrapper highest) {
        if (!highest.isBothPrivsValidAndNonNull()) {
            throw new IllegalStateException("Search results are not valid. This should not happen");
        }

        highest.getResultAsOptionalForUncompressed().ifPresent(result -> rootController.insertSearchResultsToUiForUncompressed(result));
        highest.getResultAsOptionalForCompressed().ifPresent(result -> rootController.insertSearchResultsToUiForCompressed(result));

        rootController.setCurrentKeyInComparisonTab(highest.getCommonPriv());
        rootController.setReferenceKeyInComparisonTab(quickSearchTextFieldTargetKey.getText(), getQuickSearchTypeFromUi());
        rootController.setScaleFactorInComparisonTab(highest.getCommonScaleFactor());
        rootController.calculateOutputs();
    }

    public int getNormalizedMapIndexFromComparisonResult(int resultPoints, ScaleFactorEnum scaleFactor) {
        return helper.recalculateIndexForSimilarityMappings(resultPoints, scaleFactor == null ? Config.DEFAULT_SCALE_FACTOR : scaleFactor);
    }

    //UI methods
    int getAccuracyFieldFromUi() {
        String requiredAccuracy = quickSearchTextFieldSimilarityPercent.getText();

        int accuracy = validateAndGetOrDefault(requiredAccuracy);
        quickSearchTextFieldSimilarityPercent.setText(Integer.toString(accuracy));

        return accuracy;
    }

    private int validateAndGetOrDefault(String requiredAccuracy) {
        if (!HeatVisualizerConstants.DIGITS_ONLY_MAX3.matcher(requiredAccuracy).matches()) {
            insertErrorMessage(rb.getString("error.invalidAccuracyUseDefault") + Config.DEFAULT_ACCURACY_QUICK_SEARCH);
            return Config.DEFAULT_ACCURACY_QUICK_SEARCH;
        }

        int accuracy = Integer.parseInt(requiredAccuracy);

        if (accuracy < 0 || accuracy > 100) {
            insertErrorMessage(rb.getString("error.invalidAccuracyUseDefault") + Config.DEFAULT_ACCURACY_QUICK_SEARCH);
            return Config.DEFAULT_ACCURACY_QUICK_SEARCH;
        }

        return accuracy;
    }

    int getIterationsFieldFromUi(int max) {
        String iterationsInput = quickSearchTextFieldIterations.getText();
        if (!HeatVisualizerConstants.DIGITS_ONLY_MAX6.matcher(iterationsInput).matches()) {
            quickSearchTextFieldIterations.setText(String.valueOf(max));
            return max;
        }
        int iterations = Integer.parseInt(iterationsInput);
        return Math.min(iterations, max);
    }

    public SearchModeEnum getSearchModeFromUi() {
        return SearchModeEnum.getByLabel(quickSearchChoiceBoxSearchMode.getValue()).orElse(null);
    }

    public ScaleFactorEnum getPubAccuracyScaleFactorFromUi() {
        if (!quickSearchCbxOverrideScaleFactor.isSelected()) {
            return Config.DEFAULT_SCALE_FACTOR;
        }

        return quickSearchComboBoxScaleFactor.getSelectionModel().getSelectedItem();
    }

    public void modifyPubAccuracyScaleFactorTextFieldAccess(boolean enabled) {
        quickSearchComboBoxScaleFactor.setDisable(!enabled);
    }

    void setScaleFactor(ScaleFactorEnum scaleFactor) {
        if (ScaleFactorEnum.DISABLED != scaleFactor) {
            quickSearchCbxOverrideScaleFactor.setSelected(true);
            quickSearchComboBoxScaleFactor.getSelectionModel().select(scaleFactor);
        }
    }

    private void insertErrorMessageAndRedBorder(String message, TextInputControl component) {
        insertErrorMessage(message);
        if (!component.getStyleClass().contains(CssConstants.BORDER_RED)) {
            component.getStyleClass().add(CssConstants.BORDER_RED);
        }
    }

    private void removeRedBorder(TextInputControl component) {
        component.getStyleClass().remove(CssConstants.BORDER_RED);
    }

    public final boolean isParentValid() {
        return rootController != null;
    }
}
