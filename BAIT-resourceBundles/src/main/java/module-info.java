import com.bearlycattable.bait.resourceBundles.spi.GeneralResourceBundleProviderImpl;

module bait.resourceBundles {
    exports com.bearlycattable.bait.resourceBundles.spi;

    //bundle providers
    provides com.bearlycattable.bait.resourceBundles.spi.BundleTestProvider with GeneralResourceBundleProviderImpl;

    provides com.bearlycattable.bait.resourceBundles.spi.AdvancedTabPageHelperProvider with GeneralResourceBundleProviderImpl;

    provides com.bearlycattable.bait.resourceBundles.spi.AdvancedSubTabConfigProvider with GeneralResourceBundleProviderImpl;
    provides com.bearlycattable.bait.resourceBundles.spi.AdvancedSubTabLogProvider with GeneralResourceBundleProviderImpl;
    provides com.bearlycattable.bait.resourceBundles.spi.AdvancedSubTabProgressProvider with GeneralResourceBundleProviderImpl;
    provides com.bearlycattable.bait.resourceBundles.spi.AdvancedSubTabResultsProvider with GeneralResourceBundleProviderImpl;
    provides com.bearlycattable.bait.resourceBundles.spi.AdvancedSubTabSearchProvider with GeneralResourceBundleProviderImpl;
    provides com.bearlycattable.bait.resourceBundles.spi.AdvancedSubTabToolsProvider with GeneralResourceBundleProviderImpl;

    provides com.bearlycattable.bait.resourceBundles.spi.ConstructionTabProvider with GeneralResourceBundleProviderImpl;
    provides com.bearlycattable.bait.resourceBundles.spi.ConverterTabProvider with GeneralResourceBundleProviderImpl;
    provides com.bearlycattable.bait.resourceBundles.spi.HeatComparisonTabProvider with GeneralResourceBundleProviderImpl;
    provides com.bearlycattable.bait.resourceBundles.spi.QuickSearchTabProvider with GeneralResourceBundleProviderImpl;
    provides com.bearlycattable.bait.resourceBundles.spi.StaticLabelsProvider with GeneralResourceBundleProviderImpl;
}