package com.bearlycattable.bait.advanced.providers;

import java.util.Optional;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.advancedCommons.contexts.AdvancedSearchHelperCreationContext;
import com.bearlycattable.bait.advancedCommons.interfaces.AdvancedSearchHelper;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;

public interface AdvancedSearchHelperProvider {

    Optional<AdvancedSearchHelper> findAdvancedSearchHelper(SearchModeEnum searchMode, @NonNull AdvancedSearchHelperCreationContext creationContext);

    void updateTargetForExactMatchCheck(@NonNull Set<String> unencodedAddresses, AdvancedSearchHelper advancedSearchHelper);
}
