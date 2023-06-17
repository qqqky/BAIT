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

    exports com.bearlycattable.bait.advanced to bait.bl;
    exports com.bearlycattable.bait.advanced.helpers to bait.bl;
    exports com.bearlycattable.bait.advanced.searchHelper;
    exports com.bearlycattable.bait.advanced.context;
    exports com.bearlycattable.bait.advanced.searchHelper.factory;
    exports com.bearlycattable.bait.advanced.searchHelper.impl;
}