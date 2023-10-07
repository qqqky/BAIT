module bait.utility {
    requires bait.resourceBundles;
    requires bait.commons;

    requires java.logging;
    requires javafx.controls;
    requires javafx.graphics;

    requires static org.checkerframework.checker.qual;
    requires static lombok;

    exports com.bearlycattable.bait.utility to bait.bl, bait.ui, bait.advanced, bait.advancedCommons;
    exports com.bearlycattable.bait.utility.logUtils to bait.bl;
    exports com.bearlycattable.bait.utility.addressModifiers.stringModifiers to bait.advanced, bait.bl;
    exports com.bearlycattable.bait.utility.addressModifiers.byteModifiers to bait.advanced;

    uses com.bearlycattable.bait.resourceBundles.spi.BundleTestProvider;
}