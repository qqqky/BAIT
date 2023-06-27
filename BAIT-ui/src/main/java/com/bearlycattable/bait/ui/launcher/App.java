package com.bearlycattable.bait.ui.launcher;

import java.util.Objects;
import java.util.ResourceBundle;

import com.bearlycattable.bait.bl.controllers.RootController;
import com.bearlycattable.bait.commons.HeatVisualizerConstants;
import com.bearlycattable.bait.utility.BundleUtils;
import com.bearlycattable.bait.utility.LocaleUtils;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception  {
        System.out.println("Starting app");
        System.out.println("User dir is: " + System.getProperty("user.dir"));
        final boolean verbose = getParameters().getRaw().contains("-v");
        String bundleBaseName = BundleUtils.GLOBAL_BASE_NAME;

        if (bundleBaseName == null) {
            System.out.println("Could not locate or access required bundles. BAIT will now shut down.");
            Platform.exit();
            return;
        }

        System.out.println("BaseName has been derived for further bundle acquisitions: " + bundleBaseName);

        // Function<String, URL> urlAccessFunction = BundleUtils.MODULAR ?
        //         (path) -> Objects.requireNonNull(ClassLoader.getSystemResource(path)) :
        //         (path) -> getClass().getClassLoader().getResource(path);

        FXMLLoader loader = new FXMLLoader(
                Objects.requireNonNull(getClass().getClassLoader().getResource("com.bearlycattable.bait.ui.fxmls/gui_main.fxml")),
                Objects.requireNonNull(ResourceBundle.getBundle(bundleBaseName + "StaticLabels", LocaleUtils.APP_LANGUAGE)));

        Parent root = loader.load();

        //apply verbose mode if necessary
        if (verbose) {
            loader.<RootController>getController().setVerboseMode(verbose);
            System.out.println("App mode: verbose");
        }

        primaryStage.setTitle("BAIT " + HeatVisualizerConstants.CURRENT_VERSION);
        Scene scene = new Scene(root, 1280, 720); //16:9
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getClassLoader().getResource("com.bearlycattable.bait.ui.css/styles.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    //Possible ways to acquire resources:
    //Objects.requireNonNull(getClass().getResource("/com.bearlycattable.bait.ui.fxmls/gui_main.fxml")); will work with Maven
    //Objects.requireNonNull(getClass().getClassLoader().getResource("com.bearlycattable.bait.ui.fxmls/gui_main.fxml"));

    //for local executions (direct in IDE) we can use this:
    //Objects.requireNonNull(ClassLoader.getSystemResource("com.bearlycattable.bait.ui.fxmls/gui_main.fxml"));

    //and some other ways:
    //Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
    //SomeClass.class.getClassLoader().getResourceAsStream(resource);
}
