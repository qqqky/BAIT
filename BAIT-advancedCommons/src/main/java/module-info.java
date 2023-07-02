module bait.advancedCommons {
    requires bait.resourceBundles;
    requires bait.commons;
    requires bait.utility;

    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires javafx.controls;

    requires static org.checkerframework.checker.qual;
    requires static lombok;
    requires java.logging;
    requires java.desktop;
    requires javafx.fxml; //for 'ShortSoundEffects'

    exports com.bearlycattable.bait.advancedCommons.helpers to bait.bl, bait.advanced;
    exports com.bearlycattable.bait.advancedCommons.validators to bait.bl;
    exports com.bearlycattable.bait.advancedCommons.interfaces to bait.bl, bait.advanced;
    exports com.bearlycattable.bait.advancedCommons.contexts to bait.advanced, bait.bl, com.fasterxml.jackson.databind;
    exports com.bearlycattable.bait.advancedCommons.other to bait.advanced;
    exports com.bearlycattable.bait.advancedCommons.models to bait.bl, bait.advanced;
    exports com.bearlycattable.bait.advancedCommons.serialization to bait.bl, com.fasterxml.jackson.databind;
    exports com.bearlycattable.bait.advancedCommons.wrappers to bait.bl, bait.advanced;
    exports com.bearlycattable.bait.advancedCommons to bait.advanced;
    exports com.bearlycattable.bait.advancedCommons.dataAccessors to bait.advanced, bait.bl;
}