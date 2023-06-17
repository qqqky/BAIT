module bait.ui {
    requires bait.resourceBundles;
    requires bait.commons;
    requires bait.bl;
    requires bait.utility;

    requires javafx.controls;
    requires javafx.fxml;

    opens com.bearlycattable.bait.ui.css;
    opens com.bearlycattable.bait.ui.fxmls;
    opens com.bearlycattable.bait.ui.txts;
    opens com.bearlycattable.bait.ui.sounds;

    exports com.bearlycattable.bait.ui.launcher to javafx.graphics; //mandatory to launch Application

    uses com.bearlycattable.bait.resourceBundles.spi.StaticLabelsProvider;
}