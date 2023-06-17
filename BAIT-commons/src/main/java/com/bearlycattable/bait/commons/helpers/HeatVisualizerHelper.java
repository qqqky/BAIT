package com.bearlycattable.bait.commons.helpers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;

import com.bearlycattable.bait.commons.HeatVisualizerConstants;
import com.bearlycattable.bait.commons.enums.NumberFormatTypeEnum;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;
import com.bearlycattable.bait.commons.wrappers.PrivHeatResultWrapper;
import com.bearlycattable.bait.commons.wrappers.PrivHeatResultWrapperDecimal;
import com.bearlycattable.bait.commons.wrappers.PrivHeatResultWrapperHex;
import com.bearlycattable.bait.commons.wrappers.PubHeatResultWrapper;

import javafx.event.ActionEvent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

public class HeatVisualizerHelper {

    private final Base58 base58 = new Base58();

    public String getPubKeyHashCompressed(String key, boolean trustInput) {
        if (!trustInput && !isValidKey(key)) {
            return null;
        }
        return bytesToHexString(ECKey.fromPrivate(hexToByteData(key), true).getPubKeyHash());
    }

    public String getPubKeyHashUncompressed(String key, boolean trustInput) {
        if (!trustInput && !isValidKey(key)) {
            return null;
        }
        return bytesToHexString(ECKey.fromPrivate(hexToByteData(key), false).getPubKeyHash());
    }

    public String getWIF(String key, boolean forCompressed) {
        if (!isValidKey(key)) {
            return null;
        }

       return ECKey.fromPrivate(hexToByteData(key), forCompressed).getPrivateKeyAsWiF(NetworkParameters.fromID(NetworkParameters.ID_MAINNET));
    }

    private boolean isValidKey(String key) {
        return HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(key).matches();
    }

    public byte[] hexToByteData(@NonNull String hex) {
        byte[] convertedByteArray = new byte[hex.length() / 2];
        int count = 0;

        for (int i = 0; i < hex.length() - 1; i += 2) {
            String output;
            output = hex.substring(i, (i + 2));
            int decimal = Integer.parseInt(output, 16);
            convertedByteArray[count] = (byte) (decimal & 0xFF);
            count++;
        }
        return convertedByteArray;
    }

    String bytesToHexString(@NonNull byte[] hexbytes) {
        StringBuilder builder = new StringBuilder();
        for (byte a : hexbytes) {
            int i = a & 0xFF;        //apply mask so result is always positive
            if (i < 16) {
                builder.append("0").append(Integer.toHexString(i)); //always want 2 symbols
            } else {
                builder.append(Integer.toHexString(i));
            }
        }
        return builder.toString();
    }

    public String padTo8(String hexInput, boolean uppercase) {
        if (hexInput == null) {
            return null;
        }

        int length = hexInput.length();

        if (length == 8) {
            return uppercase ? hexInput.toUpperCase() : hexInput;
        } else if (length > 8) {
            throw new IllegalStateException("Received hex word is too long [" + hexInput + "] at HeatVisualizerHelper#padTo8");
        }

        StringBuilder sb = new StringBuilder(8);
        sb.append(hexInput);
        while (sb.length() != 8) {
            sb.insert(0, "0");
        }

        String result = sb.toString();
        if (!HeatVisualizerConstants.PATTERN_SIMPLE_08.matcher(result).matches()) {
            return null;
        }

        return uppercase ? result.toUpperCase() : result;
    }

    public String padTo5(String hexInput, boolean uppercase) {
        if (hexInput == null) {
            return null;
        }

        int length = hexInput.length();

        if (length == 5) {
            return uppercase ? hexInput.toUpperCase() : hexInput;
        } else if (length > 5) {
            throw new IllegalStateException("Received hex word is too long [" + hexInput + "] at HeatVisualizerHelper#padTo8");
        }

        StringBuilder sb = new StringBuilder(5);
        sb.append(hexInput);
        while (sb.length() != 5) {
            sb.insert(0, "0");
        }

        String result = sb.toString();
        if (!HeatVisualizerConstants.PATTERN_SIMPLE_05.matcher(result).matches()) {
            return null;
        }

        return uppercase ? result.toUpperCase() : result;
    }

    public static String padTo2(String hexInput, boolean uppercase) {
        if (hexInput == null) {
            return null;
        }

        int length = hexInput.length();

        if (length == 2) {
            return uppercase ? hexInput.toUpperCase() : hexInput;
        } else if (length > 2) {
            throw new IllegalStateException("Received hex word is too long [" + hexInput + "] at HeatVisualizerHelper#padTo2");
        }

        StringBuilder sb = new StringBuilder(2);
        sb.append(hexInput);
        while (sb.length() != 2) {
            sb.insert(0, "0");
        }

        String result = sb.toString();
        if (!HeatVisualizerConstants.PATTERN_SIMPLE_02.matcher(result).matches()) {
            return null;
        }

        return uppercase ? result.toUpperCase() : result;
    }

    public Optional<PrivHeatResultWrapper> calculatePrivHeatResults(Long locked, Long current, Long overflow_reference, NumberFormatTypeEnum requestedType) {
        //eg.: current EEEEEEEE, locked FFFFFFFF
        long difference = current-locked;

        long resultPositive;
        long resultNegative;

        if (difference > 0) { //means negative overflow is lower
            resultPositive = difference;
            resultNegative = overflow_reference - current + locked;
        } else if (current - locked < 0) { //means positive overflow is lower
            resultPositive = overflow_reference - locked + current;
            resultNegative = locked - current;
        } else {
            resultPositive = HeatVisualizerConstants.ZERO_LONG;
            resultNegative = HeatVisualizerConstants.ZERO_LONG;
        }

        if (requestedType == null) {
            return Optional.empty();
        }

        return Optional.of(buildHeatResultsFromCalculatedValues(requestedType, resultPositive, resultNegative));
    }

    public PubHeatResultWrapper calculatePubHeatResults(int reference, int current, int overflow_reference) {
        int difference = current - reference;

        int resultPositive;
        int resultNegative;
        if (difference > 0) { //means negative overflow is lower
            resultPositive = difference;
            resultNegative = overflow_reference - current + reference;
        } else if (current - reference < 0) { //means positive overflow is lower
            resultPositive = overflow_reference - reference + current;
            resultNegative = reference - current;
        } else {
            resultPositive = HeatVisualizerConstants.ZERO_INT;
            resultNegative = HeatVisualizerConstants.ZERO_INT;
        }

        return buildHeatResultsFromCalculatedValues(resultPositive, resultNegative);
    }

    private PrivHeatResultWrapper buildHeatResultsFromCalculatedValues(NumberFormatTypeEnum requestedType, long resultPositive, long resultNegative) {
        switch (requestedType) {
            case HEX:
                return PrivHeatResultWrapperHex.builder()
                        .heatPositive(resultPositive)
                        .heatNegative(resultNegative)
                        .build();
            case DECIMAL:
                return PrivHeatResultWrapperDecimal.builder()
                        .heatPositive(resultPositive)
                        .heatNegative(resultNegative)
                        .build();
            default:
                throw new IllegalArgumentException("Type not supported in #calculatePrivHeatResults [type=" + requestedType + "]");
        }
    }

    private PubHeatResultWrapper buildHeatResultsFromCalculatedValues(int resultPositive, int resultNegative) {
        return PubHeatResultWrapper.builder()
                .heatPositive(resultPositive)
                .heatNegative(resultNegative)
                .build();
    }

    public int determineColorIndex(String hexData, int privAccuracyResolution) {
        if (hexData == null || hexData.length() != 8) {
            throw new IllegalArgumentException("Wrong hex value provided at #determineColor. Must be 8 characters!");
        }

        int index = recursive(hexData, 0, privAccuracyResolution); //start from index 0

        if (privAccuracyResolution > 1 && index < privAccuracyResolution - 1) {
            return 15; //Integer.parseInt(Character.toString('F'), 16); //return max (red)
        }

        return Integer.parseInt(Character.toString(hexData.charAt(index)), 16);
    }

    private int recursive(String hexData, int currentIndex, int privAccuracyResolution) {
        if (currentIndex >= hexData.length() || currentIndex == privAccuracyResolution - 1) {
            return currentIndex;
        }

        if (hexData.charAt(currentIndex) == HeatVisualizerConstants.ZERO_CHARACTER) {
            return recursive(hexData, ++currentIndex, privAccuracyResolution);
        }

        return currentIndex;
    }

    public NumberFormatTypeEnum getNumberFormatFromActionEvent(ActionEvent actionEvent) {
        Object source = actionEvent.getSource();
        NumberFormatTypeEnum selectedNumberFormat = null;
        if (source.getClass().isAssignableFrom(ChoiceBox.class)) {
            ChoiceBox<?> cb = (ChoiceBox<?>) source;
            Object value = cb.getValue();
            if (value.getClass().isAssignableFrom(NumberFormatTypeEnum.class)) {
                selectedNumberFormat = (NumberFormatTypeEnum) value;
            }
        }
        return selectedNumberFormat;
    }

    public int recalculateIndexForSimilarityMappings(int totalPoints, ScaleFactorEnum scaleFactor) {
        if (scaleFactor == null || ScaleFactorEnum.DISABLED == scaleFactor) {
            return totalPoints;
        }

        if (scaleFactor.getMaxPoints().intValue() == totalPoints) {
            return HeatVisualizerConstants.MAX_SIMILARITY_MAP_INDEX.intValue();
        }

        return HeatVisualizerConstants.MAX_SIMILARITY_MAP_INDEX
                .divide(scaleFactor.getMaxPoints(), 10, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(totalPoints)).setScale(0, RoundingMode.HALF_UP).intValue();
    }

    public String getHeatResultFromWrapperData(long wrapperLongResultValue) {
        return padTo8(Long.toHexString(wrapperLongResultValue), true); //convert result to HEX(padded to 8) to map colors correctly
    }

    public static String removeNonBase58Characters(String text) {
        return text.chars().mapToObj(num -> isValidBase58Symbol(num) ? convertCharToString((char)num, false) : "").collect(Collectors.joining(""));
    }

    public static String removeNonIntegerCharacters(String text) {
        return text.chars().mapToObj(num -> isValidInteger(num) ? convertCharToString((char)num, false) : "").collect(Collectors.joining(""));
    }

    public static String removeNonHexCharacters(String text, boolean uppercaseOutput) {
        return text.chars().mapToObj(num ->  isValidHexNum(num) ? convertCharToString((char)num, uppercaseOutput) : "").collect(Collectors.joining(""));
    }

    private static String convertCharToString(char character, boolean uppercase) {
        return uppercase ? Character.toString(character).toUpperCase() : Character.toString(character).toLowerCase();
    }

    private static boolean isValidHexNum(int num) {
       return (num > 47 && num < 58) || (num > 64 && num < 71) || (num > 96 && num < 103);
    }

    private static boolean isValidInteger(int num) {
        return num > 47 && num < 58;
    }
    private static boolean isValidBase58Symbol(int num) {
        return (num > 47 && num < 58) || ((num > 64 && num < 91) && num != 73 && num != 79) || ((num > 96 && num < 123) && num != 108);
    }

    public List<String> processErrorMessage(String messageForUser, int maxWords, int maxWordLength) {
        if (messageForUser == null) {
            return Collections.singletonList(HeatVisualizerConstants.EMPTY_STRING);
        }
        return Arrays.stream(messageForUser.split(" "))
                .limit(8)
                .map(word -> {
                    if (word != null && word.length() > 9) {
                        return word.substring(0, 9);
                    }
                    return word;
                })
                .collect(Collectors.toList());
    }

    public final String encodeToBase58(int versionPrefix, String unencodedHexString) {
       return base58.encodeChecked(versionPrefix, hexToByteData(unencodedHexString));
    }

    public final String decodeFromBase58(String base58String, boolean withChecksumValidation) {
        return bytesToHexString(withChecksumValidation ? base58.decodeChecked(base58String) : base58.decodeUnchecked(base58String));
    }

    public String checkAndRetrievePKH(TextField resultLockedOutputUncompressed, TextField resultLockedOutputCompressed) {
        String one = resultLockedOutputUncompressed.getText();
        String two = resultLockedOutputCompressed.getText();

        if (!HeatVisualizerConstants.PATTERN_SIMPLE_40.matcher(one).matches() || !HeatVisualizerConstants.PATTERN_SIMPLE_40.matcher(two).matches()) {
            throw new IllegalArgumentException("One or more locked PKHs are not valid. Cannot proceed");
        }

        if (!one.equals(two)) {
            throw new IllegalStateException("Locked PKHs are different. They must be the same to proceed with the quickSearch");
        }

        return one;
    }

    public String extractWordFromPKH(String referencePKH, int wordNum) {
        if (referencePKH == null || !(wordNum > 0 && wordNum < 6)) {
            throw new IllegalArgumentException("PKH or requested word is not valid at #extractWordFromPKH");
        }

        return referencePKH.substring((wordNum - 1) * 8, ((wordNum - 1) * 8) + 8);
    }

    public String extractWordFromPK(String referencePK, int wordNum) {
        if (referencePK == null || !(wordNum > 0 && wordNum < 9)) {
            throw new IllegalArgumentException("PKH or requested word is not valid at #extractWordFromPK");
        }

        return referencePK.substring((wordNum - 1) * 8, ((wordNum - 1) * 8) + 8);
    }

    public static Map<String, String> newLowercaseHexMap() {
        Map<String, String> map = new HashMap<>();
        String current;
        for (int i = 0x0; i < 0x10; i++) {
            current = Integer.toHexString(i).toLowerCase(Locale.ROOT);
            map.put(current, current);
        }
        return map;
    }

    public void writeMatchToFile(String priv, String forPKH, String somePath) {
        throw new IllegalStateException("writeMatchToFile not implemented");
    }
}
