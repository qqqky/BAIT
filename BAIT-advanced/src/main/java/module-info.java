module bait.advanced {
    requires bait.resourceBundles;
    requires bait.advancedCommons;
    requires bait.commons;

    requires java.logging;
    requires javafx.graphics;

    requires static org.checkerframework.checker.qual;
    requires static lombok;
    requires java.desktop;
    requires bait.utility;

    exports com.bearlycattable.bait.advanced.providers to bait.bl;

    provides com.bearlycattable.bait.advanced.providers.AdvancedSearchHelperProvider with com.bearlycattable.bait.advanced.searchHelper.providerImpl.AdvancedSearchHelperProviderImpl;
    provides com.bearlycattable.bait.advanced.providers.UnencodedAddressListReaderProvider with com.bearlycattable.bait.advanced.addressReader.UnencodedAddressListReaderProviderImpl;
}