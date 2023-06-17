package com.bearlycattable.bait.advanced.context;

import java.nio.file.Path;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ListFilterContext {

    private final Path absolutePathToOriginalAddressListTsv;
    private final Path saveToPathForFilteredAddresses;
    private final Path saveToPathForBareUnencodedAddresses;
    private final String outputFileExtension; //eg. ".txt"
    private final String fileNamePrefixForFilteredAddressesFile;
    private final String fileNamePrefixForBareUnencodedAddressesFile;
    private final long filterToleranceInSats;
    private final int scale; //eg.: scale 5 would show 5 decimal places when sats are denominated in BTC
    private final int limit; //max num of addresses to be filtered
    private final boolean tolerancePostfixInBtc; //if true, adds postfix in BTC, rather than in sats.
    private final boolean balanceRetainedInFilteredOutput;

    public static class ListFilterContextBuilder {
        private long filterToleranceInSats;
        private int scale;

        public ListFilterContextBuilder filterToleranceInSats(long filterToleranceInSats) {
            if (filterToleranceInSats < 0) {
                throw new IllegalStateException("Tolerance cannot be less than 0");
            }
            this.filterToleranceInSats = filterToleranceInSats;
            return this;
        }

        public ListFilterContextBuilder scale(int scale) {
            if (scale < 1 || scale > 10) {
                throw new IllegalStateException("Scale cannot be less than 1 or more than 10");
            }
            this.scale = scale;
            return this;
        }
    }
}
