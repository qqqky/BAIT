package com.bearlycattable.bait.utility.addressModifiers.stringModifiers;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.commons.helpers.BaitHelper;
import com.bearlycattable.bait.utility.RandomAddressGenerator;

public abstract class AbstractModifier {

    protected final RandomAddressGenerator generator = RandomAddressGenerator.getSecureGenerator(64);
    protected final BaitHelper helper = new BaitHelper();

    protected boolean isValidIndex(int inputLength, int index) {
        return index >= 0 && index <= inputLength;
    }

    protected String extractCurrentWordFromAddress(String address, int wordNumber) {
        return address.substring((wordNumber - 1) * 8, ((wordNumber - 1) * 8) + 8);
    }

    protected void clearBuilder(@NonNull StringBuilder sb) {
        sb.delete(0, sb.length());
    }

    protected void replaceBuilderWordAtIndex(@NonNull StringBuilder sb, String currentWord, int highestWordNum) {
        sb.replace((highestWordNum - 1) * 8, ((highestWordNum - 1) * 8) + 8, currentWord);
    }

    protected void appendToBuilder(@NonNull StringBuilder sb, String address) {
        sb.append(address);
    }

    protected int findHighestUnlockedIndex(List<Integer> ignored, int highestUnlockedIndex) {
        return Stream.iterate(1, i -> ++i)
                .map(num -> ignored.contains(num) ? null : num)
                .limit(8)
                .filter(Objects::nonNull)
                .reduce(highestUnlockedIndex, (result, currentNum) -> currentNum > result ? currentNum : result);
    }
}
