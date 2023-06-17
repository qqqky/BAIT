module bait.bl {
    requires bait.resourceBundles;
    requires bait.commons;
    requires bait.utility;
    requires bait.advancedCommons;
    requires bait.advanced;

    requires java.desktop;
    requires java.logging;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires static org.checkerframework.checker.qual;
    requires static lombok;

    exports com.bearlycattable.bait.bl.controllers to bait.ui, javafx.fxml;
    opens com.bearlycattable.bait.bl.controllers to javafx.fxml; //allow reflective access of private members

    uses com.bearlycattable.bait.resourceBundles.spi.AdvancedTabPageHelperProvider;
    uses com.bearlycattable.bait.resourceBundles.spi.AdvancedSubTabConfigProvider;
    uses com.bearlycattable.bait.resourceBundles.spi.AdvancedSubTabLogProvider;
    uses com.bearlycattable.bait.resourceBundles.spi.AdvancedSubTabProgressProvider;
    uses com.bearlycattable.bait.resourceBundles.spi.AdvancedSubTabResultsProvider;
    uses com.bearlycattable.bait.resourceBundles.spi.AdvancedSubTabSearchProvider;
    uses com.bearlycattable.bait.resourceBundles.spi.AdvancedSubTabToolsProvider;
    uses com.bearlycattable.bait.resourceBundles.spi.ConstructionTabProvider;
    uses com.bearlycattable.bait.resourceBundles.spi.ConverterTabProvider;
    uses com.bearlycattable.bait.resourceBundles.spi.HeatComparisonTabProvider;
    uses com.bearlycattable.bait.resourceBundles.spi.QuickSearchTabProvider;
}