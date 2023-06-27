package com.bearlycattable.bait.advanced.addressReader;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.advanced.providers.UnencodedAddressListReaderProvider;
import com.bearlycattable.bait.advancedCommons.contexts.P2PKHSingleResultData;

public class UnencodedAddressListReaderProviderImpl implements UnencodedAddressListReaderProvider {

    @Override
    public synchronized @NonNull P2PKHSingleResultData[] createTemplateFromFile(String relativePathToUnencodedPubsFile, int max) {
        return UnencodedPubListReader.createTemplateFromFile(relativePathToUnencodedPubsFile, max);
    }

    @Override
    public P2PKHSingleResultData[] createTemplateFromStringList(List<String> unencodedAddresses, int max) {
        return UnencodedPubListReader.createTemplateFromStringList(unencodedAddresses, max);
    }

    @Override
    public int readAndTestFile(String pathToUnencodedAddresses) {
        return UnencodedPubListReader.readAndTestFile(pathToUnencodedAddresses);
    }

    @Override
    public Map<String, Object> readUnencodedPubsListIntoMap(String pathToUnencodedAddressesFile) {
        return UnencodedPubListReader.readUnencodedPubsListIntoMap(pathToUnencodedAddressesFile);
    }

    @Override
    public Set<String> readUnencodedPubsListIntoSet(String pathToUnencodedAddressesFile) {
        return UnencodedPubListReader.readUnencodedPubsListIntoSet(pathToUnencodedAddressesFile);
    }
}
