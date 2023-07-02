package com.bearlycattable.bait.advanced.providerImpls;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.advanced.providers.UnencodedAddressListReaderProvider;
import com.bearlycattable.bait.advancedCommons.contexts.P2PKHSingleResultData;
import com.bearlycattable.bait.commons.HeatVisualizerConstants;

public class UnencodedAddressListReaderProviderImpl implements UnencodedAddressListReaderProvider {

    private static final Logger LOG = Logger.getLogger(UnencodedAddressListReaderProviderImpl.class.getName());

    @Override
    @NonNull
    public synchronized P2PKHSingleResultData[] createTemplateFromFile(String pathToUnencodedAddressesFile, int max) {
        List<P2PKHSingleResultData> list = new ArrayList<>();

        if (max == -1) {
            max = Integer.MAX_VALUE;
        }

        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(pathToUnencodedAddressesFile))) {
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

    @Override
    public Optional<P2PKHSingleResultData[]> createTemplateFromStringList(List<String> unencodedAddresses, int max) {
        if (unencodedAddresses == null || unencodedAddresses.isEmpty()) {
            return Optional.empty();
        }

        if (max == -1) {
            max = Integer.MAX_VALUE;
        }

        List<P2PKHSingleResultData> list = new ArrayList<>();

        unencodedAddresses.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(trimmed -> HeatVisualizerConstants.PATTERN_SIMPLE_40.matcher(trimmed).matches())
                .limit(max)
                .forEach(address -> list.add(P2PKHSingleResultData.createEmptyBackbone(address)));

        return Optional.of(list.toArray(new P2PKHSingleResultData[0]));
    }

    @Override
    @NonNull
    public Map<String, Object> readUnencodedPubsListIntoMap(String pathToUnencodedAddressesFile) {
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

    @Override
    @NonNull
    public Set<String> readUnencodedPubsListIntoSet(String pathToUnencodedAddressesFile) {
        Path pathToUnencodedAddresses = Paths.get(pathToUnencodedAddressesFile);
        Set<String> set = new HashSet<>();

        try (BufferedReader br = Files.newBufferedReader(pathToUnencodedAddresses.toAbsolutePath(), StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                set.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOG.info("Error reading file at UnencodedPubListReader#readUnencodedPubsListIntoMap");
        }

        return set;
    }

    @Override
    public int readAndTestFile(String pathToUnencodedAddresses) {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(pathToUnencodedAddresses))) {
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
}
