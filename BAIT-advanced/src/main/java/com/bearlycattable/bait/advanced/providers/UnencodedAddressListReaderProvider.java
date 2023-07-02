package com.bearlycattable.bait.advanced.providers;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.advancedCommons.contexts.P2PKHSingleResultData;

public interface UnencodedAddressListReaderProvider {

    @NonNull
    P2PKHSingleResultData[] createTemplateFromFile(String pathToUnencodedAddressesFile, int max);

    Optional<P2PKHSingleResultData[]> createTemplateFromStringList(List<String> unencodedAddresses, int max);

    @NonNull
    Map<String, Object> readUnencodedPubsListIntoMap(String pathToUnencodedAddressesFile);

    @NonNull
    Set<String> readUnencodedPubsListIntoSet(String pathToUnencodedAddressesFile);

    int readAndTestFile(String pathToUnencodedAddresses);
}
