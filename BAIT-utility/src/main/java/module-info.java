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

    uses com.bearlycattable.bait.resourceBundles.spi.BundleTestProvider;
}