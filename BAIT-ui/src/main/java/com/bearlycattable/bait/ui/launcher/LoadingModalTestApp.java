package com.bearlycattable.bait.ui.launcher;

import java.util.Objects;

import com.bearlycattable.bait.commons.uiHelpers.LoadingAnimationHelper;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class LoadingModalTestApp extends Application {

    private Parent root;

    @Override
    public void start(Stage primaryStage) throws Exception {
        root = constructTestCanvas();
        Scene scene = new Scene(root, 300, 300);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getClassLoader().getResource("com.bearlycattable.bait.ui.css/styles.css")).toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    private Pane constructTestCanvas() {
        Pane pane = new StackPane();
        pane.setBackground(new Background(new BackgroundFill(Color.DARKBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        Button b = new Button("Loading splash test");
        b.setOnAction(event -> {
            Alert a = createAlert();
            a.showAndWait();
        });
        pane.getChildren().add(b);

        StackPane.setAlignment(b, Pos.CENTER);
        return pane;
    }

    private Alert createAlert() {
        Alert a = new Alert(Alert.AlertType.NONE); //AlertType.NONE cannot be closed unless we add at least 1 button to it
        a.initModality(Modality.NONE);
        a.initStyle(StageStyle.TRANSPARENT); //no buttons
        a.setGraphic(null); //no default icon on the right side

        // a.setHeaderText("Loading Header?");
        a.getButtonTypes().add(ButtonType.CLOSE);
        a.getDialogPane().lookupButton(ButtonType.CLOSE).setVisible(false);
        a.getDialogPane().getScene().setFill(Color.TRANSPARENT);
        //This one works for a simple text case: a.getDialogPane().setContent(createContentNode());
        a.getDialogPane().setContent(LoadingAnimationHelper.createLoadingAnimationPane(Color.GRAY));
        // a.setContentText("Loading Content?"); //otherwise that area will not be styled at all

        a.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getClassLoader().getResource("com.bearlycattable.bait.ui.css/styles.css")).toExternalForm());
        a.getDialogPane().getStyleClass().add("alertLoading");
        a.setResizable(false);

        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.runLater(a::close);
        });

        a.setOnShowing(dialogEvent -> thread.start());

        return a;
    }

    private Node createContentNode() {
        VBox loadingAnimationContainer = new VBox();
        loadingAnimationContainer.setAlignment(Pos.CENTER);
        HBox animationWrapper = new HBox();
        animationWrapper.setAlignment(Pos.CENTER);
        // animationWrapper.getChildren().add(createLoadingLabel());

        loadingAnimationContainer.setBackground(new Background(new BackgroundFill(Color.DARKSALMON, CornerRadii.EMPTY, Insets.EMPTY)));

        loadingAnimationContainer.getChildren().add(createLoadingLabel());
        return loadingAnimationContainer;
    }

    private Node createLoadingLabel() {
        Label l = new Label("Loading...");
        l.setTextFill(Color.YELLOW);
        l.setStyle("-fx-font-size: 20; -fx-alignment: center;");
        // l.setTranslateY(26);
        // l.setTranslateX(-1);

        return l;
    }

    private DialogPane createDialogPane() {
        DialogPane dp = new DialogPane();
        dp.setHeaderText("Meow");
        dp.setStyle("-fx-background-color: transparent; -fx-fill: transparent");

        VBox vbox = new VBox();
        // vbox.setMinWidth(50);
        vbox.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, null, null)));

        dp.getButtonTypes().add(ButtonType.CLOSE);
        dp.lookupButton(ButtonType.CLOSE).setVisible(false);
        dp.lookup(".button-bar").setStyle("-fx-background-color: transparent; -fx-fill: transparent");

        Label loadingLabel = new Label("Test");
        loadingLabel.setStyle("-fx-font-size: 25;");
        vbox.getChildren().add(loadingLabel);
        dp.setContent(vbox);

        // ButtonBar buttonBar = (ButtonBar)dp.lookup(".button-bar");
        // dp.lookupButton(ButtonType.CLOSE).setVisible(false);

        // Button invisibleCloseButton = new Button("meow");//dialogs cannot be closed normally, unless they have at least 1 button!
        // vbox.getChildren().add(invisibleCloseButton);

        // dp.getChildren().add(vbox);

        return dp;
    }
}
