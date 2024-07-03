package com.bearlycattable.bait.commons.contexts;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.bearlycattable.bait.commons.Config;
import com.bearlycattable.bait.commons.enums.QuickSearchComparisonType;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;
import com.bearlycattable.bait.commons.validators.PrivKeyValidator;
import com.bearlycattable.bait.commons.pubKeyComparison.PubComparisonResultSWrapper;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
public class QuickSearchContext {

    private final QuickSearchComparisonType type;
    private final SearchModeEnum searchMode;
    private final @Nullable String targetPriv; //null if type=BLIND
    private final @Nullable String targetPub; //null if type=COLLISION
    @Setter
    private String seed;
    private PubComparisonResultSWrapper currentHighestResult; //for new searches it is PubComparisonResultWrapper.empty()
    private final List<Integer> disabledWords;
    private int accuracy;
    @Setter
    private int iterations;
    private final int printSpacing;
    private final boolean verbose;
    Consumer<String> error;

    //fields only used by random modes which use word prefix
    @Setter
    String wordPrefixForRandomMode;

    @NonNull
    public Optional<String> validate() {
        if (type == null) {
            return Optional.of("Comparison type is required for QuickSearchContext");
        }
        if (searchMode == null) {
            return Optional.of("Search mode is required for QuickSearchContext");
        }
        if (type == QuickSearchComparisonType.COLLISION && !PrivKeyValidator.isValidPK(targetPriv)) {
            return Optional.of("Target key must be valid for COLLISION search");
        }
        if (accuracy < 0 || accuracy > 100) {
            return Optional.of("Invalid accuracy");
        }
        if (iterations < 0 || iterations > Config.MAX_ITERATIONS_QUICK_SEARCH) {
            return Optional.of("Invalid number of iterations provided or exceeds max allowed: " + Config.MAX_ITERATIONS_QUICK_SEARCH);
        }
        if (printSpacing < 1) {
            return Optional.of("Print spacing cannot be less than 1");
        }
        if (QuickSearchComparisonType.COLLISION == type && (targetPriv == null || targetPriv.length() != 64)) {
            return Optional.of("Target priv is not valid for type " + type);
        }
        if (QuickSearchComparisonType.BLIND == type && (targetPub == null || targetPub.length() != 40)) {
            return Optional.of("Target pub is not valid for type " + type);
        }

        if (!SearchModeEnum.isRandomRelatedMode(searchMode) && (seed == null || seed.length() != 64)) {
            return Optional.of("Selected search mode requires a 64-hex digit seed");
        }

        if (SearchModeEnum.ROTATION_FULL == searchMode || SearchModeEnum.ROTATION_FULL_WITH_HEADER == searchMode) {
            if (disabledWords != null && !disabledWords.isEmpty()) {
                return Optional.of("There must be no disabled words for this search mode");
            }
        }

        if (disabledWords != null && disabledWords.stream().anyMatch(i -> i < 1 || i > 8)) {
            return Optional.of("Disabled words list contains illegal data. Here's the list: " + disabledWords);
        }

        return Optional.empty();
    }
}
