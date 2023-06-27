package com.bearlycattable.bait.bl.controllers.advancedTab;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.bearlycattable.bait.commons.CssConstants;
import com.bearlycattable.bait.commons.HeatVisualizerConstants;
import com.bearlycattable.bait.advancedCommons.contexts.P2PKHSingleResultData;
import com.bearlycattable.bait.commons.enums.JsonResultScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.JsonResultTypeEnum;
import com.bearlycattable.bait.commons.enums.LogTextTypeEnum;
import com.bearlycattable.bait.commons.enums.QuickSearchComparisonType;
import com.bearlycattable.bait.commons.enums.TextColorEnum;
import com.bearlycattable.bait.commons.helpers.HeatVisualizerModalHelper;
import com.bearlycattable.bait.advancedCommons.helpers.P2PKHSingleResultDataHelper;
import com.bearlycattable.bait.advancedCommons.serialization.SerializedSearchResultsReader;
import com.bearlycattable.bait.utility.BundleUtils;
import com.bearlycattable.bait.utility.LocaleUtils;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;

public class AdvancedSubTabResultsController {

    private static final Logger LOG = Logger.getLogger(AdvancedSubTabResultsController.class.getName());
    private final ResourceBundle rb = ResourceBundle.getBundle(BundleUtils.GLOBAL_BASE_NAME + "AdvancedSubTabResults", LocaleUtils.APP_LANGUAGE);
    private static final String BTN_ID_PREFIX = "advancedBtnShowHeat"; //for 'show result in main tab' buttons
    private static final int FILTERED_RESULTS_UI_LIMIT = 20;
    private AdvancedTabMainController parentController;
    @Setter
    private P2PKHSingleResultData[] loadedResultTemplate;
    private volatile boolean filtered;

    private Map<String, Map<JsonResultTypeEnum, Map<JsonResultScaleFactorEnum, Pair<String, Integer>>>> mapForResults = null;
    private Map<String, Map<JsonResultTypeEnum, Map<JsonResultScaleFactorEnum, Pair<String, Integer>>>> filteredMap;

    @FXML
    private Button advancedResultsBtnJustShowMeBest;
    @FXML
    private TextField advancedResultsTextFieldUHPKey;
    @FXML
    private TextField advancedResultsUHPAcc;
    @FXML
    private TextField advancedResultsTextFieldUHNKey;
    @FXML
    private TextField advancedResultsUHNAcc;
    @FXML
    private TextField advancedResultsTextFieldCHPKey;
    @FXML
    private TextField advancedResultsCHPAcc;
    @FXML
    private TextField advancedResultsTextFieldCHNKey;
    @FXML
    private TextField advancedResultsCHNAcc;
    @FXML
    @Getter
    private TextField advancedResultsFilterPubPrefix;
    @FXML
    @Getter
    private TextField advancedResultsFilterTextFieldAccuracyMin;
    @FXML
    @Getter
    private ComboBox<JsonResultScaleFactorEnum> advancedResultsComboFilterResultsScaleFactor;
    @FXML
    @Getter
    private TextField advancedResultsFilterTextFieldMaxResults;
    @FXML
    private Button advancedResultsBtnFilterResults;
    @FXML
    private Label advancedResultsLabelFilterMessage; //error/success message for filtering

    //results
    @FXML
    private ComboBox<String> advancedResultsComboBoxSelectPubFromResults;
    @FXML
    @Getter
    private ComboBox<JsonResultScaleFactorEnum> advancedResultsComboResultsScaleFactor;
    @FXML
    private TextField advancedResultsTextFieldSelectAndLoadFromFile;
    @FXML
    private Button advancedResultsBtnDoLoadSearchResults;

    @FXML
    private Button advancedResultsBtnShowHeatUHP;
    @FXML
    private Button advancedResultsBtnShowHeatUHN;
    @FXML
    private Button advancedResultsBtnShowHeatCHP;
    @FXML
    private Button advancedResultsBtnShowHeatCHN;

    @FXML
    void initialize() {
        System.out.println("CREATING (child): AdvancedSubTabResultsController......");
    }

    public void setParentController(AdvancedTabMainController parentController) {
        this.parentController = Objects.requireNonNull(parentController);
    }

    @FXML
    private void doShowResultsForSelectedHash() {
        showResultsForSelectedHash();
    }

    @FXML
    private void doShowResultsForSelectedScaleFactor() {
        showResultsForSelectedHash();
    }

    @FXML
    private void doShowBestMatch() {
        if (mapForResults == null) {
            if (loadedResultTemplate == null) {
                advancedResultsBtnJustShowMeBest.setDisable(true);
                showErrorLabel("No loaded result template has been found");
                return;
            }
            mapForResults = P2PKHSingleResultDataHelper.toDataMap(loadedResultTemplate);
        }

        String bestResult = null;
        int bestAccuracy = -1;
        JsonResultScaleFactorEnum sf = getSelectedResultScaleFactorFromUi();

        for (String key : mapForResults.keySet()) {
            Map<JsonResultTypeEnum, Map<JsonResultScaleFactorEnum, Pair<String, Integer>>> dataByResultType = mapForResults.get(key);
            for (JsonResultTypeEnum type : dataByResultType.keySet()) {
                Pair<String, Integer> resultsForScaleFactor = dataByResultType.get(type).get(sf);
                if (bestAccuracy < resultsForScaleFactor.getValue()) {
                    bestAccuracy = resultsForScaleFactor.getValue();
                    bestResult = key;
                } else if (bestAccuracy == resultsForScaleFactor.getValue()) {
                    if (bestResult != null && bestResult.equals(key)) {
                        continue;
                    }
                    bestResult = compareByWeight(bestResult, key, sf);
                    if (bestResult == null) {
                        showErrorLabel(rb.getString("error.cannotShowBestMatch"));
                        return;
                    }
                }
            }
        }
        removeErrorLabel();

        advancedResultsComboBoxSelectPubFromResults.getSelectionModel().select(bestResult);
        if (parentController.isVerboseMode()) {
            System.out.println("Best result: " + bestResult + " (acc: " + bestAccuracy + ")");
        }
    }

    private String compareBySum(String bestHash, String currentHash, JsonResultScaleFactorEnum sf) {
        Objects.requireNonNull(mapForResults);

        if (mapForResults.get(bestHash).keySet().size() != mapForResults.get(currentHash).keySet().size()) {
            throw new IllegalStateException("Collection has been corrupted at AdvancedSubTabResultsController#compareBySum");
        }

        int sum1 = mapForResults.get(bestHash).keySet().stream()
                .map(key -> mapForResults.get(bestHash).get(key).get(sf).getValue())
                .filter(val -> val instanceof Integer && val >= 0 && val <= 100)
                .reduce(0, Integer::sum);

        int sum2 = mapForResults.get(currentHash).keySet().stream()
                .map(key -> mapForResults.get(currentHash).get(key).get(sf).getValue())
                .filter(val -> val instanceof Integer && val >= 0 && val <= 100)
                .reduce(0, Integer::sum);

        return sum1 >= sum2 ? bestHash : currentHash;
    }

    private String compareByWeight(String bestHash, String currentHash, JsonResultScaleFactorEnum sf) {
        Objects.requireNonNull(mapForResults);

        if (mapForResults.get(bestHash).keySet().size() != mapForResults.get(currentHash).keySet().size()) {
            throw new IllegalStateException("Collection has been corrupted at AdvancedSubTabResultsController#compareByWeight");
        }

        List<Integer> items1 = mapForResults.get(bestHash).keySet().stream()
                .map(key -> mapForResults.get(bestHash).get(key).get(sf).getValue())
                .filter(val -> val instanceof Integer && val >= 0 && val <= 100)
                .sorted()
                .collect(Collectors.toList());


        List<Integer> items2 = mapForResults.get(currentHash).keySet().stream()
                .map(key -> mapForResults.get(currentHash).get(key).get(sf).getValue())
                .filter(val -> val instanceof Integer && val >= 0 && val <= 100)
                .sorted()
                .collect(Collectors.toList());

        if (items1.size() != items2.size()) {
            return null;
        }

        int a = 0;
        int b = 0;

        for (int i = 0; i < items1.size(); i++) {
            a = items1.get(i);
            b = items2.get(i);
            if (a != b) {
                break;
            }
        }

        return a >= b ? bestHash : currentHash;
    }

    @FXML
    private void doShowResultsInHeatComparisonTab(ActionEvent actionEvent) {
        String selectedHash = getSelectedHashFromUi();

        if (selectedHash == null) {
            showErrorLabel(rb.getString("error.noHashSelected"));
            return;
        }
        removeErrorLabel();
        JsonResultTypeEnum type = getSelectedResultTypeFromUi(actionEvent);
        JsonResultScaleFactorEnum scaleFactor = getSelectedResultScaleFactorFromUi();

        if (loadedResultTemplate == null || loadedResultTemplate.length == 0) {
            showErrorLabel("No result template found");
            return;
        }

        //reload the map in case it has changed
        mapForResults = P2PKHSingleResultDataHelper.toDataMap(loadedResultTemplate);
        if (mapForResults == null || mapForResults.isEmpty()) {
            showErrorLabel("Result template appears to be invalid. Cannot show heat comparison");
            return;
        }

        String priv = getPrivFromResultMap(selectedHash, type, scaleFactor);
        int acc = getAccuracyFromResultMap(selectedHash, type, scaleFactor);

        if (parentController.isVerboseMode()) {
            System.out.println("Expected accuracy for hash " + selectedHash + " at scale factor " + scaleFactor + " for priv key:\r\n" + priv + " - " + acc);
        }

        parentController.setReferenceKeyInComparisonTab(selectedHash, QuickSearchComparisonType.BLIND);
        parentController.setCurrentKeyInComparisonTab(priv);
        parentController.setScaleFactorInComparisonTab(JsonResultScaleFactorEnum.toScaleFactorEnum(scaleFactor));
        parentController.calculateOutputsInHeatComparisonTab();

        parentController.switchToComparisonTab();
    }

    @FXML
    private void doFilterExistingResults() {
        String startingPrefix = advancedResultsFilterPubPrefix.getText().trim();
        if (!isValidPubPrefix(startingPrefix)) {
            showErrorLabel("Invalid filter prefix. It must only contain HEX characters");
            return;
        }
        removeErrorLabel();
        JsonResultScaleFactorEnum filterScaleFactorValue = advancedResultsComboFilterResultsScaleFactor.getSelectionModel().getSelectedItem();

        int minAccuracy = Integer.parseInt(advancedResultsFilterTextFieldAccuracyMin.getText());
        if (minAccuracy < 0 || minAccuracy > 100) {
            showErrorLabel(rb.getString("error.accuracyOutOfRange"));
            return;
        }
        removeErrorLabel();
        int maxResults = Integer.parseInt(advancedResultsFilterTextFieldMaxResults.getText());

        mapForResults = P2PKHSingleResultDataHelper.toDataMap(loadedResultTemplate);
        filteredMap = filterHelper(mapForResults, startingPrefix, minAccuracy, filterScaleFactorValue, maxResults);

        if (filteredMap.isEmpty()) {
            showErrorLabel(rb.getString("error.noMatchingResults"));
            return;
        }
        removeErrorLabel();
        parentController.logToUi(P2PKHSingleResultData.toStringPretty(P2PKHSingleResultDataHelper.toArray(filteredMap)), Color.LIGHTGRAY, LogTextTypeEnum.GENERAL);
        parentController.logToUi("Number of filtered items: " + filteredMap.keySet().size(), Color.GREEN, LogTextTypeEnum.GENERAL);

        showInfoLabel(rb.getString("info.filterSuccess") + filteredMap.keySet().size(), TextColorEnum.GREEN);
        loadFilteredResultsToUi(filteredMap, maxResults);
    }

    private boolean isValidPubPrefix(String startingPrefix) {
        if (startingPrefix == null) {
            return false;
        }
        if (startingPrefix.isEmpty()) {
            return true;
        }

        return HeatVisualizerConstants.PATTERN_SIMPLE_UP_TO_40.matcher(startingPrefix).matches();
    }

    private Map<String, Map<JsonResultTypeEnum, Map<JsonResultScaleFactorEnum, Pair<String, Integer>>>> filterHelper(
            Map<String, Map<JsonResultTypeEnum, Map<JsonResultScaleFactorEnum, Pair<String, Integer>>>> mapForResults, String startingPrefix, int minAccuracy, JsonResultScaleFactorEnum filterScaleFactorValue, int maxResults) {

        return mapForResults.keySet().stream()
                .map(key -> {
                    if (startingPrefix != null && !startingPrefix.isEmpty()) {
                        return key.startsWith(startingPrefix) ? key : null;
                    }
                    return key;
                })
                .filter(Objects::nonNull)
                .filter(key -> {
                    Map<JsonResultTypeEnum, Map<JsonResultScaleFactorEnum, Pair<String, Integer>>> data = mapForResults.get(key);
                    List<JsonResultTypeEnum> validResultTypeKeys = data.keySet().stream()
                            .filter(resultTypeKey -> {
                                Map<JsonResultScaleFactorEnum, Pair<String, Integer>> gg = data.get(resultTypeKey);
                                List<JsonResultScaleFactorEnum> validScaleFactorKeys = gg.keySet().stream()
                                        .filter(k -> {
                                            boolean scaleFactorMatches = (k == filterScaleFactorValue);
                                            boolean accuracyMatches = gg.get(k).getValue() >= minAccuracy;
                                            return scaleFactorMatches && accuracyMatches;
                                        }).collect(Collectors.toList());
                                return validScaleFactorKeys.size() != 0;
                            })
                            .collect(Collectors.toList());
                    return validResultTypeKeys.size() != 0;
                })
                .limit(maxResults)
                .collect(Collectors.toMap(key -> key, mapForResults::get, (v1,v2) -> v1, LinkedHashMap::new));
    }

    private void showInfoLabel(String message, TextColorEnum color) {
        advancedResultsLabelFilterMessage.getStyleClass().clear();
        advancedResultsLabelFilterMessage.getStyleClass().add(CssConstants.ERROR_INFO_MESSAGE_STYLE_CLASS);
        advancedResultsLabelFilterMessage.getStyleClass().add(color.getStyleClass());
        advancedResultsLabelFilterMessage.setText(message);
    }

    private void showErrorLabel(String error) {
        advancedResultsLabelFilterMessage.getStyleClass().clear();
        advancedResultsLabelFilterMessage.getStyleClass().add(CssConstants.ERROR_INFO_MESSAGE_STYLE_CLASS);
        advancedResultsLabelFilterMessage.getStyleClass().add(TextColorEnum.RED.getStyleClass());
        advancedResultsLabelFilterMessage.setText(error);
    }

    private void removeErrorLabel() {
        advancedResultsLabelFilterMessage.getStyleClass().clear();
        advancedResultsLabelFilterMessage.getStyleClass().add(CssConstants.ERROR_INFO_MESSAGE_STYLE_CLASS);
        advancedResultsLabelFilterMessage.setText(HeatVisualizerConstants.EMPTY_STRING);
    }

    private void showResultsForSelectedHash() {
        //when selected
        String selectedHash = advancedResultsComboBoxSelectPubFromResults.getSelectionModel().getSelectedItem();

        if (selectedHash == null) { //can be null when value list is removed from comboBox (event triggers)
            return;
        }

        JsonResultScaleFactorEnum selectedScaleFactor = getSelectedResultScaleFactorFromUi();

        Map<String, Map<JsonResultTypeEnum, Map<JsonResultScaleFactorEnum, Pair<String, Integer>>>> mapForResults = P2PKHSingleResultDataHelper.toDataMap(loadedResultTemplate);
        if (mapForResults == null || mapForResults.isEmpty()) {
            mapForResults = Arrays.stream(loadedResultTemplate)
                    .collect(Collectors.toMap(P2PKHSingleResultData::getHash, P2PKHSingleResultData::getResults, (v1,v2) -> v1, LinkedHashMap::new));
        }

        //show results:
        Map<JsonResultTypeEnum, Map<JsonResultScaleFactorEnum, Pair<String, Integer>>> resultsForCurrentHash = mapForResults.get(selectedHash);

        // for UHP
        Pair<String, Integer> pairForUHP = resultsForCurrentHash.get(JsonResultTypeEnum.UHP).get(selectedScaleFactor);
        advancedResultsTextFieldUHPKey.setText(pairForUHP.getKey());
        advancedResultsUHPAcc.setText(String.valueOf(pairForUHP.getValue()));

        // for UHN
        Pair<String, Integer> pairForUHN = resultsForCurrentHash.get(JsonResultTypeEnum.UHN).get(selectedScaleFactor);
        advancedResultsTextFieldUHNKey.setText(pairForUHN.getKey());
        advancedResultsUHNAcc.setText(String.valueOf(pairForUHN.getValue()));

        // for CHP
        Pair<String, Integer> pairForCHP = resultsForCurrentHash.get(JsonResultTypeEnum.CHP).get(selectedScaleFactor);
        advancedResultsTextFieldCHPKey.setText(pairForCHP.getKey());
        advancedResultsCHPAcc.setText(String.valueOf(pairForCHP.getValue()));

        // for CHN
        Pair<String, Integer> pairForCHN = resultsForCurrentHash.get(JsonResultTypeEnum.CHN).get(selectedScaleFactor);
        advancedResultsTextFieldCHNKey.setText(pairForCHN.getKey());
        advancedResultsCHNAcc.setText(String.valueOf(pairForCHN.getValue()));
    }

    @FXML
    private void doBrowseForSearchResultFilePath() {
        HeatVisualizerModalHelper.selectJsonResourceForOpen(rb.getString("label.selectLoadTarget"), advancedResultsTextFieldSelectAndLoadFromFile).ifPresent(absPath -> {
            advancedResultsTextFieldSelectAndLoadFromFile.setText(absPath);
            advancedResultsBtnDoLoadSearchResults.setDisable(false);
        });
    }

    @FXML
    private void doLoadSearchResults() {
        String pathToResultFile = advancedResultsTextFieldSelectAndLoadFromFile.getText();

        if (!pathToResultFile.endsWith(".json")) {
            showErrorLabel(rb.getString("error.unsupportedFormat"));
            return;
        }
        removeErrorLabel();
        P2PKHSingleResultData[] results = SerializedSearchResultsReader.deserializeExistingSearchResults(pathToResultFile);
        loadAdvancedSearchResultsToUi(pathToResultFile, P2PKHSingleResultDataHelper.toDataMap(results));

        advancedResultsBtnJustShowMeBest.setDisable(false);
        modifyAccessToHeatViewButtons(true);
    }

    private void modifyAccessToHeatViewButtons(boolean enable) {
        advancedResultsBtnShowHeatUHP.setDisable(!enable);
        advancedResultsBtnShowHeatUHN.setDisable(!enable);
        advancedResultsBtnShowHeatCHP.setDisable(!enable);
        advancedResultsBtnShowHeatCHN.setDisable(!enable);
    }

    /**
     * Loads selected data map to UI's 'Results' tab
     * @param pathToResultFile
     * @param mapForResults
     */
    void loadAdvancedSearchResultsToUi(String pathToResultFile, Map<String, Map<JsonResultTypeEnum, Map<JsonResultScaleFactorEnum, Pair<String, Integer>>>> mapForResults) {
        if (mapForResults == null || mapForResults.isEmpty()) {
            throw new IllegalStateException("Result map hasn't been loaded properly. Cannot display the results");
        }

        filtered = false;
        setLoadedResultTemplate(P2PKHSingleResultDataHelper.deepCopy(P2PKHSingleResultDataHelper.toArray(mapForResults)));

        //display all filtered hashes
        advancedResultsComboBoxSelectPubFromResults.getItems().clear();
        advancedResultsComboBoxSelectPubFromResults.getItems().addAll(mapForResults.keySet().stream().limit(FILTERED_RESULTS_UI_LIMIT).collect(Collectors.toList()));
        showInfoLabel(rb.getString("info.resultsLoadedWarnAboutLimit") + FILTERED_RESULTS_UI_LIMIT, TextColorEnum.GREEN);

        if (!advancedResultsTextFieldSelectAndLoadFromFile.getText().equals(pathToResultFile)) {
            advancedResultsTextFieldSelectAndLoadFromFile.setText(pathToResultFile);
        }

        //select first
        advancedResultsComboBoxSelectPubFromResults.getSelectionModel().select(0);
        advancedResultsBtnFilterResults.setDisable(false);
    }

    private String getSelectedHashFromUi() {
        return advancedResultsComboBoxSelectPubFromResults.getSelectionModel().getSelectedItem();
    }

    private JsonResultScaleFactorEnum getSelectedResultScaleFactorFromUi() {
        return advancedResultsComboResultsScaleFactor.getSelectionModel().getSelectedItem();
    }

    private int getAccuracyFromResultMap(String selectedHash, JsonResultTypeEnum type, JsonResultScaleFactorEnum scaleFactor) {
        return mapForResults.get(selectedHash).get(type).get(scaleFactor).getValue();
    }

    private String getPrivFromResultMap(String selectedHash, JsonResultTypeEnum type, JsonResultScaleFactorEnum scaleFactor) {
        return mapForResults.get(selectedHash).get(type).get(scaleFactor).getKey();
    }

    private void loadFilteredResultsToUi(Map<String, Map<JsonResultTypeEnum, Map<JsonResultScaleFactorEnum, Pair<String, Integer>>>> filteredMap, int maxResults) {
        filtered = true;
        //display all filtered hashes
        advancedResultsComboBoxSelectPubFromResults.getItems().clear();
        advancedResultsComboBoxSelectPubFromResults.getItems().addAll(filteredMap.keySet().stream().limit(maxResults).collect(Collectors.toList()));

        //select the first one
        advancedResultsComboBoxSelectPubFromResults.getSelectionModel().select(0);
    }

    void modifyAccessToFilterBtn(boolean enabled) {
        advancedResultsBtnFilterResults.setDisable(!enabled);
    }

    private JsonResultTypeEnum getSelectedResultTypeFromUi(ActionEvent actionEvent) {
        String sourceObj = actionEvent.getSource().toString();
        return JsonResultTypeEnum.valueOf(sourceObj.substring(sourceObj.indexOf(BTN_ID_PREFIX) + BTN_ID_PREFIX.length(), sourceObj.indexOf(", ")));
    }

    public final boolean isParentValid() {
        return parentController != null;
    }
}
