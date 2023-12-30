package com.bearlycattable.bait.commons.helpers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.commons.HeatVisualizerConstants;
import com.bearlycattable.bait.commons.enums.NumberFormatTypeEnum;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;
import com.bearlycattable.bait.commons.extern.bitcoinjExtern.ECKeyLite;
import com.bearlycattable.bait.commons.extern.bitcoinjExtern.LazyECPoint;
import com.bearlycattable.bait.commons.extern.bitcoinjExtern.addresses.legacyEncoding.Base58;
import com.bearlycattable.bait.commons.wrappers.PrivHeatResultWrapper;
import com.bearlycattable.bait.commons.wrappers.PrivHeatResultWrapperDecimal;
import com.bearlycattable.bait.commons.wrappers.PrivHeatResultWrapperHex;
import com.bearlycattable.bait.commons.wrappers.PubHeatResultWrapper;

import javafx.event.ActionEvent;
import javafx.scene.control.ChoiceBox;

public class HeatVisualizerHelper {

    private final Base58 base58 = new Base58();
    private final byte[] ALL_BYTES_MIN = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2};
    private final byte[] ALL_BYTES_MAX = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -2, -70, -82, -36, -26, -81, 72, -96, 59, -65, -46, 94, -116, -48, 54, 65, 64};

    public String getPubKeyHashCompressed(String key, boolean trustInput) {
        if (!trustInput && !isValidKey(key)) {
            return null;
        }
        BigInteger asBigInt = new BigInteger(1, hexToByteData(key));
        ECKeyLite ecKeyLite = new ECKeyLite(asBigInt, new LazyECPoint(ECKeyLite.publicPointFromPrivate(asBigInt), true));

        return bytesToHexString(ecKeyLite.getPubKeyHash());
    }

    public String getPubKeyHashCompressed(byte[] key, boolean trustInput) {
        if (!trustInput && !isValidKey(key)) {
            return null;
        }

        BigInteger asBigInt = new BigInteger(1, key);
        ECKeyLite ecKeyLite = new ECKeyLite(asBigInt, new LazyECPoint(ECKeyLite.publicPointFromPrivate(asBigInt), true));

        return bytesToHexString(ecKeyLite.getPubKeyHash());
    }

    public byte[] getPubKeyHashCompressedBytes(byte[] key, boolean trustInput) {
        if (!trustInput && !isValidKey(key)) {
            return null;
        }

        BigInteger asBigInt = new BigInteger(1, key);
        ECKeyLite ecKeyLite = new ECKeyLite(asBigInt, new LazyECPoint(ECKeyLite.publicPointFromPrivate(asBigInt), true));

        return ecKeyLite.getPubKeyHash();
    }

    public void loadPubKeyHashCompressedBytes(byte[] key, byte[] result, boolean trustInput) {
        if (!trustInput && !isValidKey(key)) {
            return;
        }

        BigInteger asBigInt = new BigInteger(1, key);
        ECKeyLite ecKeyLite = new ECKeyLite(asBigInt, new LazyECPoint(ECKeyLite.publicPointFromPrivate(asBigInt), true));

        System.arraycopy(ecKeyLite.getPubKeyHash(), 0, result, 0, 40);
    }

    public String getPubKeyHashUncompressed(String key, boolean trustInput) {
        if (!trustInput && !isValidKey(key)) {
            return null;
        }
        BigInteger asBigInt = new BigInteger(1, hexToByteData(key));
        ECKeyLite ecKeyLite = new ECKeyLite(asBigInt, new LazyECPoint(ECKeyLite.publicPointFromPrivate(asBigInt), false));

        return bytesToHexString(ecKeyLite.getPubKeyHash());
    }

    public String getPubKeyHashUncompressed(byte[] key, boolean trustInput) {
        if (!trustInput && !isValidKey(key)) {
            return null;
        }
        BigInteger asBigInt = new BigInteger(1, key);
        ECKeyLite ecKeyLite = new ECKeyLite(asBigInt, new LazyECPoint(ECKeyLite.publicPointFromPrivate(asBigInt), false));

        return bytesToHexString(ecKeyLite.getPubKeyHash());
    }

    public byte[] getPubKeyHashUncompressedBytes(byte[] key, boolean trustInput) {
        if (!trustInput && !isValidKey(key)) {
            return null;
        }
        BigInteger asBigInt = new BigInteger(1, key);
        ECKeyLite ecKeyLite = new ECKeyLite(asBigInt, new LazyECPoint(ECKeyLite.publicPointFromPrivate(asBigInt), false));

        return ecKeyLite.getPubKeyHash();
    }

    public void loadPubKeyHashUncompressedBytes(byte[] key, byte[] result, boolean trustInput) {
        if (!trustInput && !isValidKey(key)) {
            return;
        }
        BigInteger asBigInt = new BigInteger(1, key);
        ECKeyLite ecKeyLite = new ECKeyLite(asBigInt, new LazyECPoint(ECKeyLite.publicPointFromPrivate(asBigInt), false));

        System.arraycopy(ecKeyLite.getPubKeyHash(), 0, result, 0, 40);
    }

    /**
     * Encode private key to WIF (without clutter). Only for Bitcoin MainNet
     * @param key - private key as String
     * @param forCompressed - whether WIF will be used for compressed or uncompressed derivation of public key
     * @return
     */
    public String getWIF(String key, boolean forCompressed) {
        if (!isValidKey(key)) {
            return null;
        }
        Base58 myBase58 = new Base58();

        byte[] originalBytes = hexToByteData(key);

        if (forCompressed) {
            byte[] result = new byte[33];
            System.arraycopy(originalBytes, 0, result, 0, originalBytes.length);
            result[32] = 1;
            return myBase58.encodeChecked(128, result);
        }
        //"DumpedPrivateKey header" (also called 'version') for mainnet is int 128
        return myBase58.encodeChecked(128, originalBytes);
    }

    /**
     * Decodes WIF (a base58 encoded private key) to get the original private key
     * @param WIF - WIF key as String
     * @return
     */
    public String getPrivateKeyFromWIF(String WIF) {
        //If WIF is intended to be used for derivation of compressed public key - the resulting bytes will have a marker
        // (01 byte at the end) that indicates that their corresponding public key should be compressed
        byte[] result = new Base58().decodeChecked(WIF);

        return bytesToHexString(Arrays.copyOfRange(result, 0, result.length == 33 ? result.length - 1 : result.length));
    }

    private boolean isValidKey(String key) {
        return HeatVisualizerConstants.PATTERN_SIMPLE_64.matcher(key).matches();
    }

    private boolean isValidKey(byte[] bytes) {
        if (bytes == null || bytes.length != 32) {
            return false;
        }

        final boolean isMinContender = bytes[0] == 0;
        byte current;

        for (int i = 0; i < bytes.length; i++) {
            current = bytes[i];

            if (isMinContender) {
                if (current == ALL_BYTES_MIN[i]) {
                    continue;
                }
                return i != 31 || (current != 0 && current != 1);
            }

            if (current == ALL_BYTES_MAX[i]) {
                continue;
            }
            //0 is the lowest number (0 -> 127 -> -128 -> -1 -> 0)
            //-1 is the biggest number, so valid nums must be lower
            int target = ALL_BYTES_MAX[i];
            return target < 0 ? (current < target || current > -1) : (current >= 0 && current < target);
        }

        return true;
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

    /**
     * Pad (leading) input string with "0" up to required output length
     * @param hexInput - must be a valid 1-8 character hex string
     * @param uppercase - whether output should be uppercase
     * @return
     */
    public String padToX(String hexInput, int padTo, boolean uppercase) {
        if (padTo < 1 || padTo > 8) {
            throw new IllegalArgumentException("Length of requested padding must be between 1 and 8");
        }
        if (hexInput == null || !HeatVisualizerConstants.PATTERN_HEX_01_TO_08.matcher(hexInput).matches()) {
            return null;
        }

        int length = hexInput.length();

        if (length == padTo) {
            return uppercase ? hexInput.toUpperCase() : hexInput;
        }

        StringBuilder sb = new StringBuilder(padTo);
        sb.append(hexInput);
        while (sb.length() != padTo) {
            sb.insert(0, "0");
        }

        return uppercase ? sb.toString().toUpperCase() : sb.toString();
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
        return padToX(Long.toHexString(wrapperLongResultValue), 8, true); //convert result to HEX(padded to 8) to map colors correctly
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
        return uppercase ? Character.toString(character).toUpperCase() : Character.toString(character);
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

    public final String encodeToBase58(int versionPrefix, String unencodedHexString) {
       return base58.encodeChecked(versionPrefix, hexToByteData(unencodedHexString));
    }

    public final String decodeFromBase58(String base58String, boolean withChecksumValidation) {
        return bytesToHexString(withChecksumValidation ? base58.decodeChecked(base58String) : base58.decodeUnchecked(base58String));
    }

    public static String extractWordFromPKH(String referencePKH, int wordNum) {
        if (referencePKH == null || !(wordNum > 0 && wordNum < 6)) {
            throw new IllegalArgumentException("PKH or requested word is not valid at #extractWordFromPKH");
        }

        return referencePKH.substring((wordNum - 1) * 8, ((wordNum - 1) * 8) + 8);
    }

    public static String extractWordFromPK(String referencePK, int wordNum) {
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
}
