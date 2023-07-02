import com.bearlycattable.bait.resourceBundles.spi.AdvancedTaskControlImplProvider;
import com.bearlycattable.bait.resourceBundles.spi.GeneralResourceBundleImplProvider;

module bait.resourceBundles {
    exports com.bearlycattable.bait.resourceBundles.spi;

    //bundle providers
    provides com.bearlycattable.bait.resourceBundles.spi.BundleTestProvider with GeneralResourceBundleImplProvider;

    provides AdvancedTaskControlImplProvider with GeneralResourceBundleImplProvider;

    provides com.bearlycattable.bait.resourceBundles.spi.AdvancedSubTabConfigProvider with GeneralResourceBundleImplProvider;
    provides com.bearlycattable.bait.resourceBundles.spi.AdvancedSubTabLogProvider with GeneralResourceBundleImplProvider;
    provides com.bearlycattable.bait.resourceBundles.spi.AdvancedSubTabProgressProvider with GeneralResourceBundleImplProvider;
    provides com.bearlycattable.bait.resourceBundles.spi.AdvancedSubTabResultsProvider with GeneralResourceBundleImplProvider;
    provides com.bearlycattable.bait.resourceBundles.spi.AdvancedSubTabSearchProvider with GeneralResourceBundleImplProvider;
    provides com.bearlycattable.bait.resourceBundles.spi.AdvancedSubTabToolsProvider with GeneralResourceBundleImplProvider;

    provides com.bearlycattable.bait.resourceBundles.spi.ConstructionTabProvider with GeneralResourceBundleImplProvider;
    provides com.bearlycattable.bait.resourceBundles.spi.ConverterTabProvider with GeneralResourceBundleImplProvider;
    provides com.bearlycattable.bait.resourceBundles.spi.HeatComparisonTabProvider with GeneralResourceBundleImplProvider;
    provides com.bearlycattable.bait.resourceBundles.spi.QuickSearchTabProvider with GeneralResourceBundleImplProvider;
    provides com.bearlycattable.bait.resourceBundles.spi.StaticLabelsProvider with GeneralResourceBundleImplProvider;
}