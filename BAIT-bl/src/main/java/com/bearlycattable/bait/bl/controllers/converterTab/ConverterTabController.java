package com.bearlycattable.bait.bl.controllers.converterTab;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import com.bearlycattable.bait.bl.controllers.ConverterTabAccessProxy;
import com.bearlycattable.bait.commons.CssConstants;
import com.bearlycattable.bait.commons.BaitConstants;
import com.bearlycattable.bait.commons.enums.TextColorEnum;
import com.bearlycattable.bait.commons.helpers.BaitHelper;
import com.bearlycattable.bait.commons.validators.PrivKeyValidator;
import com.bearlycattable.bait.utility.BundleUtils;
import com.bearlycattable.bait.utility.LocaleUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import lombok.Getter;

public class ConverterTabController {

    private final BaitHelper helper = new BaitHelper();
    private final ResourceBundle rb = ResourceBundle.getBundle(BundleUtils.GLOBAL_BASE_NAME + "ConverterTab", LocaleUtils.APP_LANGUAGE);
    private ConverterTabAccessProxy converterTabAccessProxy;

    @FXML
    @Getter
    private TextField converterConversionTextFieldEncodedPub;
    @FXML
    @Getter
    private TextField converterConversionTextFieldUnencodedPub;
    @FXML
    @Getter
    private TextField converterWIFTextFieldPriv;
    @FXML
    private TextField converterWIFTextFieldResultUncompressed;
    @FXML
    private TextField converterWIFTextFieldResultCompressed;
    @FXML
    private Label converterLabelSuccessErrorResultForEncoding;
    @FXML
    private Label converterLabelSuccessErrorResultForWIF;

    public void setConverterTabAccessProxy(ConverterTabAccessProxy proxy) {
        this.converterTabAccessProxy = proxy;
    }

    @FXML
    void initialize() {
        System.out.println("CREATING: ConverterTabController......");
    }

    @FXML
    private void doConvertToUnencoded() {
        String input = converterConversionTextFieldEncodedPub.getText();

        //P2PKH keys are of length 26-34 (some sources state length 35 is possible)
        if (input.length() < 26 || input.length() > 34) {
            addErrorMessageForEncodingAndRedBorder(rb.getString("error.validPubRequired"), converterConversionTextFieldEncodedPub);
            removeRedBorder(converterConversionTextFieldUnencodedPub);
            return;
        }

        if (!Validator.isValidEncodedPubKey(input)) {
            addErrorMessageForEncodingAndRedBorder(rb.getString("error.invalidPub"), converterConversionTextFieldEncodedPub);
            removeRedBorder(converterConversionTextFieldUnencodedPub);
            return;
        }

        removeRedBorder(converterConversionTextFieldEncodedPub);
        removeRedBorder(converterConversionTextFieldUnencodedPub);
        removeErrorLabelEncoding();

        try {
            converterConversionTextFieldUnencodedPub.setText(helper.decodeFromBase58(input, true));
        } catch (RuntimeException e) {
            addErrorMessageForEncodingAndRedBorder(rb.getString("error.checksumMismatch"), converterConversionTextFieldEncodedPub);
        }
    }

    @FXML
    private void doConvertToEncoded() {
        String input = converterConversionTextFieldUnencodedPub.getText();

        if (input.length() != 40) {
            addErrorMessageForEncodingAndRedBorder(rb.getString("error.validPkhRequired"), converterConversionTextFieldUnencodedPub);
            removeRedBorder(converterConversionTextFieldEncodedPub);
            return;
        }

        String lowerCased = input.toLowerCase(Locale.ROOT);

        if (!Validator.isValidUnencodedPubKey(lowerCased)) {
            addErrorMessageForEncodingAndRedBorder(rb.getString("error.invalidPkh"), converterConversionTextFieldUnencodedPub);
            return;
        }

        removeRedBorder(converterConversionTextFieldUnencodedPub);
        removeRedBorder(converterConversionTextFieldEncodedPub);
        removeErrorLabelEncoding();

        converterConversionTextFieldEncodedPub.setText(helper.encodeToBase58(0, input));
    }

    @FXML
    private void doConvertToWIF() {
        String input = converterWIFTextFieldPriv.getText();

        if (!PrivKeyValidator.isValidPK(input)) {
            addErrorMessageForWIFAndRedBorder(rb.getString("error.invalidPriv"), converterWIFTextFieldPriv);
            return;
        }

        removeRedBorder(converterWIFTextFieldPriv);
        removeErrorLabelWIF();

        converterWIFTextFieldResultUncompressed.setText(helper.getWIF(input, false));
        converterWIFTextFieldResultCompressed.setText(helper.getWIF(input, true));
    }

    private void removeErrorLabelWIF() {
        converterLabelSuccessErrorResultForWIF.setText(BaitConstants.EMPTY_STRING);
    }

    private void removeErrorLabelEncoding() {
       converterLabelSuccessErrorResultForEncoding.setText(BaitConstants.EMPTY_STRING);
    }

    private void addErrorMessageForWIFAndRedBorder(String message, TextInputControl component) {
        insertErrorMessageForWIF(message);
        addRedBorder(component);
    }

    private void addErrorMessageForEncodingAndRedBorder(String message, TextInputControl component) {
        insertErrorMessageForEncoding(message);
        addRedBorder(component);
    }

    private void insertErrorMessageForEncoding(String message) {
        insertErrorMessageLabel(message, converterLabelSuccessErrorResultForEncoding);
    }

    private void insertErrorMessageForWIF(String message) {
        insertErrorMessageLabel(message, converterLabelSuccessErrorResultForWIF);
    }

    private void insertErrorMessageLabel(String message, Label label) {
        if (!label.getStyleClass().contains(TextColorEnum.RED.getStyleClass())) {
            label.getStyleClass().add(TextColorEnum.RED.getStyleClass());
        }
        label.setText(message);
    }

    private void addRedBorder(Control component) {
        if (!component.getStyleClass().contains(CssConstants.BORDER_RED)) {
            component.getStyleClass().add(CssConstants.BORDER_RED);
        }
    }

    private void removeRedBorder(Control component) {
        component.getStyleClass().remove(CssConstants.BORDER_RED);
    }

    public void insertUnencodedPublicKeyToUi(String unencodedPub) {
        converterConversionTextFieldUnencodedPub.setText(unencodedPub);
    }

    public String getUnencodedPublicKeyFromUi() {
        return converterConversionTextFieldUnencodedPub.getText();
    }

    public final boolean isParentValid() {
        return converterTabAccessProxy != null;
    }

    private static class Validator {

        private static final Pattern ENCODED_PUB_PATTERN = Pattern.compile("[123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz]{26,34}");

        public static boolean isValidUnencodedPubKey(String key) {
            return BaitConstants.PATTERN_SIMPLE_40.matcher(key).matches();
        }

        public static boolean isValidEncodedPubKey(String key) {
            return ENCODED_PUB_PATTERN.matcher(key).matches();
       }
    }
}
