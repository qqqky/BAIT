module bait.commons {
    requires bait.resourceBundles;

    requires com.fasterxml.jackson.databind;
    requires java.logging;
    requires javafx.controls;
    requires org.bouncycastle.provider;
    requires jdk.unsupported;   //for sun.misc.Unsafe

    requires static org.checkerframework.checker.qual;
    requires static lombok;

    exports com.bearlycattable.bait.commons to bait.bl, bait.ui, bait.utility, bait.advanced, bait.advancedCommons;
    exports com.bearlycattable.bait.commons.enums to bait.bl, bait.utility, bait.advancedCommons, bait.advanced;
    exports com.bearlycattable.bait.commons.helpers to bait.bl, bait.utility, bait.advancedCommons, bait.advanced;
    exports com.bearlycattable.bait.commons.contexts to bait.bl, bait.advanced, bait.advancedCommons;
    exports com.bearlycattable.bait.commons.other to bait.bl, bait.advanced, bait.advancedCommons;
    exports com.bearlycattable.bait.commons.interfaces to bait.bl, bait.advanced;
    exports com.bearlycattable.bait.commons.serialization to bait.bl;
    exports com.bearlycattable.bait.commons.dataStructures to bait.bl;

    exports com.bearlycattable.bait.commons.functions;
    exports com.bearlycattable.bait.commons.validators to bait.bl, bait.advancedCommons, bait.advanced;
    exports com.bearlycattable.bait.commons.wrappers to bait.advanced, bait.advancedCommons, bait.bl;

    exports com.bearlycattable.bait.commons.extern.guavaExtern;
}