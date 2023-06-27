package com.bearlycattable.bait.bl.controllers.heatComparisonTab;

import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.advancedCommons.helpers.HeatVisualizerComponentHelper;
import com.bearlycattable.bait.bl.controllers.RootController;
import com.bearlycattable.bait.bl.helpers.DarkModeHelper;
import com.bearlycattable.bait.bl.helpers.HeatVisualizerFormatterFactory;
import com.bearlycattable.bait.commons.CssConstants;
import com.bearlycattable.bait.commons.HeatVisualizerConstants;
import com.bearlycattable.bait.commons.enums.HeatOverflowTypeEnum;
import com.bearlycattable.bait.commons.enums.NumberFormatTypeEnum;
import com.bearlycattable.bait.commons.enums.PrivKeyTargetTypeEnum;
import com.bearlycattable.bait.commons.enums.PubTypeEnum;
import com.bearlycattable.bait.commons.enums.QuickSearchComparisonType;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.TextColorEnum;
import com.bearlycattable.bait.commons.helpers.HeatVisualizerHelper;
import com.bearlycattable.bait.commons.wrappers.PrivHeatResultWrapper;
import com.bearlycattable.bait.commons.wrappers.PrivHeatResultWrapperDecimal;
import com.bearlycattable.bait.commons.wrappers.PrivHeatResultWrapperHex;
import com.bearlycattable.bait.commons.other.PubComparer;
import com.bearlycattable.bait.commons.other.PubComparisonResult;
import com.bearlycattable.bait.commons.wrappers.PubHeatResultWrapper;
import com.bearlycattable.bait.utility.BundleUtils;
import com.bearlycattable.bait.utility.LocaleUtils;
import com.bearlycattable.bait.commons.validators.PrivKeyValidator;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;

public class HeatComparisonTabController {

    private static final Logger LOG = Logger.getLogger(HeatComparisonTabController.class.getName());
    private final ResourceBundle rb = ResourceBundle.getBundle(BundleUtils.GLOBAL_BASE_NAME + "HeatComparisonTab", LocaleUtils.APP_LANGUAGE);
    private final HeatVisualizerHelper helper = new HeatVisualizerHelper();
    private final PubComparer pubComparer = new PubComparer();

    @Getter
    private final Map<Integer, Label> pubCompressedHeatPositiveAbsoluteIndexes = new HashMap<>();
    @Getter
    private final Map<Integer, Label> pubCompressedHeatNegativeAbsoluteIndexes = new HashMap<>();

    @Getter
    private final Map<Integer, Label> pubUncompressedHeatPositiveAbsoluteIndexes = new HashMap<>();
    @Getter
    private final Map<Integer, Label> pubUncompressedHeatNegativeAbsoluteIndexes = new HashMap<>();

    @Getter
    private final Map<Integer, Label> referenceForUncompressedAbsoluteIndexes = new HashMap<>();
    @Getter
    private final Map<Integer, Label> referenceForCompressedAbsoluteIndexes = new HashMap<>();

    @Getter
    private final Map<Integer, HBox> privHeatPositiveContainerMappings = new HashMap<>();
    @Getter
    private final Map<Integer, Label> privHeatPositiveAbsoluteIndexes = new HashMap<>();
    @Getter
    private final Map<Integer, Label> privHeatPositiveNumericLabels = new HashMap<>();

    @Getter
    private final Map<Integer, HBox> privHeatNegativeContainerMappings = new HashMap<>();
    @Getter
    private final Map<Integer, Label> privHeatNegativeAbsoluteIndexes = new HashMap<>();
    @Getter
    private final Map<Integer, Label> privHeatNegativeNumericLabels = new HashMap<>();

    private int currentPrivAccuracyResolution = 1;
    private volatile NumberFormatTypeEnum currentNumberFormatType;

    private RootController rootController;
    //current key and main reference key
    @FXML
    @Getter
    private TextField comparisonTextFieldCurrentKey;
    @Getter
    private TextField comparisonTextFieldReferenceKey; //dynamically created

    //reference key radio types
    @FXML
    private RadioButton comparisonRadioReferenceKeyTypePK;
    @FXML
    private RadioButton comparisonRadioReferenceKeyTypePKH;

    @FXML
    private VBox comparisonVBoxReferenceKeyParent;

    @FXML
    @Getter
    private Spinner<Integer> comparisonSpinnerResolutionPriv;

    //percent labels
    @FXML
    private Label labelPercentUncompressedPositive;
    @FXML
    private Label labelPercentUncompressedNegative;
    @FXML
    private Label labelPercentCompressedPositive;
    @FXML
    private Label labelPercentCompressedNegative;

    @FXML
    @Getter
    private ChoiceBox<NumberFormatTypeEnum> comparisonChoiceBoxNumberFormatType;
    @FXML
    @Getter
    private ComboBox<ScaleFactorEnum> comparisonComboBoxScaleFactor;
    //length labels
    @FXML
    @Getter
    private Label comparisonLabelLengthCurrentKey;

    @FXML
    @Getter
    private Button comparisonBtnShowPrivStats;
    //parent containers for priv heat
    @FXML
    @Getter
    private HBox containerPrivHeatPositive;
    @FXML
    @Getter
    private HBox containerPrivHeatNegative;
    @FXML
    @Getter
    private HBox privHeatPositiveNumericLabelContainer;
    @FXML
    @Getter
    private HBox privHeatNegativeNumericLabelContainer;
    //parent containers for pub heat
    @FXML
    @Getter
    private HBox containerPubHeatPositiveUncompressed;
    @FXML
    @Getter
    private HBox containerPubHeatPositiveCompressed;
    @FXML
    @Getter
    private HBox containerPubHeatNegativeUncompressed;
    @FXML
    @Getter
    private HBox containerPubHeatNegativeCompressed;
    @FXML
    private Label comparisonLabelResultForScaleFactor;
    @FXML
    private Label comparisonLabelPubErrorSuccessResult;

    @FXML
    @Getter
    private HBox containerReferenceUncompressed;
    @FXML
    @Getter
    private HBox containerReferenceCompressed;

    public void setRootController(RootController rootController) {
        this.rootController = rootController;
    }

    @FXML
    void initialize() {
        System.out.println("CREATING: HeatComparisonTabController......");

        comparisonRadioReferenceKeyTypePK.setOnAction(event -> {
            insertComponentForReferencePK();
            comparisonBtnShowPrivStats.setDisable(false);
            comparisonSpinnerResolutionPriv.setDisable(false);
            comparisonChoiceBoxNumberFormatType.setDisable(false);
        });

        comparisonRadioReferenceKeyTypePKH.setOnAction(event -> {
            insertComponentForReferencePKH();
            comparisonBtnShowPrivStats.setDisable(true);
            comparisonSpinnerResolutionPriv.setDisable(true);
            comparisonChoiceBoxNumberFormatType.setDisable(true);
        });

        //default option
        comparisonRadioReferenceKeyTypePK.fire();
    }

    private void insertComponentForReferencePK() {
        comparisonVBoxReferenceKeyParent.getChildren().clear();

        HBox parent = new HBox();
        parent.setAlignment(Pos.CENTER);

        parent.getChildren().add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(5, true));

        Label label = new Label(rb.getString("label.referenceKey"));
        label.setPrefWidth(110.0);
        parent.getChildren().add(label);
        parent.getChildren().add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(56, false));

        TextField textField = new TextField();
        textField.getStyleClass().add("fullPKInput");
        textField.setPrefWidth(694.0); //TODO: width should depend on current font metrics...
        textField.setTextFormatter(HeatVisualizerFormatterFactory.getDefaultPrivateKeyFormatter());
        comparisonTextFieldReferenceKey = textField;
        parent.getChildren().add(textField);

        parent.getChildren().add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(5, true));

        String lengthLabelId = "referenceKeyLength";
        HBox lengthLabelContainer = HeatVisualizerComponentHelper.createHBoxWithLengthLabel(lengthLabelId);
        lengthLabelContainer.getChildren().stream()
                .map(node -> Label.class.isAssignableFrom(node.getClass()) ? (Label)node : null)
                .filter(Objects::nonNull)
                .filter(lbl -> lengthLabelId.equals(lbl.getId()))
                .findAny()
                .ifPresent(lengthLabel -> textField.textProperty().addListener(event -> lengthLabel.setText(Integer.toString(textField.getLength()))));

        parent.getChildren().add(lengthLabelContainer);
        parent.getChildren().add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(5, true));

        Button btn = new Button(rb.getString("label.importFromConstruction"));
        btn.setTooltip(new Tooltip(rb.getString("tooltip.importFromConstruction")));
        btn.setOnAction(event -> {
            String key = rootController.getCurrentInput();
            if (!HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(key).matches()) {
                showErrorMessage(rb.getString("error.keyNotValidInConstructionTab"));
                return;
            }
            removeErrorMessageAndRedBorder(comparisonTextFieldReferenceKey);
            textField.setText(key);
        });
        parent.getChildren().add(btn);
        parent.getChildren().add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(56, false));

        if (rootController != null) {
            DarkModeHelper.toggleDarkModeForComponent(rootController.isDarkMode(), parent);
        }

        comparisonVBoxReferenceKeyParent.getChildren().add(parent);
    }

    public void setReferenceKey(String input, QuickSearchComparisonType type) {
        if (type == null) {
            return;
        }

        switch (type) {
            case COLLISION:
                comparisonRadioReferenceKeyTypePK.fire();
                if (!HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(input).matches()) {
                    showErrorMessage(rb.getString("error.invalidReferenceKey64"));
                    return;
                }
                break;
            case BLIND:
                comparisonRadioReferenceKeyTypePKH.fire();
                if (!HeatVisualizerConstants.PATTERN_SIMPLE_40.matcher(input).matches()) {
                    showErrorMessage(rb.getString("error.invalidReferenceKey40"));
                    return;
                }
                break;
            default:
                throw new IllegalArgumentException("Type not supported");
        }

        comparisonTextFieldReferenceKey.setText(input);
    }

    public void setCurrentKey(String input) {
        if (!HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(input).matches()) {
            showErrorMessage(rb.getString("error.invalidCurrentKey64"));
            return;
        }
        removeMessage();
        comparisonTextFieldCurrentKey.setText(input);
    }

    private void insertComponentForReferencePKH() {
        comparisonVBoxReferenceKeyParent.getChildren().clear();

        HBox parent = new HBox();
        parent.setAlignment(Pos.CENTER);

        parent.getChildren().add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(5, true));

        Label label = new Label(rb.getString("label.publicKey"));
        parent.getChildren().add(label);

        parent.getChildren().add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(80, false));

        TextField textField = new TextField();
        textField.setPrefWidth(440.0);
        textField.getStyleClass().add("fullPKHInput"); //TODO: width should depend on font metrics
        textField.setTextFormatter(HeatVisualizerFormatterFactory.getDefaultUnencodedPublicKeyFormatter());
        comparisonTextFieldReferenceKey = textField;
        parent.getChildren().add(textField);

        parent.getChildren().add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(5, true));

        String lengthLabelId = "referenceKeyLength";
        HBox lengthLabelContainer = HeatVisualizerComponentHelper.createHBoxWithLengthLabel(lengthLabelId);
        lengthLabelContainer.getChildren().stream()
                .map(node -> Label.class.isAssignableFrom(node.getClass()) ? (Label)node : null)
                .filter(Objects::nonNull)
                .filter(lbl -> lengthLabelId.equals(lbl.getId()))
                .findAny()
                .ifPresent(lengthLabel -> textField.textProperty().addListener(event -> lengthLabel.setText(Integer.toString(textField.getLength()))));

        parent.getChildren().add(lengthLabelContainer);
        parent.getChildren().add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(5, true));

        Button btn = new Button(rb.getString("label.importFromConverter"));
        btn.setTooltip(new Tooltip(rb.getString("tooltip.importFromConverter")));
        btn.setOnAction(event -> {
            String PKH = rootController.getUnencodedPubFromConverterTab();
            if (!HeatVisualizerConstants.PATTERN_SIMPLE_40.matcher(PKH).matches()) {
                showErrorMessage(rb.getString("error.pkhNotValidInConverterTab"));
                return;
            }
            removeMessage();
            textField.setText(PKH);
        });
        parent.getChildren().add(btn);

        parent.getChildren().add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(5, true));

        Button btn2 = new Button(rb.getString("label.exportToConverter"));
        btn2.setTooltip(new Tooltip(rb.getString("tooltip.exportToConverter")));
        btn2.setOnAction(event -> {
            String PKH = textField.getText();
            if (!HeatVisualizerConstants.PATTERN_SIMPLE_40.matcher(PKH).matches()) {
                showErrorMessage(rb.getString("error.invalidPkhCannotExport"));
                return;
            }
            removeMessage();
            rootController.setUnencodedPubInConverterTab(PKH);
        });
        parent.getChildren().add(btn2);
        parent.getChildren().add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(80, false));

        if (rootController != null) {
            DarkModeHelper.toggleDarkModeForComponent(rootController.isDarkMode(), parent);
        }

        comparisonVBoxReferenceKeyParent.getChildren().add(parent);
    }

    @FXML
    private void doCompare() {
        calculateOutputs();
    }

    public void calculateOutputs() {
        String currentPriv = comparisonTextFieldCurrentKey.getText();

        if (!isCurrentKeyValid()) {
            removeAllStats();
            return;
        }
        removeErrorMessageAndRedBorder(comparisonTextFieldCurrentKey);

        if (!isReferenceKeyValid()) {
            removeAllStats();
            return;
        }
        removeErrorMessageAndRedBorder(comparisonTextFieldReferenceKey);

        QuickSearchComparisonType type = getSelectedReferenceKeyTypeFromUi();
        insertReferencePKHsToUi(); //by design, this must be done before any heat comparison

        String pubKeyHashUncompressed = helper.getPubKeyHashUncompressed(currentPriv, false);
        String pubKeyHashCompressed = helper.getPubKeyHashCompressed(currentPriv, false);

        insertCalculatedHashesToPubHeatMaps(pubKeyHashUncompressed, pubKeyHashCompressed);
        insertColorsToPubHeatMaps(pubKeyHashUncompressed, pubKeyHashCompressed);

        ScaleFactorEnum scaleFactor = getSelectedScaleFactorFromUi();
        showCurrentScaleFactorMessage(rb.getString("info.currentResultsScaleFactor") + scaleFactor.getScaleFactorAsString(), TextColorEnum.GREEN);

        //compare and set pub accuracy labels
        compareWithReferenceKey(pubKeyHashUncompressed, PubTypeEnum.UNCOMPRESSED, scaleFactor)
                .ifPresent(heatResult -> insertPubSimilarityPercentLabels(heatResult, PubTypeEnum.UNCOMPRESSED));
        compareWithReferenceKey(pubKeyHashCompressed, PubTypeEnum.COMPRESSED, scaleFactor)
                .ifPresent(heatResult -> insertPubSimilarityPercentLabels(heatResult, PubTypeEnum.COMPRESSED));

        if (QuickSearchComparisonType.BLIND == type) {
            removePrivStats();
            return;
        }

        modifyAccessForShowPrivStatsButton(false);
        showPrivStats();
    }

    private void removeAllStats() {
        removePubStats();
        removeCurrentScaleFactorMessage();
        removePrivStats();
        removePercentLabelData();
    }

    private void removePercentLabelData() {
        labelPercentUncompressedPositive.setText("");
        labelPercentUncompressedNegative.setText("");
        labelPercentCompressedPositive.setText("");
        labelPercentCompressedNegative.setText("");
    }

    private void removePubStats() {
        pubUncompressedHeatPositiveAbsoluteIndexes.keySet().forEach(key -> {
            pubUncompressedHeatPositiveAbsoluteIndexes.get(key).getStyleClass().clear();
            pubUncompressedHeatPositiveAbsoluteIndexes.get(key).setText("");
        });

        pubUncompressedHeatNegativeAbsoluteIndexes.keySet().forEach(key -> {
            pubUncompressedHeatNegativeAbsoluteIndexes.get(key).getStyleClass().clear();
            pubUncompressedHeatNegativeAbsoluteIndexes.get(key).setText("");
        });

        pubCompressedHeatPositiveAbsoluteIndexes.keySet().forEach(key -> {
            pubCompressedHeatPositiveAbsoluteIndexes.get(key).getStyleClass().clear();
            pubCompressedHeatPositiveAbsoluteIndexes.get(key).setText("");
        });

        pubCompressedHeatNegativeAbsoluteIndexes.keySet().forEach(key -> {
            pubCompressedHeatNegativeAbsoluteIndexes.get(key).getStyleClass().clear();
            pubCompressedHeatNegativeAbsoluteIndexes.get(key).setText("");
        });

        referenceForUncompressedAbsoluteIndexes.keySet().forEach(key -> {
            referenceForUncompressedAbsoluteIndexes.get(key).setText("");
        });

        referenceForCompressedAbsoluteIndexes.keySet().forEach(key -> {
            referenceForCompressedAbsoluteIndexes.get(key).setText("");
        });
    }

    private void removePrivStats() {
        for (int i = 0; i < 64; i++) {
            privHeatPositiveAbsoluteIndexes.get(i).setText(HeatVisualizerConstants.EMPTY_STRING);
            privHeatNegativeAbsoluteIndexes.get(i).setText(HeatVisualizerConstants.EMPTY_STRING);
        }

        for (int i = 1; i <= 8; i++) {
            privHeatPositiveNumericLabels.get(i).setText(HeatVisualizerConstants.EMPTY_STRING);
            privHeatNegativeNumericLabels.get(i).setText(HeatVisualizerConstants.EMPTY_STRING);
            privHeatPositiveContainerMappings.get(i).getStyleClass().clear();
            privHeatNegativeContainerMappings.get(i).getStyleClass().clear();
        }
    }

    private boolean isCurrentKeyValid() {
        if (!HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(comparisonTextFieldCurrentKey.getText()).matches()) {
            addErrorMessageAndRedBorder(rb.getString("error.invalidCurrentKey64"), comparisonTextFieldCurrentKey);
            return false;
        }

        if (!PrivKeyValidator.isValidPK(comparisonTextFieldCurrentKey.getText())) {
            addErrorMessageAndRedBorder(rb.getString("error.invalidCurrentKeyWithReason") + buildReasonForInvalidKey(comparisonTextFieldCurrentKey.getText()), comparisonTextFieldCurrentKey);
            return false;
        }

        return true;
    }

    private boolean isReferenceKeyValid() {
        QuickSearchComparisonType type = getSelectedReferenceKeyTypeFromUi();

        if (type == null) {
            return false;
        }

        switch (type) {
            case COLLISION:
                return isReferenceKeyValidAsPriv();
            case BLIND:
                if (!isReferenceKeyValidAsPKH()) {
                    addErrorMessageAndRedBorder(rb.getString("error.invalidReferenceKey40"), comparisonTextFieldReferenceKey);
                    return false;
                }
                return true;
            default:
                throw new IllegalStateException("Received wrong type at #isReferenceKeyValid [type=" + getSelectedReferenceKeyTypeFromUi() + "]");
        }
    }

    private boolean isReferenceKeyValidAsPriv() {
        if (!HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(comparisonTextFieldReferenceKey.getText()).matches()) {
            addErrorMessageAndRedBorder(rb.getString("error.invalidReferenceKey64"), comparisonTextFieldReferenceKey);
            return false;
        }

        if (!PrivKeyValidator.isValidPK(comparisonTextFieldReferenceKey.getText())) {
            addErrorMessageAndRedBorder(rb.getString("error.invalidReferenceKeyWithReason") + buildReasonForInvalidKey(comparisonTextFieldReferenceKey.getText()), comparisonTextFieldReferenceKey);
            return false;
        }

        return true;
    }

    private String buildReasonForInvalidKey(@NonNull String invalidKey) {
        if (invalidKey.startsWith("0000")) {
            return rb.getString("error.privLowerThanMin");
        }

        return invalidKey.startsWith("FFFF") ? rb.getString("error.privHigherThanMax") : rb.getString("error.unknown");
    }

    private boolean isReferenceKeyValidAsPKH() {
        return HeatVisualizerConstants.PATTERN_SIMPLE_40.matcher(comparisonTextFieldReferenceKey.getText()).matches();
    }

    private QuickSearchComparisonType getSelectedReferenceKeyTypeFromUi() {
        if (comparisonRadioReferenceKeyTypePK.isSelected()) {
            return QuickSearchComparisonType.COLLISION;
        }
        if (comparisonRadioReferenceKeyTypePKH.isSelected()) {
            return QuickSearchComparisonType.BLIND;
        }

        throw new IllegalStateException("No reference key type is selected");
    }

    private void addErrorMessageAndRedBorder(String message, Control component) {
        showErrorMessage(message);
        addRedBorder(component);
    }

    private void removeErrorMessageAndRedBorder(Control component) {
        removeMessage();
        removeRedBorder(component);
    }

    private void removeMessage() {
        comparisonLabelPubErrorSuccessResult.setText(HeatVisualizerConstants.EMPTY_STRING);
    }

    void showInfoMessage(String message) {
        comparisonLabelPubErrorSuccessResult.getStyleClass().clear();
        comparisonLabelPubErrorSuccessResult.setText(message);
    }

    void showCurrentScaleFactorMessage(String message, TextColorEnum color) {
        comparisonLabelResultForScaleFactor.getStyleClass().clear();
        comparisonLabelResultForScaleFactor.getStyleClass().add(CssConstants.ERROR_INFO_MESSAGE_STYLE_CLASS);
        comparisonLabelResultForScaleFactor.getStyleClass().add(color.getStyleClass());
        comparisonLabelResultForScaleFactor.setText(message);
    }

    void removeCurrentScaleFactorMessage() {
        comparisonLabelResultForScaleFactor.getStyleClass().clear();
        comparisonLabelResultForScaleFactor.getStyleClass().add(CssConstants.ERROR_INFO_MESSAGE_STYLE_CLASS);
        comparisonLabelResultForScaleFactor.setText(HeatVisualizerConstants.EMPTY_STRING);
    }

    void showErrorMessage(String message) {
        if (!comparisonLabelPubErrorSuccessResult.getStyleClass().contains(TextColorEnum.RED.getStyleClass())) {
            comparisonLabelPubErrorSuccessResult.getStyleClass().add(TextColorEnum.RED.getStyleClass());
        }
        comparisonLabelPubErrorSuccessResult.setText(message);
    }

    private void addRedBorder(Control component) {
        if (!component.getStyleClass().contains(CssConstants.BORDER_RED)) {
            component.getStyleClass().add(CssConstants.BORDER_RED);
        }
    }

    private void removeRedBorder(Control component) {
        component.getStyleClass().remove(CssConstants.BORDER_RED);
    }

    private void insertReferencePKHsToUi() {
        if (!isReferenceKeyValid()) {
            return;
        }

        String referenceKey = comparisonTextFieldReferenceKey.getText();
        QuickSearchComparisonType type = getSelectedReferenceKeyTypeFromUi();

        setReferencePKHForUncompressed(QuickSearchComparisonType.BLIND == type ? referenceKey : helper.getPubKeyHashUncompressed(referenceKey, false));
        setReferencePKHForCompressed(QuickSearchComparisonType.BLIND == type ? referenceKey : helper.getPubKeyHashCompressed(referenceKey, false));
    }

    private void setReferencePKHForCompressed(String CPKH) {
        if (!HeatVisualizerConstants.PATTERN_SIMPLE_40.matcher(CPKH).matches()) {
            showErrorMessage(rb.getString("error.invalidReferenceCPKH"));
            return;
        }
        insertCalculatedHashesToReferencePKHForComp(CPKH);
    }

    private void setReferencePKHForUncompressed(String UPKH) {
        if (!HeatVisualizerConstants.PATTERN_SIMPLE_40.matcher(UPKH).matches()) {
            showErrorMessage(rb.getString("error.invalidReferenceUPKH"));
            return;
        }
        insertCalculatedHashesToReferencePKHForUncomp(UPKH);
    }

    private ScaleFactorEnum getSelectedScaleFactorFromUi() {
        return comparisonComboBoxScaleFactor.getSelectionModel().getSelectedItem();
    }

    void insertPubSimilarityPercentLabels(PubComparisonResult heatResult, PubTypeEnum pubType) {
        if (heatResult == null || pubType == null) {
            return;
        }

        int mapIndexForPositive = getResultForHeatType(heatResult, HeatOverflowTypeEnum.HEAT_POSITIVE);
        int mapIndexForNegative = getResultForHeatType(heatResult, HeatOverflowTypeEnum.HEAT_NEGATIVE);

        switch (pubType) {
            case UNCOMPRESSED:
                labelPercentUncompressedPositive.setText(rootController.getSimilarityMappings().get(mapIndexForPositive).setScale(0, RoundingMode.HALF_UP).toString());
                labelPercentUncompressedNegative.setText(rootController.getSimilarityMappings().get(mapIndexForNegative).setScale(0, RoundingMode.HALF_UP).toString());
                break;
            case COMPRESSED:
                labelPercentCompressedPositive.setText(rootController.getSimilarityMappings().get(mapIndexForPositive).setScale(0, RoundingMode.HALF_UP).toString());
                labelPercentCompressedNegative.setText(rootController.getSimilarityMappings().get(mapIndexForNegative).setScale(0, RoundingMode.HALF_UP).toString());
                break;
            default:
                throw new IllegalArgumentException("This pub key type is not supported [type=" + pubType + "]");
        }
    }

    private int getResultForHeatType(PubComparisonResult currentResult, HeatOverflowTypeEnum heatType) {
        return rootController.getNormalizedMapIndexFromComparisonResult(HeatOverflowTypeEnum.HEAT_POSITIVE == heatType ? currentResult.getPositive() : currentResult.getNegative(), currentResult.getForScaleFactor());
    }

    private void insertColorsToPubHeatMaps(String pubUncompressed, String pubCompressed) {
        insertColorsToPubHeatMapsForUncomp(pubUncompressed);
        insertColorsToPubHeatMapsForComp(pubCompressed);
    }

    private void insertCalculatedHashesToPubHeatMaps(String pubUncompressed, String pubCompressed) {
        insertCalculatedHashesToPubHeatMapsForUncomp(pubUncompressed);
        insertCalculatedHashesToPubHeatMapsForComp(pubCompressed);
    }

    void insertCalculatedHashesToPubHeatMapsForUncomp(String pubUncompressed) {
        for (int i = 0; i < pubUncompressed.length(); i++) {
            String current = String.valueOf(pubUncompressed.charAt(i));
            pubUncompressedHeatPositiveAbsoluteIndexes.get(i).setText(current);
            pubUncompressedHeatNegativeAbsoluteIndexes.get(i).setText(current);
        }
    }

    void insertCalculatedHashesToReferencePKHForUncomp(String pubUncompressed) {
        for (int i = 0; i < pubUncompressed.length(); i++) {
            String current = String.valueOf(pubUncompressed.charAt(i));
            referenceForUncompressedAbsoluteIndexes.get(i).setText(current);
        }
    }

    void insertCalculatedHashesToReferencePKHForComp(String pubCompressed) {
        for (int i = 0; i < pubCompressed.length(); i++) {
            String current = String.valueOf(pubCompressed.charAt(i));
            referenceForCompressedAbsoluteIndexes.get(i).setText(current);
        }
    }

    void insertCalculatedHashesToPubHeatMapsForComp(String pubCompressed) {
        for (int i = 0; i < pubCompressed.length(); i++) {
            String current = String.valueOf(pubCompressed.charAt(i));
            pubCompressedHeatPositiveAbsoluteIndexes.get(i).setText(current);
            pubCompressedHeatNegativeAbsoluteIndexes.get(i).setText(current);
        }
    }

    public void insertSearchResultsToUiForUncompressed(PubComparisonResult result) {
        String pubKeyHashUncompressed = helper.getPubKeyHashUncompressed(result.getForPriv(), false);

        //add highest items to pub heat map (and translate accuracy)
        insertCalculatedHashesToPubHeatMapsForUncomp(pubKeyHashUncompressed);
        insertColorsToPubHeatMapsForUncomp(pubKeyHashUncompressed);

        //set pub accuracy labels
        insertPubSimilarityPercentLabels(result, PubTypeEnum.UNCOMPRESSED);
    }

    public void insertSearchResultsToUiForCompressed(PubComparisonResult result) {
        String pubKeyHashCompressed = helper.getPubKeyHashCompressed(result.getForPriv(), false);

        //add highest items to pub heat map (and translate accuracy)
        insertCalculatedHashesToPubHeatMapsForComp(pubKeyHashCompressed);
        insertColorsToPubHeatMapsForComp(pubKeyHashCompressed);

        //set pub accuracy labels
        insertPubSimilarityPercentLabels(result, PubTypeEnum.COMPRESSED);
    }

    void insertColorsToPubHeatMapsForUncomp(String pubUncompressed) {
        calculateAndInsertPubColors(pubUncompressed, pubUncompressedHeatPositiveAbsoluteIndexes, pubUncompressedHeatNegativeAbsoluteIndexes, PubTypeEnum.UNCOMPRESSED);
    }

    void insertColorsToPubHeatMapsForComp(String pubCompressed) {
        calculateAndInsertPubColors(pubCompressed, pubCompressedHeatPositiveAbsoluteIndexes, pubCompressedHeatNegativeAbsoluteIndexes, PubTypeEnum.COMPRESSED);
    }

    private void calculateAndInsertPubColors(String currentPKH, Map<Integer, Label> pubPositiveHeatMappings, Map<Integer, Label> pubNegativeHeatMappings, PubTypeEnum pubType) {
        String referencePKH;

        if (pubType == null) {
            return;
        }

        switch (pubType) {
            case UNCOMPRESSED:
                referencePKH = getReferencePKHForUncompressed();
                break;
            case COMPRESSED:
                referencePKH = getReferencePKHForCompressed();
                break;
            default:
                throw new IllegalArgumentException("Type is not supported [type=" + pubType + "]");
        }

        int length = referencePKH.length();

        for (int i = 0; i < length; i++) {
            int locked = Integer.parseInt(String.valueOf(referencePKH.charAt(i)), 16);
            int current = Integer.parseInt(String.valueOf(currentPKH.charAt(i)), 16);
            int overflow_reference = HeatVisualizerConstants.OVERFLOW_REFERENCE_1_HEX;

            PubHeatResultWrapper wrapper = helper.calculatePubHeatResults(locked, current, overflow_reference);
            insertPubColorStylesToUi(i, pubPositiveHeatMappings, pubNegativeHeatMappings, wrapper);
        }
    }

    private String getReferencePKHForUncompressed() {
        StringBuilder sb = new StringBuilder(40);
        for (int i = 0; i < 40; i++) {
            sb.append(referenceForUncompressedAbsoluteIndexes.get(i).getText());
        }

        return sb.toString();
    }

    private String getReferencePKHForCompressed() {
        StringBuilder sb = new StringBuilder(40);
        for (int i = 0; i < 40; i++) {
            sb.append(referenceForCompressedAbsoluteIndexes.get(i).getText());
        }

        return sb.toString();
    }

    void insertPubColorStylesToUi(int wordNumber, Map<Integer, Label> pubPositiveHeatMappings, Map<Integer, Label> pubNegativeHeatMappings, PubHeatResultWrapper resultWrapper) {
        pubPositiveHeatMappings.get(wordNumber).getStyleClass().clear();
        pubPositiveHeatMappings.get(wordNumber).getStyleClass().add(rootController.getColorMappings().get(resultWrapper.getHeatPositive()));

        pubNegativeHeatMappings.get(wordNumber).getStyleClass().clear();
        pubNegativeHeatMappings.get(wordNumber).getStyleClass().add(rootController.getColorMappings().get(resultWrapper.getHeatNegative()));
    }

    Optional<PubComparisonResult> compareWithReferenceKey(String currentPHK, PubTypeEnum type, ScaleFactorEnum scaleFactor) {
        if (type == null) {
            return Optional.empty();
        }

        switch (type) {
            case UNCOMPRESSED:
                return pubComparer.comparePubKeyHashes(type, getReferencePKHForUncompressed(), currentPHK, scaleFactor);
            case COMPRESSED:
                return pubComparer.comparePubKeyHashes(type, getReferencePKHForCompressed(), currentPHK, scaleFactor);
            default:
                throw new IllegalArgumentException("Requested type is not supported at #compareWithLocked [type=" + type + "]");
        }
    }

    @FXML
    private void doShowPrivStats(ActionEvent actionEvent) {
        showPrivStats();
    }

    void showPrivStats() {
        if (!isCurrentKeyValid()) {
            return;
        }
        removeErrorMessageAndRedBorder(comparisonTextFieldCurrentKey);

        if (!isReferenceKeyValid()) {
            return;
        }
        removeErrorMessageAndRedBorder(comparisonTextFieldReferenceKey);

        for (int i = 1; i <= 8; i++) {
            String wordInCurrentKeyField = readHexWordFromUi(i, PrivKeyTargetTypeEnum.CURRENT_KEY);
            insertPrivWordToPrivHeatMaps(i, wordInCurrentKeyField);

            int justI = i; //lambda needs final
            getPrivHeat(i, wordInCurrentKeyField, currentNumberFormatType).ifPresent(result -> {
                insertColorsToPrivHeatMaps(result, justI);
                insertPrivNumericDifferenceLabel(justI, result, currentNumberFormatType);
            });
        }

        // buildLastPrivFromUserDataFields();
    }

    // private String buildLastPrivFromUserDataFields() {
    //     return Stream.iterate(1, i -> ++i)
    //             .limit(8)
    //             .map(index -> (String) privHeatPositiveContainerMappings.get(index).getUserData())
    //             .filter(Objects::nonNull)
    //             .collect(Collectors.joining(HeatVisualizerConstants.EMPTY_STRING));
    // }

    @FXML
    private void doChangeHeatResultFormat(ActionEvent actionEvent) {
        String currentPriv = comparisonTextFieldCurrentKey.getText();
        if (!HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(currentPriv).matches()) {
            currentNumberFormatType = comparisonChoiceBoxNumberFormatType.getSelectionModel().getSelectedItem();
            return;
        }

        NumberFormatTypeEnum selectedNumberFormat = helper.getNumberFormatFromActionEvent(actionEvent);

        if (selectedNumberFormat == null || currentNumberFormatType == selectedNumberFormat) {
            return;
        }

        for (int i = 1; i <= 8; i++) {
            insertPrivNumericDifferenceLabel(i, readAndConvertPrivHeatResultsFromUi(i, currentNumberFormatType), selectedNumberFormat);
        }

        currentNumberFormatType = selectedNumberFormat;
    }

    public final void changePrivHeatResolution() {
        if (comparisonRadioReferenceKeyTypePKH.isSelected()) {
           showErrorMessage("No reference priv. Cannot calculate priv heat");
           return;
        }

        if (!isCurrentKeyValid()) {
            return;
        }

        if (QuickSearchComparisonType.BLIND == getSelectedReferenceKeyTypeFromUi()) {
            addErrorMessageAndRedBorder(rb.getString("error.cannotCompareWithoutReference"), comparisonRadioReferenceKeyTypePKH);
            return;
        }

        if (!isReferenceKeyValid()) {
            return;
        }

        for (int i = 1; i <= 8; i++) {
            String wordInCurrentKeyField = readHexWordFromUi(i, PrivKeyTargetTypeEnum.CURRENT_KEY);

            int justI = i; //lambda needs final
            getPrivHeat(i, wordInCurrentKeyField, currentNumberFormatType)
                    .ifPresent(result -> insertColorsToPrivHeatMaps(result, justI));
        }
    }

    private Optional<PrivHeatResultWrapper> getPrivHeat(int wordNumber, String word, @NonNull NumberFormatTypeEnum type) {
        String wordInReferenceKeyField = readHexWordFromUi(wordNumber, PrivKeyTargetTypeEnum.REFERENCE_KEY);
        Long reference = Long.parseLong(wordInReferenceKeyField, 16);
        Long current = Long.parseLong(word, 16);
        Long overflow_reference = HeatVisualizerConstants.OVERFLOW_REFERENCE_8_HEX;

        return helper.calculatePrivHeatResults(reference, current, overflow_reference, type);
    }

    private String readHexWordFromUi(int wordNumber, @NonNull PrivKeyTargetTypeEnum type) {
        if (!isValidWordNumber(wordNumber)) {
            throw new IllegalArgumentException("Word number is not valid at #readAsHexWord");
        }

        switch (type) {
            case CURRENT_KEY:
                return comparisonTextFieldCurrentKey.getText().substring((wordNumber - 1) * 8, ((wordNumber - 1) * 8) + 8);
            case REFERENCE_KEY:
                return comparisonTextFieldReferenceKey.getText().substring((wordNumber - 1) * 8, ((wordNumber - 1) * 8) + 8);
            default:
                throw new IllegalArgumentException("Type is not valid at #read8HexWord [type=" + type + "]");
        }
    }

    private void insertPrivWordToPrivHeatMaps(int wordNumber, String word) {
        if (!isValidWordNumber(wordNumber)) {
            return;
        }
        if (word == null || word.length() != 8) {
            return;
        }

        //TODO: saving of data will only be needed if 'goBackToLastComparison' button is implemented someday
        privHeatPositiveContainerMappings.get(wordNumber).setUserData(word);
        privHeatNegativeContainerMappings.get(wordNumber).setUserData(word);

        //calculate absolute index range, since we already have them saved
        int startIndex = (wordNumber - 1) * 8;

        for (int i = 0; i < 8; i++, startIndex++) {
            String ch = String.valueOf(word.charAt(i));
            privHeatPositiveAbsoluteIndexes.get(startIndex).setText(ch);
            privHeatNegativeAbsoluteIndexes.get(startIndex).setText(ch);
        }
    }

    private void insertPrivNumericDifferenceLabel(int index, PrivHeatResultWrapper heatWrapper, @NonNull NumberFormatTypeEnum numberFormatType) {
        String resultPositive = convertToStringResult(numberFormatType, heatWrapper.getHeatPositive());
        String resultNegative = convertToStringResult(numberFormatType, heatWrapper.getHeatNegative());

        privHeatPositiveNumericLabels.get(index).setText(resultPositive);
        privHeatNegativeNumericLabels.get(index).setText(resultNegative);
    }

    private String convertToStringResult(@NonNull NumberFormatTypeEnum targetType, long heatResult) {
        switch (targetType) {
            case DECIMAL:
                return String.valueOf(heatResult).toUpperCase();
            case HEX:
                return helper.padTo8(Long.toHexString(heatResult), true);
            default:
                throw new IllegalArgumentException("Number format type not supported [type=" + targetType + "]");
        }
    }

    @FXML
    private void doImportPriv() {
        String currentInput = rootController.getCurrentInput();
        if (!HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(currentInput).matches()) {
            showErrorMessage(rb.getString("error.keyNotValidInConstructionTab"));
            return;
        }
        removeErrorMessageAndRedBorder(comparisonTextFieldCurrentKey);
        comparisonTextFieldCurrentKey.setText(currentInput);
    }

    private void insertColorsToPrivHeatMaps(PrivHeatResultWrapper heatResults, int wordNumber) {
        String hexResultPositive = helper.getHeatResultFromWrapperData(heatResults.getHeatPositive());
        String hexResultNegative = helper.getHeatResultFromWrapperData(heatResults.getHeatNegative());

        int mappedColorIndexPositive = helper.determineColorIndex(hexResultPositive, currentPrivAccuracyResolution);
        int mappedColorIndexNegative = helper.determineColorIndex(hexResultNegative, currentPrivAccuracyResolution);

        insertPrivColorStylesWithResolutionToUi(wordNumber, mappedColorIndexPositive, mappedColorIndexNegative);
    }

    private void insertPrivColorStylesWithResolutionToUi(int wordNumber, int colorIndexPositive, int colorIndexNegative) {
        privHeatPositiveContainerMappings.get(wordNumber).getStyleClass().clear();
        privHeatPositiveContainerMappings.get(wordNumber).getStyleClass().add(rootController.getColorMappings().get(colorIndexPositive));

        privHeatNegativeContainerMappings.get(wordNumber).getStyleClass().clear();
        privHeatNegativeContainerMappings.get(wordNumber).getStyleClass().add(rootController.getColorMappings().get(colorIndexNegative));
    }

    private void clearColorsFromPrivHeatMaps() {
        Stream.iterate(1, i -> ++i)
                .limit(8)
                .forEach(i -> {
                    privHeatPositiveContainerMappings.get(i).getStyleClass().clear();
                    privHeatNegativeContainerMappings.get(i).getStyleClass().clear();
                });
    }

    private PrivHeatResultWrapper readAndConvertPrivHeatResultsFromUi(int wordNumber, NumberFormatTypeEnum currentNumberFormatType) {
        switch (currentNumberFormatType) {
            case DECIMAL: //means previous was HEX
                return PrivHeatResultWrapperDecimal.builder()
                        .heatPositive(convertToDecimalResult(wordNumber, privHeatPositiveNumericLabels))
                        .heatNegative(convertToDecimalResult(wordNumber, privHeatNegativeNumericLabels))
                        .build();
            case HEX: //means previous was DECIMAL
                return PrivHeatResultWrapperHex.builder()
                        .heatPositive(convertToHexResult(wordNumber, privHeatPositiveNumericLabels))
                        .heatNegative(convertToHexResult(wordNumber, privHeatNegativeNumericLabels))
                        .build();
            default:
                throw new IllegalStateException("This number format type is not supported at #buildHeatResultsFromUi [type=" + currentNumberFormatType + "]");
        }
    }

    private long convertToDecimalResult(int wordNumber, Map<Integer, Label> privHeatLabelsMap) {
        return Long.parseLong(privHeatLabelsMap.get(wordNumber).getText());
    }

    private long convertToHexResult(int wordNumber, Map<Integer, Label> privHeatLabelsMap) {
        return Long.parseLong(privHeatLabelsMap.get(wordNumber).getText(), 16);
    }

    boolean isValidWordNumber(int number) {
        return number > 0 && number < 9;
    }

    public synchronized void setCurrentPrivAccuracyResolution(int resolution) {
        this.currentPrivAccuracyResolution = resolution;
    }

    public void modifyPrivHeatNumberFormatChoiceBoxAccess(boolean disabled) {
        comparisonChoiceBoxNumberFormatType.setDisable(disabled);
    }

    public void modifyPrivAccuracyResolutionSpinnerAccess(boolean disabled) {
        comparisonSpinnerResolutionPriv.setDisable(disabled);
    }

    public void modifyAccessForShowPrivStatsButton(boolean disabled) {
        comparisonBtnShowPrivStats.setDisable(disabled);
    }

    public void setScaleFactor(ScaleFactorEnum scaleFactor) {
        comparisonComboBoxScaleFactor.getSelectionModel().select(scaleFactor);
    }

    public void setCurrentNumberFormatType(NumberFormatTypeEnum numberFormatTypeEnum) {
        this.currentNumberFormatType = numberFormatTypeEnum;
    }

    public final boolean isParentValid() {
        return rootController != null;
    }
}
