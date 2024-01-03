package com.bearlycattable.bait.bl.controllers.heatComparisonTab;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.advancedCommons.helpers.DarkModeHelper;
import com.bearlycattable.bait.advancedCommons.helpers.BaitComponentHelper;
import com.bearlycattable.bait.bl.contexts.HeatComparisonContext;
import com.bearlycattable.bait.bl.controllers.HeatComparisonTabAccessProxy;
import com.bearlycattable.bait.bl.helpers.BaitFormatterFactory;
import com.bearlycattable.bait.commons.Config;
import com.bearlycattable.bait.commons.CssConstants;
import com.bearlycattable.bait.commons.BaitConstants;
import com.bearlycattable.bait.commons.enums.HeatOverflowTypeEnum;
import com.bearlycattable.bait.commons.enums.NumberFormatTypeEnum;
import com.bearlycattable.bait.commons.enums.PrivKeyTargetTypeEnum;
import com.bearlycattable.bait.commons.enums.PubTypeEnum;
import com.bearlycattable.bait.commons.enums.QuickSearchComparisonType;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;
import com.bearlycattable.bait.commons.enums.TextColorEnum;
import com.bearlycattable.bait.commons.helpers.BaitHelper;
import com.bearlycattable.bait.commons.pubKeyComparison.PubComparerS;
import com.bearlycattable.bait.commons.pubKeyComparison.PubComparisonResultS;
import com.bearlycattable.bait.commons.validators.PrivKeyValidator;
import com.bearlycattable.bait.commons.wrappers.PrivHeatResultWrapper;
import com.bearlycattable.bait.commons.wrappers.PrivHeatResultWrapperDecimal;
import com.bearlycattable.bait.commons.wrappers.PrivHeatResultWrapperHex;
import com.bearlycattable.bait.commons.wrappers.PubHeatResultWrapper;
import com.bearlycattable.bait.utility.BaitUtils;
import com.bearlycattable.bait.utility.BundleUtils;
import com.bearlycattable.bait.utility.LocaleUtils;

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
    private final BaitHelper helper = new BaitHelper();
    private final PubComparerS pubComparer = new PubComparerS();
    private final Map<Integer, BigDecimal> similarityMappings = Collections.unmodifiableMap(BaitUtils.buildSimilarityMappings());
    private final Map<Integer, String> colorMappings = Collections.unmodifiableMap(BaitUtils.buildColorMappings());

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

    private HeatComparisonTabAccessProxy heatComparisonTabAccessProxy;
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

    public void setHeatComparisonTabAccessProxy(HeatComparisonTabAccessProxy proxy) {
        this.heatComparisonTabAccessProxy = proxy;
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

        parent.getChildren().add(BaitComponentHelper.createEmptyHBoxSpacer(5, true));

        Label label = new Label(rb.getString("label.referenceKey"));
        label.setPrefWidth(110.0);
        parent.getChildren().add(label);
        parent.getChildren().add(BaitComponentHelper.createEmptyHBoxSpacer(56, false));

        TextField textField = new TextField();
        textField.getStyleClass().add("fullPKInput");
        textField.setPrefWidth(694.0); //TODO: width should depend on current font metrics...
        textField.setTextFormatter(BaitFormatterFactory.getDefaultPrivateKeyFormatter());
        comparisonTextFieldReferenceKey = textField;
        parent.getChildren().add(textField);

        parent.getChildren().add(BaitComponentHelper.createEmptyHBoxSpacer(5, true));

        String lengthLabelId = "referenceKeyLength";
        HBox lengthLabelContainer = BaitComponentHelper.createHBoxWithLengthLabel(lengthLabelId);
        lengthLabelContainer.getChildren().stream()
                .map(node -> Label.class.isAssignableFrom(node.getClass()) ? (Label)node : null)
                .filter(Objects::nonNull)
                .filter(lbl -> lengthLabelId.equals(lbl.getId()))
                .findAny()
                .ifPresent(lengthLabel -> textField.textProperty().addListener(event -> lengthLabel.setText(Integer.toString(textField.getLength()))));

        parent.getChildren().add(lengthLabelContainer);
        parent.getChildren().add(BaitComponentHelper.createEmptyHBoxSpacer(5, true));

        Button btn = new Button(rb.getString("label.importFromConstruction"));
        btn.setTooltip(new Tooltip(rb.getString("tooltip.importFromConstruction")));
        btn.setOnAction(event -> {
            String key = heatComparisonTabAccessProxy.getCurrentInput();
            if (!BaitConstants.PATTERN_SIMPLE_64.matcher(key).matches()) {
                showErrorMessage(rb.getString("error.keyNotValidInConstructionTab"));
                return;
            }
            removeErrorMessageAndRedBorder(comparisonTextFieldReferenceKey);
            textField.setText(key);
        });
        parent.getChildren().add(btn);
        parent.getChildren().add(BaitComponentHelper.createEmptyHBoxSpacer(56, false));

        if (heatComparisonTabAccessProxy != null) {
            DarkModeHelper.toggleDarkModeForComponent(heatComparisonTabAccessProxy.isDarkModeEnabled(), parent);
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
                if (!BaitConstants.PATTERN_SIMPLE_64.matcher(input).matches()) {
                    showErrorMessage(rb.getString("error.invalidReferenceKey64"));
                    return;
                }
                break;
            case BLIND:
                comparisonRadioReferenceKeyTypePKH.fire();
                if (!BaitConstants.PATTERN_SIMPLE_40.matcher(input).matches()) {
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
        if (!BaitConstants.PATTERN_SIMPLE_64.matcher(input).matches()) {
            showErrorMessage(rb.getString("error.invalidCurrentKey64"));
            return;
        }
        removeErrorOrInfoMessage();
        comparisonTextFieldCurrentKey.setText(input);
    }

    private void insertComponentForReferencePKH() {
        comparisonVBoxReferenceKeyParent.getChildren().clear();

        HBox parent = new HBox();
        parent.setAlignment(Pos.CENTER);

        parent.getChildren().add(BaitComponentHelper.createEmptyHBoxSpacer(5, true));

        Label label = new Label(rb.getString("label.publicKey"));
        parent.getChildren().add(label);

        parent.getChildren().add(BaitComponentHelper.createEmptyHBoxSpacer(80, false));

        TextField textField = new TextField();
        textField.setPrefWidth(440.0);
        textField.getStyleClass().add("fullPKHInput"); //TODO: width should depend on font metrics
        textField.setTextFormatter(BaitFormatterFactory.getDefaultUnencodedPublicKeyFormatter());
        comparisonTextFieldReferenceKey = textField;
        parent.getChildren().add(textField);

        parent.getChildren().add(BaitComponentHelper.createEmptyHBoxSpacer(5, true));

        String lengthLabelId = "referenceKeyLength";
        HBox lengthLabelContainer = BaitComponentHelper.createHBoxWithLengthLabel(lengthLabelId);
        lengthLabelContainer.getChildren().stream()
                .map(node -> Label.class.isAssignableFrom(node.getClass()) ? (Label)node : null)
                .filter(Objects::nonNull)
                .filter(lbl -> lengthLabelId.equals(lbl.getId()))
                .findAny()
                .ifPresent(lengthLabel -> textField.textProperty().addListener(event -> lengthLabel.setText(Integer.toString(textField.getLength()))));

        parent.getChildren().add(lengthLabelContainer);
        parent.getChildren().add(BaitComponentHelper.createEmptyHBoxSpacer(5, true));

        Button btn = new Button(rb.getString("label.importFromConverter"));
        btn.setTooltip(new Tooltip(rb.getString("tooltip.importFromConverter")));
        btn.setOnAction(event -> {
            String PKH = heatComparisonTabAccessProxy.getUnencodedPubFromConverterTab();
            if (!BaitConstants.PATTERN_SIMPLE_40.matcher(PKH).matches()) {
                showErrorMessage(rb.getString("error.pkhNotValidInConverterTab"));
                return;
            }
            removeErrorOrInfoMessage();
            textField.setText(PKH);
        });
        parent.getChildren().add(btn);

        parent.getChildren().add(BaitComponentHelper.createEmptyHBoxSpacer(5, true));

        Button btn2 = new Button(rb.getString("label.exportToConverter"));
        btn2.setTooltip(new Tooltip(rb.getString("tooltip.exportToConverter")));
        btn2.setOnAction(event -> {
            String PKH = textField.getText();
            if (!BaitConstants.PATTERN_SIMPLE_40.matcher(PKH).matches()) {
                showErrorMessage(rb.getString("error.invalidPkhCannotExport"));
                return;
            }
            removeErrorOrInfoMessage();
            heatComparisonTabAccessProxy.setUnencodedPubInConverterTab(PKH);
        });
        parent.getChildren().add(btn2);
        parent.getChildren().add(BaitComponentHelper.createEmptyHBoxSpacer(80, false));

        if (heatComparisonTabAccessProxy != null) {
            DarkModeHelper.toggleDarkModeForComponent(heatComparisonTabAccessProxy.isDarkModeEnabled(), parent);
        }

        comparisonVBoxReferenceKeyParent.getChildren().add(parent);
    }

    @FXML
    private void doShowFullHeatComparison() {
        HeatComparisonContext heatComparisonContext = buildHeatComparisonContextFromUi();
        showFullHeatComparison(heatComparisonContext);
    }

    private HeatComparisonContext buildHeatComparisonContextFromUi() {
       return HeatComparisonContext.builder()
                .targetPK(comparisonTextFieldCurrentKey.getText())
                .referenceKey(comparisonTextFieldReferenceKey.getText())
                .scaleFactor(getSelectedScaleFactorFromUi())
                .comparisonType(getSelectedReferenceKeyTypeFromUi())
                .build();
    }

    public void showFullHeatComparison(HeatComparisonContext heatComparisonContext) {
        //validate context for HeatComparison
        Optional<String> error = validateContextForPubHeatDisplay(heatComparisonContext);

        if (error.isPresent()) {
            removeAllStats();
            showErrorMessage(error.get());
            return;
        }
        removeErrorOrInfoMessage();

        switch (heatComparisonContext.getComparisonType()) {
            case BLIND:
                comparisonRadioReferenceKeyTypePKH.fire();
                showPubHeatComparison(heatComparisonContext);
                removePrivStats();
                comparisonTextFieldCurrentKey.setText(heatComparisonContext.getTargetPK());
                comparisonTextFieldReferenceKey.setText(heatComparisonContext.getReferenceKey());
                setScaleFactor(heatComparisonContext.getScaleFactor());
                return;
            case COLLISION:
                comparisonRadioReferenceKeyTypePK.fire();
                showPubHeatComparison(heatComparisonContext);
                modifyAccessForShowPrivStatsButton(false);
                showPrivHeatComparison(heatComparisonContext);
                return;
            default:
                throw new IllegalArgumentException("Illegal comparison type provided at #showFullHeatComparison [type=" + heatComparisonContext.getComparisonType() + "]");
        }
    }

    private void showPubHeatComparison(HeatComparisonContext heatComparisonContext) {
        String targetPK = heatComparisonContext.getTargetPK();
        String referenceKey = heatComparisonContext.getReferenceKey();
        ScaleFactorEnum scaleFactor = heatComparisonContext.getScaleFactor();
        QuickSearchComparisonType comparisonType = heatComparisonContext.getComparisonType();

        String targetUPKH = helper.getPubKeyHashUncompressed(targetPK, false);
        String targetCPKH = helper.getPubKeyHashCompressed(targetPK, false);

        String referenceUPKH = QuickSearchComparisonType.BLIND == comparisonType ? referenceKey : helper.getPubKeyHashUncompressed(referenceKey, false);
        String referenceCPKH = QuickSearchComparisonType.BLIND == comparisonType ? referenceKey : helper.getPubKeyHashCompressed(referenceKey, false);

        setReferencePKHForUncompressed(referenceUPKH);
        setReferencePKHForCompressed(referenceCPKH);

        showCurrentScaleFactorMessage(rb.getString("info.currentResultsScaleFactor") + heatComparisonContext.getScaleFactor().getScaleFactorAsString(), TextColorEnum.GREEN);

        setTargetPKHAndCompareHeatWithReference(targetUPKH, referenceUPKH, pubUncompressedHeatPositiveAbsoluteIndexes, pubUncompressedHeatNegativeAbsoluteIndexes);
        setTargetPKHAndCompareHeatWithReference(targetCPKH, referenceCPKH, pubCompressedHeatPositiveAbsoluteIndexes, pubCompressedHeatNegativeAbsoluteIndexes);

        //compare and set pub accuracy labels
        compareWithReferenceKey(targetUPKH, referenceUPKH, PubTypeEnum.UNCOMPRESSED, scaleFactor)
                .ifPresent(heatResult -> insertPubSimilarityPercentLabels(heatResult, PubTypeEnum.UNCOMPRESSED));
        compareWithReferenceKey(targetCPKH, referenceCPKH, PubTypeEnum.COMPRESSED, scaleFactor)
                .ifPresent(heatResult -> insertPubSimilarityPercentLabels(heatResult, PubTypeEnum.COMPRESSED));
    }

    private void setTargetPKHAndCompareHeatWithReference(String targetUPKH, String referenceUPKH, Map<Integer, Label> pubPositiveHeatMappings, Map<Integer, Label> pubNegativeHeatMappings) {
        int length = referenceUPKH.length();

        for (int i = 0; i < length; i++) {
            String currentValue = String.valueOf(targetUPKH.charAt(i));
            pubPositiveHeatMappings.get(i).setText(currentValue);
            pubNegativeHeatMappings.get(i).setText(currentValue);

            int reference = Integer.parseInt(String.valueOf(referenceUPKH.charAt(i)), 16);
            int target = Integer.parseInt(currentValue, 16);
            int overflow_reference = BaitConstants.OVERFLOW_REFERENCE_1_HEX;

            PubHeatResultWrapper resultWrapper = helper.calculatePubHeatResults(reference, target, overflow_reference);
            insertPubColorStylesToUi(i, pubPositiveHeatMappings, pubNegativeHeatMappings, resultWrapper);
        }
    }

    private Optional<String> validateContextForPubHeatDisplay(HeatComparisonContext heatComparisonContext) {
        Optional<String> error = validateTargetPK(heatComparisonContext.getTargetPK());

        if (error.isPresent()) {
            addRedBorder(comparisonTextFieldCurrentKey);
            return error;
        }
        removeErrorMessageAndRedBorder(comparisonTextFieldCurrentKey);

        error = validateReferenceKey(heatComparisonContext.getReferenceKey(), heatComparisonContext.getComparisonType());
        if (error.isPresent()) {
            addRedBorder(comparisonTextFieldReferenceKey);
            return error;
        }
        removeErrorMessageAndRedBorder(comparisonTextFieldReferenceKey);

        if (heatComparisonContext.getScaleFactor() == null) {
            return Optional.of("Scale factor must be provided!");
        }

        return Optional.empty();
    }

    private Optional<String> validateContextForPrivHeatDisplay(HeatComparisonContext heatComparisonContext) {
        Optional<String> error = validateTargetPK(heatComparisonContext.getTargetPK());

        if (error.isPresent()) {
            addRedBorder(comparisonTextFieldCurrentKey);
            return error;
        }
        removeErrorMessageAndRedBorder(comparisonTextFieldCurrentKey);

        if (QuickSearchComparisonType.COLLISION != heatComparisonContext.getComparisonType()) {
            return Optional.of(rb.getString("error.noPrivComparisonForBlindType"));
        }
        removeErrorOrInfoMessage();

        error = validateReferenceKey(heatComparisonContext.getReferenceKey(), heatComparisonContext.getComparisonType());
        if (error.isPresent()) {
            addRedBorder(comparisonTextFieldReferenceKey);
            return error;
        }
        removeErrorMessageAndRedBorder(comparisonTextFieldReferenceKey);

        return Optional.empty();
    }

    private Optional<String> validateTargetPK(@NonNull String targetPK) {
        if (!BaitConstants.PATTERN_SIMPLE_64.matcher(targetPK).matches()) {
            return Optional.of(rb.getString("error.invalidCurrentKey64"));
        }

        if (!PrivKeyValidator.isValidPK(targetPK)) {
            return Optional.of(rb.getString("error.invalidCurrentKeyWithReason") + buildReasonForInvalidKey(targetPK));
        }

        return Optional.empty();
    }

    private Optional<String> validateReferenceKey(@NonNull String referenceKey, QuickSearchComparisonType comparisonType) {
        if (comparisonType == null) {
            return Optional.of("Comparison type must be provided!");
        }

        switch (comparisonType) {
            case COLLISION:
                if (!BaitConstants.PATTERN_SIMPLE_64.matcher(referenceKey).matches()) {
                    return Optional.of(rb.getString("error.invalidReferenceKey64"));
                }

                if (!PrivKeyValidator.isValidPK(referenceKey)) {
                    return Optional.of(rb.getString("error.invalidReferenceKeyWithReason") + buildReasonForInvalidKey(referenceKey));
                }
                break;
            case BLIND:
                if (!BaitConstants.PATTERN_SIMPLE_40.matcher(referenceKey).matches()) {
                    return Optional.of(rb.getString("error.invalidReferenceKey40"));
                }
                break;
            default:
                throw new IllegalStateException("Received wrong QuickSearchComparisonType at #isReferenceKeyValid [type=" + comparisonType + "]");
        }

        return Optional.empty();
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
        removeTextFromMappedComponents(pubUncompressedHeatPositiveAbsoluteIndexes, true);
        removeTextFromMappedComponents(pubUncompressedHeatNegativeAbsoluteIndexes, true);

        removeTextFromMappedComponents(pubCompressedHeatPositiveAbsoluteIndexes, true);
        removeTextFromMappedComponents(pubCompressedHeatNegativeAbsoluteIndexes, true);

        removeTextFromMappedComponents(referenceForUncompressedAbsoluteIndexes, false);
        removeTextFromMappedComponents(referenceForCompressedAbsoluteIndexes, false);
    }
    
    private void removeTextFromMappedComponents(@NonNull Map<Integer, Label> componentMap, boolean clearStyleClass) {
        componentMap.keySet().forEach(key -> {
            if (clearStyleClass) {
                componentMap.get(key).getStyleClass().clear();
            }
            componentMap.get(key).setText("");
        });
    }

    private void removePrivStats() {
        for (int i = 0; i < 64; i++) {
            privHeatPositiveAbsoluteIndexes.get(i).setText(BaitConstants.EMPTY_STRING);
            privHeatNegativeAbsoluteIndexes.get(i).setText(BaitConstants.EMPTY_STRING);
        }

        for (int i = 1; i <= 8; i++) {
            privHeatPositiveNumericLabels.get(i).setText(BaitConstants.EMPTY_STRING);
            privHeatNegativeNumericLabels.get(i).setText(BaitConstants.EMPTY_STRING);
            privHeatPositiveContainerMappings.get(i).getStyleClass().clear();
            privHeatNegativeContainerMappings.get(i).getStyleClass().clear();
        }
    }

    private String buildReasonForInvalidKey(@NonNull String invalidKey) {
        if (invalidKey.startsWith("0000")) {
            return rb.getString("error.privLowerThanMin");
        }

        return invalidKey.startsWith("FFFF") ? rb.getString("error.privHigherThanMax") : rb.getString("error.unknown");
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
        removeErrorOrInfoMessage();
        removeRedBorder(component);
    }

    private void removeErrorOrInfoMessage() {
        comparisonLabelPubErrorSuccessResult.setText(BaitConstants.EMPTY_STRING);
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
        comparisonLabelResultForScaleFactor.setText(BaitConstants.EMPTY_STRING);
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

    private void setReferencePKHForCompressed(String CPKH) {
        if (!BaitConstants.PATTERN_SIMPLE_40.matcher(CPKH).matches()) {
            showErrorMessage(rb.getString("error.invalidReferenceCPKH"));
            return;
        }

        for (int i = 0; i < CPKH.length(); i++) {
            String current = String.valueOf(CPKH.charAt(i));
            referenceForCompressedAbsoluteIndexes.get(i).setText(current);
        }
    }

    private void setReferencePKHForUncompressed(String UPKH) {
        if (!BaitConstants.PATTERN_SIMPLE_40.matcher(UPKH).matches()) {
            showErrorMessage(rb.getString("error.invalidReferenceUPKH"));
            return;
        }

        for (int i = 0; i < UPKH.length(); i++) {
            String current = String.valueOf(UPKH.charAt(i));
            referenceForUncompressedAbsoluteIndexes.get(i).setText(current);
        }
    }

    @NonNull
    private ScaleFactorEnum getSelectedScaleFactorFromUi() {
        ScaleFactorEnum selectedScaleFactor = comparisonComboBoxScaleFactor.getSelectionModel().getSelectedItem();
        if (selectedScaleFactor == null) {
            selectedScaleFactor = Config.DEFAULT_SCALE_FACTOR;
        }
        return selectedScaleFactor;
    }

    void insertPubSimilarityPercentLabels(PubComparisonResultS heatResult, PubTypeEnum pubType) {
        if (heatResult == null || pubType == null) {
            return;
        }

        int mapIndexForPositive = getResultForHeatType(heatResult, HeatOverflowTypeEnum.HEAT_POSITIVE);
        int mapIndexForNegative = getResultForHeatType(heatResult, HeatOverflowTypeEnum.HEAT_NEGATIVE);

        switch (pubType) {
            case UNCOMPRESSED:
                labelPercentUncompressedPositive.setText(similarityMappings.get(mapIndexForPositive).setScale(0, RoundingMode.HALF_UP).toString());
                labelPercentUncompressedNegative.setText(similarityMappings.get(mapIndexForNegative).setScale(0, RoundingMode.HALF_UP).toString());
                break;
            case COMPRESSED:
                labelPercentCompressedPositive.setText(similarityMappings.get(mapIndexForPositive).setScale(0, RoundingMode.HALF_UP).toString());
                labelPercentCompressedNegative.setText(similarityMappings.get(mapIndexForNegative).setScale(0, RoundingMode.HALF_UP).toString());
                break;
            default:
                throw new IllegalArgumentException("This pub key type is not supported [type=" + pubType + "]");
        }
    }

    private int getResultForHeatType(PubComparisonResultS currentResult, HeatOverflowTypeEnum heatType) {
        return heatComparisonTabAccessProxy.getNormalizedMapIndexFromComparisonResult(HeatOverflowTypeEnum.HEAT_POSITIVE == heatType ? currentResult.getPositive() : currentResult.getNegative(), currentResult.getForScaleFactor());
    }

    private String getReferencePKHForUncompressedFromUi() {
        StringBuilder sb = new StringBuilder(40);
        for (int i = 0; i < 40; i++) {
            sb.append(referenceForUncompressedAbsoluteIndexes.get(i).getText());
        }

        return sb.toString();
    }

    private String getReferencePKHForCompressedFromUi() {
        StringBuilder sb = new StringBuilder(40);
        for (int i = 0; i < 40; i++) {
            sb.append(referenceForCompressedAbsoluteIndexes.get(i).getText());
        }

        return sb.toString();
    }

    private void insertPubColorStylesToUi(int wordNumber, Map<Integer, Label> pubPositiveHeatMappings, Map<Integer, Label> pubNegativeHeatMappings, PubHeatResultWrapper resultWrapper) {
        pubPositiveHeatMappings.get(wordNumber).getStyleClass().clear();
        pubPositiveHeatMappings.get(wordNumber).getStyleClass().add(colorMappings.get(resultWrapper.getHeatPositive()));

        pubNegativeHeatMappings.get(wordNumber).getStyleClass().clear();
        pubNegativeHeatMappings.get(wordNumber).getStyleClass().add(colorMappings.get(resultWrapper.getHeatNegative()));
    }

    private Optional<PubComparisonResultS> compareWithReferenceKey(String currentPHK, String referencePKH, PubTypeEnum type, ScaleFactorEnum scaleFactor) {
        if (type == null) {
            return Optional.empty();
        }

        return pubComparer.comparePubKeyHashes(type, referencePKH, currentPHK, scaleFactor);
    }

    @FXML
    private void doShowPrivHeatComparison(ActionEvent actionEvent) {
        HeatComparisonContext heatComparisonContext = buildHeatComparisonContextFromUi();
        showPrivHeatComparison(heatComparisonContext);
    }

    private void showPrivHeatComparison(HeatComparisonContext heatComparisonContext) {
        Optional<String> error = validateContextForPrivHeatDisplay(heatComparisonContext);
        if (error.isPresent()) {
            showErrorMessage(error.get());
            return;
        }
        removeErrorOrInfoMessage();

        String targetPK = heatComparisonContext.getTargetPK();
        String referenceKey = heatComparisonContext.getReferenceKey();
        String currentPrivWord;

        for (int wordNumber = 1; wordNumber <= 8; wordNumber++) {
            currentPrivWord = targetPK.substring((wordNumber - 1) * 8, ((wordNumber - 1) * 8) + 8);
            insertPrivWordToPrivHeatMaps(wordNumber, currentPrivWord);

            String currentReferenceWord = referenceKey.substring((wordNumber - 1) * 8, ((wordNumber - 1) * 8) + 8);
            int finalWordNumber = wordNumber;
            getPrivHeat(currentReferenceWord, currentPrivWord, currentNumberFormatType).ifPresent(result -> {
                insertColorsToPrivHeatMaps(result, finalWordNumber);
                insertPrivNumericDifferenceLabel(finalWordNumber, result, currentNumberFormatType);
            });
        }

        comparisonTextFieldCurrentKey.setText(targetPK);
        comparisonTextFieldReferenceKey.setText(referenceKey);
        setScaleFactor(heatComparisonContext.getScaleFactor());

        // buildLastPrivFromUserDataFields();
    }

    // private String buildLastPrivFromUserDataFields() {
    //     return Stream.iterate(1, i -> ++i)
    //             .limit(8)
    //             .map(index -> (String) privHeatPositiveContainerMappings.get(index).getUserData())
    //             .filter(Objects::nonNull)
    //             .collect(Collectors.joining(BaitConstants.EMPTY_STRING));
    // }

    @FXML
    //TODO: HEX numbers should be prefixed with 0x
    private void doChangeHeatResultFormat(ActionEvent actionEvent) {
        String currentPriv = comparisonTextFieldCurrentKey.getText();
        if (!BaitConstants.PATTERN_SIMPLE_64.matcher(currentPriv).matches()) {
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

    public final void changePrivHeatColorFocus() {
        if (comparisonRadioReferenceKeyTypePKH.isSelected()) {
            //TODO: i18n
           showErrorMessage("No reference priv. Cannot calculate priv heat");
           return;
        }
        removeErrorOrInfoMessage();

        Optional<String> error = validateTargetPK(comparisonTextFieldCurrentKey.getText());
        if (error.isPresent()) {
            showErrorMessage(error.get());
            return;
        }
        removeErrorOrInfoMessage();

        QuickSearchComparisonType comparisonType = getSelectedReferenceKeyTypeFromUi();
        if (QuickSearchComparisonType.BLIND == comparisonType) {
            addErrorMessageAndRedBorder(rb.getString("error.cannotCompareWithoutReference"), comparisonRadioReferenceKeyTypePKH);
            return;
        }

        error = validateReferenceKey(comparisonTextFieldReferenceKey.getText(), comparisonType);
        if (error.isPresent()) {
            showErrorMessage(error.get());
            return;
        }
        removeErrorOrInfoMessage();

        for (int i = 1; i <= 8; i++) {
            String wordInCurrentKeyField = readHexWordFromUi(i, PrivKeyTargetTypeEnum.CURRENT_KEY);
            String wordInReferenceKeyField = readHexWordFromUi(i, PrivKeyTargetTypeEnum.REFERENCE_KEY);
            int justI = i; //lambda needs final
            getPrivHeat(wordInReferenceKeyField, wordInCurrentKeyField, currentNumberFormatType)
                    .ifPresent(result -> insertColorsToPrivHeatMaps(result, justI));
        }
    }

    private Optional<PrivHeatResultWrapper> getPrivHeat(String referencePKWord, String targetPKWord, @NonNull NumberFormatTypeEnum type) {
        Long reference = Long.parseLong(referencePKWord, 16);
        Long current = Long.parseLong(targetPKWord, 16);
        Long overflow_reference = BaitConstants.OVERFLOW_REFERENCE_8_HEX;

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
                return helper.padToX(Long.toHexString(heatResult), 8, true);
            default:
                throw new IllegalArgumentException("Number format type not supported [type=" + targetType + "]");
        }
    }

    @FXML
    private void doImportPriv() {
        String currentInput = heatComparisonTabAccessProxy.getCurrentInput();
        if (!BaitConstants.PATTERN_SIMPLE_64.matcher(currentInput).matches()) {
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
        privHeatPositiveContainerMappings.get(wordNumber).getStyleClass().add(colorMappings.get(colorIndexPositive));

        privHeatNegativeContainerMappings.get(wordNumber).getStyleClass().clear();
        privHeatNegativeContainerMappings.get(wordNumber).getStyleClass().add(colorMappings.get(colorIndexNegative));
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
        return heatComparisonTabAccessProxy != null;
    }
}
