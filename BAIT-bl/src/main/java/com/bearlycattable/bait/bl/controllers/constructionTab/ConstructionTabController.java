package com.bearlycattable.bait.bl.controllers.constructionTab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.bearlycattable.bait.bl.controllers.RootController;
import com.bearlycattable.bait.commons.CssConstants;
import com.bearlycattable.bait.commons.HeatVisualizerConstants;
import com.bearlycattable.bait.commons.enums.TextColorEnum;
import com.bearlycattable.bait.commons.helpers.HeatVisualizerHelper;
import com.bearlycattable.bait.commons.validators.PrivKeyValidator;
import com.bearlycattable.bait.utility.BundleUtils;
import com.bearlycattable.bait.utility.LocaleUtils;
import com.bearlycattable.bait.utility.RandomAddressGenerator;

import javafx.event.ActionEvent;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import lombok.Getter;

@Getter
public class ConstructionTabController {

    private static final Logger LOG = Logger.getLogger(ConstructionTabController.class.getName());
    private final ResourceBundle rb = ResourceBundle.getBundle(BundleUtils.GLOBAL_BASE_NAME + "ConstructionTab", LocaleUtils.APP_LANGUAGE);
    @FXML
    private Label constructionLabelShowingResultsForMessage;

    private enum RandomType {RANDOM_FULL, RANDOM_SAME_WORD}

    private final Map<Integer, TextField> inputFieldWordMappings = new HashMap<>();
    private final Map<Integer, HBox> privWordComboBoxParentContainerMappings = new HashMap<>();
    private final Map<Integer, CheckBox> privWordCheckboxMappings = new HashMap<>();
    @SuppressWarnings("rawtypes")
    private final Map<Integer, ComboBox> privCompleteComboBoxMappings = new HashMap<>();
    private final List<Integer> disabledWords = new ArrayList<>();

    private final HeatVisualizerHelper helper = new HeatVisualizerHelper();
    private final RandomAddressGenerator generator = RandomAddressGenerator.getSecureGenerator(64);

    private RootController rootController;
    private boolean programmaticChange;

    @FXML
    private HBox constructionHBoxParentForComboAndCheckBoxes;
    @FXML
    private HBox constructionHBoxCurrentInputParent;

    //random
    @FXML
    private RadioButton constructionRadioRandomFull;
    @FXML
    private RadioButton constructionRadioRandomSameWord;
    @FXML
    private HBox constructionHBoxRandomPrefixParent;
    @FXML
    private TextField constructionTextFieldRandomWordPrefix;

    //results
    @FXML
    private Label constructionLabelShowingResultsForKey; //if calculate is clicked - show current priv here
    @FXML
    private Label constructionLabelErrorSuccessMessage; //main error/success message

    @FXML
    private Label constructionLabelCurrentInputLength;
    @FXML
    private TextField constructionTextFieldUUPub;
    @FXML
    private Label constructionLabelUUPubLength;
    @FXML
    private TextField constructionTextFieldUEPub;
    @FXML
    private Label constructionLabelUEPubLength;
    @FXML
    private TextField constructionTextFieldCUPub;
    @FXML
    private Label constructionLabelCUPubLength;
    @FXML
    private TextField constructionTextFieldCEPub;
    @FXML
    private Label constructionLabelCEPubLength;
    
    public void setRootController(RootController rootController) {
        this.rootController = rootController;
    }

    @FXML
    void initialize() {
        System.out.println("CREATING: ConstructionTabController......");
    }

    @FXML
    private void doChangeInputValue(ActionEvent actionEvent) {
        EventTarget target = actionEvent.getTarget();
        if (!(target instanceof ComboBox<?>)) {
            return;
        }

        ComboBox<?> currentComboBox = (ComboBox<?>) target;
        String id = currentComboBox.getId();

        int index = Integer.parseInt(id.substring(CssConstants.PRIV_INPUT_COMBOBOX_PREFIX.length()));
        int wordNumber = (index / 8) + 1;

        privWordCheckboxMappings.get(wordNumber).setDisable(!isValidWordInComboBoxesUi(wordNumber));

        String currentWord = null;
        if (programmaticChange) { //change came from manipulating the text field
            currentWord = getCurrentInputWordFromInputField(wordNumber);
        } else if (isValidWordInComboBoxesUi(wordNumber)) {
            currentWord = getCurrentInputWordFromComboBoxes(wordNumber);
        }

        // int internalIndex = index % 8; //index currently being changed

        if (currentWord != null && currentWord.length() == 8) {
            setCurrentInputWord(currentWord, wordNumber);
        }
    }

    @FXML
    private void doGenerateRandom() {
        RandomType randomType = getRandomTypeFromUi();
        String priv;
        String prefix;

        if (randomType == null) {
            return;
        }

        switch (randomType) {
            case RANDOM_FULL:
                prefix = constructionTextFieldRandomWordPrefix.getText();
                priv = generator.generateRandomPrefixedWithBlacklist(prefix, getCurrentInput(), disabledWords);
                break;
            case RANDOM_SAME_WORD:
                prefix = constructionTextFieldRandomWordPrefix.getText();
                priv = generator.generateRandomSameWordWithBlacklist(prefix, getCurrentInput(), disabledWords);
                break;
            default:
                throw new IllegalArgumentException("Requested random type is not supported [randomType=" + randomType + "]");
        }

        setCurrentInput(priv);
        removeErrorMessageAndRedBorder();
    }

    private RandomType getRandomTypeFromUi() {
        if (constructionRadioRandomFull.isSelected() && constructionRadioRandomSameWord.isSelected() ||
                (!constructionRadioRandomFull.isSelected() && !constructionRadioRandomSameWord.isSelected())) {
            throw new IllegalStateException("None or both of the random types are selected. Cannot generate key");
        }
        return constructionRadioRandomFull.isSelected() ? RandomType.RANDOM_FULL : RandomType.RANDOM_SAME_WORD;
    }

    String getCurrentInputWordFromInputField(int wordNumber) {
        return inputFieldWordMappings.get(wordNumber).getText();
    }

    @FXML
    private void doCalculatePKH() {
        String currentPriv = rootController.getCurrentInput();

        if (!PrivKeyValidator.isValidPK(currentPriv)) {
            insertErrorMessageAndRedBorder(rb.getString("error.invalidCurrentInput"), constructionHBoxCurrentInputParent);
            return;
        }

        constructionLabelShowingResultsForMessage.setText("");
        constructionLabelShowingResultsForKey.setText("");
        removeErrorMessageAndRedBorder();

        String pubKeyHashUncompressed = helper.getPubKeyHashUncompressed(currentPriv, false);
        String pubKeyHashCompressed = helper.getPubKeyHashCompressed(currentPriv, false);

        constructionLabelShowingResultsForMessage.setText(rb.getString("label.showingResultsFor"));
        constructionLabelShowingResultsForKey.setText(currentPriv);

        constructionTextFieldUUPub.setText(pubKeyHashUncompressed);
        constructionTextFieldUEPub.setText(helper.encodeToBase58(0, pubKeyHashUncompressed));

        constructionTextFieldCUPub.setText(pubKeyHashCompressed);
        constructionTextFieldCEPub.setText(helper.encodeToBase58(0, pubKeyHashCompressed));
    }

    private void insertErrorMessageAndRedBorder(String errorMessage, Pane component) {
        constructionLabelErrorSuccessMessage.getStyleClass().clear();
        constructionLabelErrorSuccessMessage.getStyleClass().add(CssConstants.ERROR_INFO_MESSAGE_STYLE_CLASS);
        constructionLabelErrorSuccessMessage.getStyleClass().add(TextColorEnum.RED.getStyleClass());
        constructionLabelErrorSuccessMessage.setText(errorMessage);

        if (!component.getStyleClass().contains(CssConstants.BORDER_RED)) {
            component.getStyleClass().add(CssConstants.BORDER_RED);
        }
    }

    private void removeErrorMessageAndRedBorder() {
        constructionLabelErrorSuccessMessage.getStyleClass().clear();
        constructionLabelErrorSuccessMessage.getStyleClass().add(CssConstants.ERROR_INFO_MESSAGE_STYLE_CLASS);
        constructionLabelErrorSuccessMessage.setText(HeatVisualizerConstants.EMPTY_STRING);
    }

    void setCurrentInput(String priv) {
        for (int i = 1; i <= 8; i++) {
            int indexTo = calculateIndexTo(i);
            if (indexTo > priv.length()) {
                break;
            }
            String currentWord = priv.substring(calculateIndexFrom(i), indexTo);
            setCurrentInputWord(currentWord, i);
        }
    }

    /**
     * Sets current input key forcefully (ignoring any locked words)
     * @param priv - private key (64 hex string)
     */
    public void setCurrentInputForced(String priv) {
        for (int i = 1; i <= 8; i++) {
            int indexTo = calculateIndexTo(i);
            if (indexTo > priv.length()) {
                break;
            }
            String currentWord = priv.substring(calculateIndexFrom(i), indexTo);
            setCurrentInputWordForced(currentWord, i);
        }
    }

    private void setCurrentInputWordForced(String hexWord, int wordNumber) {
        if (rootController.isValidWordNumber(wordNumber)) {
            inputFieldWordMappings.get(wordNumber).setText(hexWord);
        }
    }

    private void setCurrentInputWord(String hexWord, int wordNumber) {
        if (rootController.isValidWordNumber(wordNumber) && !disabledWords.contains(wordNumber)) {
            inputFieldWordMappings.get(wordNumber).setText(hexWord);
        }
    }

    String getCurrentInputWordFromComboBoxes(int wordNumber) {
        int indexFrom = calculateIndexFrom(wordNumber);
        int indexTo = calculateIndexTo(wordNumber);

        return privCompleteComboBoxMappings.keySet().stream()
                .filter(key -> key >= indexFrom && key < indexTo)
                .map(validKey -> privCompleteComboBoxMappings.get(validKey).getSelectionModel().getSelectedItem())
                // .filter(Objects::nonNull)
                .map(String.class::cast)
                .collect(Collectors.joining(HeatVisualizerConstants.EMPTY_STRING));
    }

    @SuppressWarnings("unchecked")
    public void setPrivWordComboBoxesInUi(String newValue, int wordNumber) {
        programmaticChange = true;
        int indexFrom = (wordNumber - 1) * 8;
        int indexTo = indexFrom + 8;
        for (int i = indexFrom; i < indexTo; i++) {
            int currentCharIndex = i - indexFrom;
            if (currentCharIndex >= newValue.length()) {
                privCompleteComboBoxMappings.get(i).getSelectionModel().clearSelection(); //remove selection
                continue;
            }
            privCompleteComboBoxMappings.get(i).getSelectionModel().select(String.valueOf(newValue.charAt(currentCharIndex)));
        }
        programmaticChange = false;
    }

    private int calculateIndexFrom(int wordNumber) {
        return (wordNumber - 1) * 8;
    }

    private int calculateIndexTo(int wordNumber) {
        return ((wordNumber - 1) * 8) + 8;
    }

    /**
     * Gets current input from input fields word by word.
     * If word is not valid, "00000000" placeholder is returned instead.
     * @return combined private key from 8 'current input' fields
     */
    public String getCurrentInput() {
        return inputFieldWordMappings.keySet().stream()
                .map(k -> {
                    String word = inputFieldWordMappings.get(k).getText();
                    if (word.length() != 8) {
                        return "00000000";
                    }
                    return word;
                })
                .collect(Collectors.joining(HeatVisualizerConstants.EMPTY_STRING));
    }

    /**
     * Gets current input from input fields word by word. Even if words are not valid or empty.
     * Caller is responsible for making sure that input field has correct data.
     * @return combined private key from 8 'current input' fields
     */
    public String getCurrentInputForced() {
        return inputFieldWordMappings.keySet().stream()
                .map(k -> inputFieldWordMappings.get(k).getText())
                .collect(Collectors.joining(HeatVisualizerConstants.EMPTY_STRING));
    }

    public synchronized final void setProgrammaticChange(boolean programmaticChange) {
        this.programmaticChange = programmaticChange;
    }

    @FXML
    private void doCopyKeyFromCurrentInput() {
        Map<DataFormat, Object> content = new HashMap<>();
        String currentKey = getCurrentInputForced();
        if (!rootController.isValidPrivPattern(currentKey)) {
            insertErrorMessageAndRedBorder(rb.getString("error.invalidKeyForCopy"), constructionHBoxCurrentInputParent);
            return;
        }
        removeErrorMessageAndRedBorder();

        content.put(DataFormat.PLAIN_TEXT, currentKey);
        Clipboard.getSystemClipboard().setContent(content);
    }

    @FXML
    private void doPasteKeyToCurrentInput() {
        Object plainText = Clipboard.getSystemClipboard().getContent(DataFormat.PLAIN_TEXT);
        if (plainText != null && String.class.isAssignableFrom(plainText.getClass())) {
            String key = (String) plainText;
            if (HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(key).matches()) {
                setCurrentInputForced(key);
                return;
            }
        }

        showErrorMessage(rb.getString("error.invalidKeyForPaste"));
    }

    private void showErrorMessage(String errorMessage) {
        if (!constructionLabelErrorSuccessMessage.getStyleClass().contains(TextColorEnum.RED.getStyleClass())) {
            constructionLabelErrorSuccessMessage.getStyleClass().add(TextColorEnum.RED.getStyleClass());
        }
        constructionLabelErrorSuccessMessage.setText(errorMessage);
    }

    //ui methods
    public boolean isValidWordInComboBoxesUi(int wordNumber) {
        if (!rootController.isValidWordNumber(wordNumber)) {
            return false;
        }

        int from = calculateIndexFrom(wordNumber);
        int to = calculateIndexTo(wordNumber);
        return Stream.iterate(from, index -> ++index)
                .limit(to - from)
                .map(index -> privCompleteComboBoxMappings.get(index).getSelectionModel().getSelectedItem())
                .map(String.class::cast)
                .allMatch(str -> str != null && HeatVisualizerConstants.PATTERN_HEX_01.matcher(str).matches());
    }

    //TODO: this is never false?
    public boolean isCurrentPrivPresent() {
        return inputFieldWordMappings.keySet().stream()
                .allMatch(k -> inputFieldWordMappings.get(k).getText() != null);
    }

    public void modifyWordComboBoxAndTextFieldAccess(int wordNum, boolean isChecked) {
        HBox currentComboBoxParentContainer = privWordComboBoxParentContainerMappings.get(wordNum);
        currentComboBoxParentContainer.setDisable(isChecked);
        currentComboBoxParentContainer.getStyleClass().clear();

        TextField currentInputTextField = inputFieldWordMappings.get(wordNum);
        currentInputTextField.setDisable(isChecked);

        if (isChecked) {
            currentComboBoxParentContainer.getStyleClass().add(CssConstants.BORDER_RED);
            if (!currentInputTextField.getStyleClass().contains(CssConstants.BORDER_RED)) {
                currentInputTextField.getStyleClass().add(CssConstants.BORDER_RED);
            }
            disabledWords.add(wordNum);
            return;
        }

        currentInputTextField.getStyleClass().remove(CssConstants.BORDER_RED);
        disabledWords.remove((Integer) wordNum);
    }
    
    public final boolean isParentValid() {
        return rootController != null;
    }

}
