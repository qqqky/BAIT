import com.bearlycattable.bait.advanced.providerImpls.AdvancedSearchHelperProviderImpl;
import com.bearlycattable.bait.advanced.providerImpls.UnencodedAddressListReaderProviderImpl;

module bait.advanced {
    requires bait.resourceBundles;
    requires bait.advancedCommons;
    requires bait.commons;

    requires java.logging;
    requires javafx.controls;
    requires javafx.graphics;

    requires static org.checkerframework.checker.qual;
    requires static lombok;
    requires java.desktop;
    requires bait.utility;

    exports com.bearlycattable.bait.advanced.providers to bait.bl;
    exports com.bearlycattable.bait.advanced.providerImpls to bait.bl;
    exports com.bearlycattable.bait.advanced.interfaceImpls to bait.bl;
    exports com.bearlycattable.bait.advanced.interfaces to bait.bl;

    provides com.bearlycattable.bait.advanced.providers.AdvancedSearchHelperProvider with AdvancedSearchHelperProviderImpl;
    provides com.bearlycattable.bait.advanced.providers.UnencodedAddressListReaderProvider with UnencodedAddressListReaderProviderImpl;

    uses com.bearlycattable.bait.resourceBundles.spi.AdvancedTaskControlImplProvider;
}