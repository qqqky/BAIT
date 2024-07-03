package com.bearlycattable.bait.ui.launcher;

import com.bearlycattable.bait.commons.uiHelpers.LoadingAnimationHelper;

import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class AppPreloader extends Preloader {
    private Stage primaryStage;
    private Pane splashScreenPane;

    @Override
    public void init() {
        splashScreenPane = constructSplashScreenPane();
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        Scene scene = new Scene(splashScreenPane, 300, 300);
        scene.setFill(Color.TRANSPARENT);
        primaryStage.initStyle(StageStyle.TRANSPARENT); //this means frameless scene (no borders and no minimize/maximize/close buttons)
        primaryStage.setScene(scene);
        primaryStage.show();
        System.out.println("Splash screen should now be displayed!");
    }

    private Pane constructSplashScreenPane() {
        //Pane and AnchorPane allow absolute positioning (0,0 is top left corner), StackPane ignores it
        //and position must be set in a traditional way by setting alignment of each object
        //via a static method: StackPane.setAlignment(myObject, Pos.TOP_LEFT);
        //other panes are: TabPane, ScrollPane, BorderPane, DialogPane and probably many others
        Pane pane = new StackPane();
        pane.setBackground(Background.EMPTY);
        StackPane.setAlignment(pane, Pos.CENTER);

        VBox loadingAnimationContainer = new VBox();
        loadingAnimationContainer.setAlignment(Pos.CENTER);

        HBox animationWrapper = new HBox();
        animationWrapper.setAlignment(Pos.CENTER);

        HBox margin = new HBox();
        margin.setMinWidth(12);
        margin.setMaxWidth(12);

        animationWrapper.getChildren().add(margin);
        animationWrapper.getChildren().add(LoadingAnimationHelper.createLoadingAnimationPane(Color.TRANSPARENT));

        loadingAnimationContainer.getChildren().add(animationWrapper);

        //For testing:
        // loadingAnimationContainer.getChildren().add(createCloseButton());

        //Other easy to forget stuff:
        // pane.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, null, null)));
        // pane.setBackground(new Background(new BackgroundFill(Color.web("#abcaaa"), CornerRadii.EMPTY, Insets.EMPTY)));
        // label.setStyle("-fx-text-fill: goldenrod; -fx-font: italic 20 \"serif\"; -fx-padding: 0 0 20 0; -fx-text-alignment: center");

        pane.getChildren().addAll(loadingAnimationContainer);

        return pane;
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification evt) {
        if (evt.getType() == StateChangeNotification.Type.BEFORE_START) {
            primaryStage.hide();
        }
    }

    private Button createCloseButton() {
        Button closeBtn = new Button("Close");

        closeBtn.setMinSize(50,20);
        closeBtn.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, null, null)));
        closeBtn.setVisible(true);
        // closeBtn.setAlignment(Pos.CENTER);
        closeBtn.setOnAction(event -> Platform.exit());

        return closeBtn;
    }
}
