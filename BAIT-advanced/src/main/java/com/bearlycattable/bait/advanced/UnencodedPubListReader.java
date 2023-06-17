package com.bearlycattable.bait.advanced;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.commons.HeatVisualizerConstants;
import com.bearlycattable.bait.advancedCommons.contexts.P2PKHSingleResultData;

public class UnencodedPubListReader {

    private static final Logger LOG = Logger.getLogger(UnencodedPubListReader.class.getName());

    @NonNull
    public static P2PKHSingleResultData[] readPubsFromFileUnconstrained(String relativePathToUnencodedPubsFile) {
        return readPubsFromFile(relativePathToUnencodedPubsFile, Integer.MAX_VALUE);
    }

    @NonNull
    public static P2PKHSingleResultData[] readPubsFromFileConstrained(String relativePathToUnencodedPubsFile, int max) {
        return readPubsFromFile(relativePathToUnencodedPubsFile, max);
    }

    @NonNull
    public static P2PKHSingleResultData[] readPubsFromFile(String relativePathToUnencodedPubsFile, int max) {
        List<P2PKHSingleResultData> list = new ArrayList<>();

        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(relativePathToUnencodedPubsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                count++;
                if (count <= max) {
                    list.add(P2PKHSingleResultData.createEmptyBackbone(line));
                }
            }
        } catch (IOException e) {
            LOG.info("Error reading the list of unencoded pubs from file in UnencodedPubListReader#readPubsFromFile");
        }

        return list.toArray(new P2PKHSingleResultData[0]);
    }

    public static P2PKHSingleResultData[] readPubsFromStringList(List<String> unencodedPubs) {
        if (unencodedPubs == null || unencodedPubs.isEmpty()) {
            return null;
        }

        List<P2PKHSingleResultData> list = new ArrayList<>();

        unencodedPubs.stream()
                .filter(Objects::nonNull)
                .forEach(unformattedPub -> {
                    String formatted = unformattedPub.trim();
                    if (HeatVisualizerConstants.PATTERN_SIMPLE_40.matcher(formatted).matches()) {
                        list.add(P2PKHSingleResultData.createEmptyBackbone(unformattedPub));
                    }
        });

        return list.toArray(new P2PKHSingleResultData[0]);
    }

    public static int readAndTestFile(String pathToUnencodedPubsFile) {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(pathToUnencodedPubsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (HeatVisualizerConstants.PATTERN_SIMPLE_40.matcher(line.trim()).matches()) {
                    count++;
                }
            }
        } catch (IOException e) {
            LOG.info("Error reading the list of unencoded pubs from file in UnencodedPubListReader#readAndTestFile");
        }

        return count;
    }

    public static Map<String, Object> readUnencodedPubsListIntoMap(String pathToUnencodedAddressesFile) {
        Path pathToUnencodedAddresses = Paths.get(pathToUnencodedAddressesFile);
        Map<String, Object> map = new HashMap<>();

        try (BufferedReader br = Files.newBufferedReader(pathToUnencodedAddresses.toAbsolutePath(), StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                map.put(line, null);
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOG.info("Error reading file at UnencodedPubListReader#readUnencodedPubsListIntoMap");
        }

        return map;
    }
}
