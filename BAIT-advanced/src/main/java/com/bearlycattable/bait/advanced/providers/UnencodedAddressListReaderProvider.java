package com.bearlycattable.bait.advanced.providers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.advancedCommons.contexts.P2PKHSingleResultData;

public interface UnencodedAddressListReaderProvider {

    @NonNull
    P2PKHSingleResultData[] createTemplateFromFile(String relativePathToUnencodedPubsFile, int max);

    P2PKHSingleResultData[] createTemplateFromStringList(List<String> unencodedAddresses, int max);

    int readAndTestFile(String pathToUnencodedAddresses);

    Map<String, Object> readUnencodedPubsListIntoMap(String pathToUnencodedAddressesFile);

    Set<String> readUnencodedPubsListIntoSet(String pathToUnencodedAddressesFile);
}
