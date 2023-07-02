package com.bearlycattable.bait.advanced.providerImpls;

import java.util.Optional;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.advanced.providers.AdvancedSearchHelperProvider;
import com.bearlycattable.bait.advancedCommons.contexts.AdvancedSearchHelperCreationContext;
import com.bearlycattable.bait.advanced.searchHelper.factory.AdvancedSearchHelperFactory;
import com.bearlycattable.bait.advancedCommons.interfaces.AdvancedSearchHelper;
import com.bearlycattable.bait.commons.HeatVisualizerConstants;
import com.bearlycattable.bait.commons.enums.SearchModeEnum;

public class AdvancedSearchHelperProviderImpl implements AdvancedSearchHelperProvider {

    @Override
    public Optional<AdvancedSearchHelper> findAdvancedSearchHelper(SearchModeEnum searchMode, @NonNull AdvancedSearchHelperCreationContext creationContext) {
        return AdvancedSearchHelperFactory.findRequestedSearchHelper(searchMode, creationContext, HeatVisualizerConstants.MIXED_SEARCH_SEQUENCE_WITHOUT_RANDOM);
    }

    @Override
    public void updateTargetForExactMatchCheck(@NonNull Set<String> unencodedAddresses, AdvancedSearchHelper advancedSearchHelper) {
        advancedSearchHelper.updateTargetForExactMatchCheck(unencodedAddresses);
    }
}
