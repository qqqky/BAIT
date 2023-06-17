package com.bearlycattable.bait.bl.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

import com.bearlycattable.bait.advanced.UnencodedPubListReader;
import com.bearlycattable.bait.advanced.helpers.UserInputUtils;
import com.bearlycattable.bait.advancedCommons.helpers.HeatVisualizerComponentHelper;
import com.bearlycattable.bait.bl.helpers.DarkModeHelper;
import com.bearlycattable.bait.commons.CssConstants;
import com.bearlycattable.bait.commons.HeatVisualizerConstants;
import com.bearlycattable.bait.advancedCommons.contexts.P2PKHSingleResultData;
import com.bearlycattable.bait.commons.enums.TextColorEnum;
import com.bearlycattable.bait.commons.helpers.HeatVisualizerModalHelper;
import com.bearlycattable.bait.advancedCommons.helpers.P2PKHSingleResultDataHelper;
import com.bearlycattable.bait.advancedCommons.serialization.SerializedSearchResultsReader;
import com.bearlycattable.bait.utility.BundleUtils;
import com.bearlycattable.bait.utility.LocaleUtils;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

public class AdvancedSubTabToolsController {

    private final ResourceBundle rb = ResourceBundle.getBundle(BundleUtils.GLOBAL_BASE_NAME + "AdvancedSubTabTools", LocaleUtils.APP_LANGUAGE);
    @FXML
    private RadioButton advancedToolsRadioCreateTemplateFromManualInput;
    @FXML
    private TextField advancedToolsTextFieldMaxEntriesInManualInput;
    @FXML
    private RadioButton advancedToolsRadioCreateTemplateFromFile;
    @FXML
    private TextField advancedToolsTextFieldMaxEntriesInFile;

    @FXML
    private TextArea advancedToolsTextAreaManualPubInput;
    @FXML
    private TextField advancedToolsTextFieldPathToPubList;
    @FXML
    private TextField advancedToolsTextFieldTemplateSavePath;
    @FXML
    private Button advancedToolsBtnCreateEmptyTemplate;
    @FXML
    private Label searchToolsLabelTemplateCreationResult; //template creation error/success message label
    @FXML
    private TextField advancedToolsTextFieldMergeInputFile01;
    @FXML
    private TextField advancedToolsTextFieldMergeInputFile02;
    @FXML
    private TextField advancedToolsTextFieldMergeOutputFilePath;
    @FXML
    private Button advancedToolsBtnMergeResultFiles;
    @FXML
    private Label searchToolsLabelMergeResult; //merge error/success message label
    @FXML
    private TextField advancedToolsTextFieldMultiSource;
    @FXML
    private TextField advancedToolsTextFieldMultiTarget;
    @FXML
    private Label searchToolsLabelMultiUnencodeResultMessage; //multi-unencode error/success message label
    private AdvancedTabMainController parentController;
    @FXML
    private HBox advancedToolsTemplateCreationSourceParentContainer;

    public void setParentController(AdvancedTabMainController parentController) {
        this.parentController = Objects.requireNonNull(parentController);
    }

    @FXML
    void initialize() {
        System.out.println("CREATING (child): AdvancedSubTabToolsController......");

        advancedToolsRadioCreateTemplateFromManualInput.setOnAction(event -> {
            insertComponentForManualAddressInput();
        });

        advancedToolsRadioCreateTemplateFromFile.setOnAction(event -> {
            insertComponentForFileSelection();
        });

        advancedToolsRadioCreateTemplateFromManualInput.fire();
    }

    private void insertComponentForManualAddressInput() {
        advancedToolsTemplateCreationSourceParentContainer.getChildren().clear();

        HBox parent = new HBox();
        parent.setAlignment(Pos.CENTER);

        HBox labelContainer = new HBox();
        labelContainer.setAlignment(Pos.CENTER);
        labelContainer.setPrefHeight(50.0);
        labelContainer.getChildren().add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(5, true));
        labelContainer.getChildren().add(new Label(rb.getString("label.enterAddressesHere")));
        labelContainer.getChildren().add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(5, true));
        parent.getChildren().add(labelContainer);

        TextArea textArea = new TextArea();
        textArea.setPrefWidth(400.0);
        textArea.getStyleClass().add("fullPKHInput"); //TODO: width should depend on font metrics
        textArea.setPrefHeight(75.0);
        textArea.setWrapText(true);
        advancedToolsTextAreaManualPubInput = textArea;
        parent.getChildren().add(textArea);

        if (parentController != null) {
            DarkModeHelper.toggleDarkModeForComponent(parentController.isDarkModeEnabled(), parent);
        }

        advancedToolsTemplateCreationSourceParentContainer.getChildren().add(parent);
    }

    private void insertComponentForFileSelection() {
        advancedToolsTemplateCreationSourceParentContainer.getChildren().clear();

        HBox parent = new HBox();
        parent.setAlignment(Pos.CENTER);

        HBox parentContentWrapper = new HBox();
        parentContentWrapper.setAlignment(Pos.CENTER_LEFT);
        parentContentWrapper.getChildren().add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(5, true));
        Label label = new Label(rb.getString("label.selectFile"));
        label.setPrefWidth(96.0);
        label.setPrefHeight(32.0);
        parentContentWrapper.getChildren().add(label);

        TextField textField = new TextField();
        textField.setPrefWidth(854.0);
        textField.getStyleClass().add("pathInput"); // TODO: width should depend on font metrics
        textField.setPrefHeight(32.0);
        textField.setTooltip(new Tooltip(rb.getString("tooltip.selectYourAddressList")));
        advancedToolsTextFieldPathToPubList = textField;
        parentContentWrapper.getChildren().add(textField);
        parentContentWrapper.getChildren().add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(5, true));

        Button button = new Button(rb.getString("label.browse"));
        button.setOnAction(event -> browseAddressListPath(advancedToolsTextFieldPathToPubList));
        parentContentWrapper.getChildren().add(button);

        parent.getChildren().add(parentContentWrapper);

        if (parentController != null) {
            DarkModeHelper.toggleDarkModeForComponent(parentController.isDarkModeEnabled(), parent);
        }

        advancedToolsTemplateCreationSourceParentContainer.getChildren().add(parent);
    }

    private void browseAddressListPath(TextInputControl textInputComponent) {
        Optional<String> absPath = HeatVisualizerModalHelper.selectTxtResourceForOpen(rb.getString("label.openResource"), textInputComponent);
        if (absPath.isPresent()) {
            textInputComponent.setText(absPath.get());
            removeRedBorder(textInputComponent);
            return;
        }
        addRedBorder(textInputComponent);
    }

    @FXML
    private void doBrowseAddressListSavePath() {
        Optional<String> absPath = HeatVisualizerModalHelper.selectJsonResourceForSave(rb.getString("label.saveTo"), advancedToolsTextFieldTemplateSavePath);
        if (absPath.isPresent()) {
            advancedToolsTextFieldTemplateSavePath.setText(absPath.get());
            removeRedBorder(advancedToolsTextFieldTemplateSavePath);
            return;
        }
        addRedBorder(advancedToolsTextFieldTemplateSavePath);
    }

    @FXML
    private void doCreateEmptyResultTemplate() {
        //firstly, check if save path seems valid (advancedToolsTextFieldTemplateSavePath)
        String savePath = advancedToolsTextFieldTemplateSavePath.getText();
        if (savePath.isEmpty() || !savePath.endsWith(".json")) {
            insertErrorLabelAndRedBorder(searchToolsLabelTemplateCreationResult, advancedToolsTextFieldTemplateSavePath, "Error. Save path is empty or file format is not valid (must be .json)");
            return;
        }

        removeRedBorder(advancedToolsTextFieldTemplateSavePath);

        Optional<P2PKHSingleResultData[]> template;

        if (advancedToolsRadioCreateTemplateFromManualInput.isSelected()) {
            if (advancedToolsTextAreaManualPubInput.getText().isEmpty()) {
                insertErrorLabelAndRedBorder(searchToolsLabelTemplateCreationResult, advancedToolsTextAreaManualPubInput, "No addresses found. Cannot create template");
                return;
            }
            //remove red border and error
            removeRedBorder(advancedToolsTextAreaManualPubInput);
            removeLabel(searchToolsLabelTemplateCreationResult);

            template = createPubTemplateFromManualInput();
        } else if (advancedToolsRadioCreateTemplateFromFile.isSelected()) {
            if (advancedToolsTextFieldPathToPubList.getText().isEmpty()) {
                insertErrorLabelAndRedBorder(searchToolsLabelTemplateCreationResult, advancedToolsTextFieldPathToPubList, "Path is not correct. Cannot create template");
                return;
            }
            //remove red border and error
            removeRedBorder(advancedToolsTextFieldPathToPubList);
            removeLabel(searchToolsLabelTemplateCreationResult);

            template = createPubTemplateFromFile();
        } else {
            throw new IllegalStateException("None of the radio buttons were checked at #doCreateEmptyResultTemplate");
        }

        if (!template.isPresent()) {
            insertErrorLabel(searchToolsLabelTemplateCreationResult, rb.getString("error.cannotCreateTemplate"));
            return;
        }

        //serialize and save
        boolean result = P2PKHSingleResultDataHelper.serializeAndSave(savePath, template.get(), "[Saved after creating a new result template]");
        if (result) {
            insertInfoLabel(searchToolsLabelTemplateCreationResult, rb.getString("info.successTemplateSaved"), TextColorEnum.GREEN);
            return;
        }
        insertErrorLabel(searchToolsLabelTemplateCreationResult, rb.getString("error.templateNotSaved"));
    }

    @FXML
    private void doBrowseInputPathOne() {
        Optional<String> absPath = HeatVisualizerModalHelper.selectJsonResourceForOpen(rb.getString("label.openResource"), advancedToolsTextFieldMergeInputFile01);
        if (absPath.isPresent()) {
            advancedToolsTextFieldMergeInputFile01.setText(absPath.get());
            removeRedBorder(advancedToolsTextFieldMergeInputFile01);
            return;
        }
        addRedBorder(advancedToolsTextFieldMergeInputFile01);
    }

    @FXML
    private void doBrowseInputPathTwo() {
        Optional<String> absPath = HeatVisualizerModalHelper.selectJsonResourceForOpen(rb.getString("label.openResource"), advancedToolsTextFieldMergeInputFile02);
        if (absPath.isPresent()) {
            advancedToolsTextFieldMergeInputFile02.setText(absPath.get());
            removeRedBorder(advancedToolsTextFieldMergeInputFile02);
            return;
        }
        addRedBorder(advancedToolsTextFieldMergeInputFile02);
    }

    @FXML
    private void doBrowseOutputPath() {
        Optional<String> absPath = HeatVisualizerModalHelper.selectJsonResourceForSave(rb.getString("label.saveTo"), advancedToolsTextFieldMergeOutputFilePath);
        if (absPath.isPresent()) {
            advancedToolsTextFieldMergeOutputFilePath.setText(absPath.get());
            removeRedBorder(advancedToolsTextFieldMergeOutputFilePath);
            return;
        }
        addRedBorder(advancedToolsTextFieldMergeOutputFilePath);
    }

    @FXML
    private void doMergeResultFiles(ActionEvent actionEvent) {
        //Test first input file:
        String pathOne = advancedToolsTextFieldMergeInputFile01.getText();
        if (pathOne.isEmpty() || !pathOne.endsWith(".json")) {
            insertErrorLabelAndRedBorder(searchToolsLabelMergeResult, advancedToolsTextFieldMergeInputFile01, rb.getString("error.path1EmptyOrWrongFormat"));
            return;
        }
        removeRedBorder(advancedToolsTextFieldMergeInputFile01);

        P2PKHSingleResultData[] one = SerializedSearchResultsReader.deserializeExistingSearchResults(advancedToolsTextFieldMergeInputFile01.getText());

        if (one == null) { //eg. FileNotFoundException
            insertErrorLabelAndRedBorder(searchToolsLabelMergeResult, advancedToolsTextFieldMergeInputFile01, rb.getString("error.path1NotAccessible"));
            return;
        }
        removeRedBorder(advancedToolsTextFieldMergeInputFile01);

        //Test second input file:
        String pathTwo = advancedToolsTextFieldMergeInputFile02.getText();
        if (pathTwo.isEmpty() || !pathTwo.endsWith(".json")) {
            insertErrorLabelAndRedBorder(searchToolsLabelMergeResult, advancedToolsTextFieldMergeInputFile02,rb.getString("error.path2EmptyOrWrongFormat"));
            return;
        }
        removeRedBorder(advancedToolsTextFieldMergeInputFile02);

        P2PKHSingleResultData[] two = SerializedSearchResultsReader.deserializeExistingSearchResults(advancedToolsTextFieldMergeInputFile02.getText());

        if (two == null) { //eg. FileNotFoundException
            insertErrorLabelAndRedBorder(searchToolsLabelMergeResult, advancedToolsTextFieldMergeInputFile02, rb.getString("error.path2NotAccessible"));
            return;
        }
        removeRedBorder(advancedToolsTextFieldMergeInputFile02);

        //Merge data structures:
        P2PKHSingleResultData[] merged = P2PKHSingleResultData.merge(one, two);

        //Test output path:
        String outputPath = advancedToolsTextFieldMergeOutputFilePath.getText();
        if (outputPath.isEmpty() || !outputPath.endsWith(".json")) {
            insertErrorLabelAndRedBorder(searchToolsLabelMergeResult, advancedToolsTextFieldMergeOutputFilePath, rb.getString("error.outputPathEmptyOrWrongFormat"));
            return;
        }
        removeRedBorder(advancedToolsTextFieldMergeOutputFilePath);

        //serialize and save
        boolean result = P2PKHSingleResultDataHelper.serializeAndSave(outputPath, merged, "[Saved after result template merge]");

        if (result) {
            insertInfoLabel(searchToolsLabelMergeResult, rb.getString("info.successMergeResultsSavedTo") + advancedToolsTextFieldMergeOutputFilePath.getText(), TextColorEnum.GREEN);
            return;
        }

        insertErrorLabelAndRedBorder(searchToolsLabelMergeResult, advancedToolsTextFieldMergeOutputFilePath, rb.getString("error.mergeResultsNotSaved") + advancedToolsTextFieldMergeOutputFilePath.getText());
    }

    @FXML
    private void doBrowseMultiSourcePath() {
        Optional<String> absPath = HeatVisualizerModalHelper.selectTxtResourceForOpen(rb.getString("label.openResource"), advancedToolsTextFieldMultiSource);
        if (absPath.isPresent()) {
            advancedToolsTextFieldMultiSource.setText(absPath.get());
            removeRedBorder(advancedToolsTextFieldMultiSource);
            return;
        }
        addRedBorder(advancedToolsTextFieldMultiSource);
    }

    @FXML
    private void doBrowseMultiTargetPath() {
        Optional<String> absPath = HeatVisualizerModalHelper.selectTxtResourceForSave(rb.getString("label.saveTo"), advancedToolsTextFieldMultiTarget);
        if (absPath.isPresent()) {
            advancedToolsTextFieldMultiTarget.setText(absPath.get());
            removeRedBorder(advancedToolsTextFieldMultiTarget);
            return;
        }
        addRedBorder(advancedToolsTextFieldMultiTarget);
    }

    @FXML
    private void doUnencodeFile(ActionEvent actionEvent) {
        insertErrorLabel(searchToolsLabelMultiUnencodeResultMessage, rb.getString("error.multiUnencoderDisabled") + HeatVisualizerConstants.CURRENT_VERSION);
        //TODO: will proxy call modified version (needs refactoring) of 'SimpleListFilter.unencodeAndSaveP2PKHAddresses()'

        //show error/success at searchToolsLabelMultiUnencodeResultMessage
        //if error - showError() and addRedBorder() on the TextField
    }

    private Optional<P2PKHSingleResultData[]> createPubTemplateFromManualInput() {
        //TODO: something with limit of addresses (advancedToolsTextFieldMaxEntriesInManualInput)

        // String newLine = System.lineSeparator(); //.txt files on Windows have \n instead of \r\n
        String userInput = advancedToolsTextAreaManualPubInput.getText(); // textArea for user's manual unencoded addresses' input

        if (userInput.isEmpty()) {
            return Optional.empty();
        }

        userInput = userInput.replaceAll("[^0123456789abcdefABCDEF\r\n\t ,]+", HeatVisualizerConstants.EMPTY_STRING);

        String finalUserInput = userInput;

        List<String> inputKeyList = UserInputUtils.findDelimiter(userInput).map(delimiter -> addToMyList(finalUserInput, delimiter)).orElse(Collections.emptyList());
        if (parentController.isVerboseMode()) {
            System.out.println("Found the following keys in user input: " + inputKeyList);
        }

        if (!inputKeyList.isEmpty()) {
            P2PKHSingleResultData[] template = UnencodedPubListReader.readPubsFromStringList(inputKeyList);
            return Optional.of(template);
        }

        return Optional.empty();
    }

    private Optional<P2PKHSingleResultData[]> createPubTemplateFromFile() {
        //TODO: something with limit of addresses (advancedToolsTextFieldMaxEntriesInFile)

        String path = advancedToolsTextFieldPathToPubList.getText();
        int validPubs = UnencodedPubListReader.readAndTestFile(path);

        if (validPubs > 0) {
            // template = UnencodedPubListReader.readPubsFromFileConstrained(path, 3);
            P2PKHSingleResultData[] template = UnencodedPubListReader.readPubsFromFileUnconstrained(path);
            if (parentController.isVerboseMode()) {
                System.out.println("Valid pubs found: " + template.length);
            }
            return Optional.of(template);
        }

        return Optional.empty();
    }

    private List<String> addToMyList(String finalUserInput, String delimiter) {
        if (delimiter == null) {
            return Collections.emptyList();
        }

        List<String> inputKeyList = new ArrayList<>();
        if ("".equals(delimiter)) {
            //maybe we only have 1 valid key
            String possibleKey = finalUserInput.trim();
            if (HeatVisualizerConstants.PATTERN_SIMPLE_40.matcher(possibleKey).matches()) {
                inputKeyList.add(possibleKey);
            }
            return inputKeyList;
        }

        String processed = UserInputUtils.cleanUserInput(finalUserInput, delimiter);

        //allows max 3 valid keys for now
        Arrays.stream(processed.split(delimiter, 4))
                .filter(str -> str != null && HeatVisualizerConstants.PATTERN_SIMPLE_40.matcher(str.trim()).matches())
                .map(str -> str.toLowerCase(Locale.ROOT))
                .map(String::trim)
                .forEach(inputKeyList::add);

        return inputKeyList;
    }

    private void addRedBorder(Control component) {
        if (!component.getStyleClass().contains(CssConstants.BORDER_RED)) {
            component.getStyleClass().add(CssConstants.BORDER_RED);
        }
    }

    private void removeRedBorder(Control component) {
        component.getStyleClass().remove(CssConstants.BORDER_RED);
    }

    private void insertErrorLabelAndRedBorder(Label errorLabelComponent, Control redBorderComponent, String errorText) {
        insertErrorLabel(errorLabelComponent, errorText);
        addRedBorder(redBorderComponent);
    }

    private void insertErrorLabel(Label component, String message) {
        component.getStyleClass().clear();
        component.getStyleClass().add(CssConstants.ERROR_INFO_MESSAGE_STYLE_CLASS);
        component.getStyleClass().add(TextColorEnum.RED.getStyleClass());
        component.setText(message);
    }

    private void insertInfoLabel(Label component, String message, TextColorEnum color) {
        component.getStyleClass().clear();
        component.getStyleClass().add(CssConstants.ERROR_INFO_MESSAGE_STYLE_CLASS);
        component.getStyleClass().add(color.getStyleClass());
        component.setText(message);
    }

    private void removeLabel(Label component) {
        component.getStyleClass().clear();
        component.getStyleClass().add(CssConstants.ERROR_INFO_MESSAGE_STYLE_CLASS);
        component.setText(HeatVisualizerConstants.EMPTY_STRING);
    }
}
