package com.bearlycattable.bait.ui.launcher;

import java.util.Objects;
import java.util.ResourceBundle;

import com.bearlycattable.bait.bl.controllers.RootController;
import com.bearlycattable.bait.commons.BaitConstants;
import com.bearlycattable.bait.utility.BundleUtils;
import com.bearlycattable.bait.utility.LocaleUtils;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class App extends Application {

    private Parent root;
    private EventHandler<WindowEvent> handler;

    @Override
    public void init() throws Exception {
        //all heavy init should be done here, not in start()
        //then, preloader would be able to use handleStateChangeNotification

        System.out.println("Initializing app");
        System.out.println("User dir is: " + System.getProperty("user.dir"));
        final boolean verbose = getParameters().getRaw().contains("-v");
        String bundleBaseName = BundleUtils.GLOBAL_BASE_NAME;

        if (bundleBaseName == null) {
            System.err.println("Could not locate or access required bundles. BAIT will now shut down.");
            Platform.exit();
            return;
        }

        if (verbose) {
            System.out.println("BaseName has been derived for further bundle acquisitions: " + bundleBaseName);
        }

        // Function<String, URL> urlAccessFunction = BundleUtils.MODULAR ?
        //         (path) -> Objects.requireNonNull(ClassLoader.getSystemResource(path)) :
        //         (path) -> getClass().getClassLoader().getResource(path);

       FXMLLoader loader = new FXMLLoader(
                Objects.requireNonNull(getClass().getClassLoader().getResource("com.bearlycattable.bait.ui.fxmls/gui_main.fxml")),
                Objects.requireNonNull(ResourceBundle.getBundle(bundleBaseName + "StaticLabels", LocaleUtils.APP_LANGUAGE)));

       root = loader.load();

       loader.<RootController>getController().setVerboseMode(verbose);
       System.out.println("App mode set to: " + (verbose ? "verbose" : "default"));


       handler = createDefaultAppCloseEventHandler(loader);
    }

    private EventHandler<WindowEvent> createDefaultAppCloseEventHandler(FXMLLoader loader) {
        return event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setTitle("Exiting BAIT");
            alert.getDialogPane().setHeaderText("Are you sure you want to quit BAIT?");
            alert.getDialogPane().setContentText("If any searches are still running, the progress will be lost");
            alert.setGraphic(null);

            boolean darkModeEnabled = loader.<RootController>getController().isDarkModeEnabled();
            if (darkModeEnabled) {
                alert.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getClassLoader().getResource("com.bearlycattable.bait.ui.css/styles.css")).toExternalForm());
                alert.getDialogPane().getStyleClass().add("alertDark");
            }

            alert.showAndWait().filter(result -> result != ButtonType.OK)
                    .ifPresent(b -> event.consume()); //cancel the close request unless "OK" button is clicked
        };
    }

    @Override
    public void start(Stage primaryStage) throws Exception  {
        primaryStage.setTitle("BAIT " + BaitConstants.CURRENT_VERSION);
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo/icon.png"))));
        primaryStage.setOnCloseRequest(handler);

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
