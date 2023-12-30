package com.bearlycattable.bait.ui.launcher;

import java.util.Objects;

import javafx.animation.Interpolator;
import javafx.animation.PathTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class AppPreloader extends Preloader {
    private Stage primaryStage;
    private Pane splashScreenPane;

    @Override
    public void init() {
        splashScreenPane = constructSplashScreenPane();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
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
        //other panes are: TabPane, ScrollPane, BorderPane and probably many others
        Pane pane = new StackPane();
        pane.setBackground(Background.EMPTY);
        // pane.setOpacity(0.2);
        StackPane.setAlignment(pane, Pos.CENTER);

        VBox loadingAnimationContainer = new VBox();
        loadingAnimationContainer.setAlignment(Pos.CENTER);
        HBox animationWrapper = new HBox();
        animationWrapper.setAlignment(Pos.CENTER);
        animationWrapper.getChildren().add(createLoadingPane());

        loadingAnimationContainer.getChildren().add(animationWrapper);

        //For testing:
        // loadingAnimationContainer.getChildren().add(createCloseButton());

        //Other easy to forget stuff:
        // pane.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, null, null)));
        // pane.setBackground(new Background(new BackgroundFill(Color.web("#abcaaa"), CornerRadii.EMPTY, Insets.EMPTY)));

        pane.getChildren().addAll(loadingAnimationContainer);

        return pane;
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification evt) {
        if (evt.getType() == StateChangeNotification.Type.BEFORE_START) {
            primaryStage.hide();
        }
    }

    private Node createLoadingLabel() {
        Label loadingLabel = new Label("Loading...");
        loadingLabel.setTextFill(Color.YELLOW);
        loadingLabel.setStyle("-fx-font-size: 20; -fx-alignment: center;");
        loadingLabel.setTranslateY(26);
        loadingLabel.setTranslateX(-1);

        return loadingLabel;
    }

    private Button createCloseButton() {
        Button closeBtn = new Button("Close");

        closeBtn.setMinSize(50,20);
        closeBtn.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, null, null)));
        closeBtn.setVisible(true);
        closeBtn.setOnAction(event -> Platform.exit());

        return closeBtn;
    }

    private Pane createLoadingPane() {
        Pane loadingPane = new Pane();
        loadingPane.setMinSize(82, 82);

        Color defaultViolet = Color.web("#772ce8");
        Color defaultDark = Color.web("#0e0e11");

        // loadingPane.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, null, null)));
        int innerDotsRadius = 3;
        int outerDotsRadius = 4;
        int centerXY = 41;

        //balls are spinning along the paths of these arcs: arcA, arcB, arcC, arcD
        Arc arcExternalCircle = new Arc(centerXY, centerXY, 33, 33, 0, -360);
        Arc arcA = new Arc(centerXY, centerXY, 29, 29, 0, 360);
        Arc arcB = new Arc(centerXY, centerXY, 24, 24, 0, -360);
        Arc arcInternalCircle = new Arc(centerXY, centerXY, 20, 20, 0, -360);
        Arc arcC = new Arc(centerXY, centerXY, 17, 17, 180, 360);
        Arc arcD = new Arc(centerXY, centerXY, 12.5, 12.5, 180, -360);
        Arc arcMiddleBall = new Arc(centerXY, centerXY, 9.5, 9.5, 0, 360);

        arcA.setFill(Color.TRANSPARENT);
        arcB.setFill(Color.TRANSPARENT);
        arcC.setFill(Color.TRANSPARENT);
        arcExternalCircle.setFill(Color.TRANSPARENT);
        arcInternalCircle.setFill(Color.TRANSPARENT);
        arcD.setFill(Color.TRANSPARENT);
        arcMiddleBall.setFill(defaultViolet);

        //For testing:
        // arcA.setStroke(Color.BLACK); //this adds an outline for transparent arcs (otherwise it would be invisible)
        // arcB.setStroke(Color.BLACK); //this adds an outline for transparent arcs (otherwise it would be invisible)
        // arcC.setStroke(Color.BLACK); //this adds an outline for transparent arcs (otherwise it would be invisible)
        arcExternalCircle.setStroke(defaultViolet);
        arcExternalCircle.setStrokeWidth(2);
        arcInternalCircle.setStroke(defaultViolet);
        arcInternalCircle.setStrokeWidth(2);

        Circle startCArcA = new Circle(outerDotsRadius);
        startCArcA.setFill(defaultDark);
        Circle startCArcB = new Circle(outerDotsRadius);
        startCArcB.setFill(defaultDark);
        Circle startCArcC = new Circle(innerDotsRadius);
        startCArcC.setFill(defaultDark);
        Circle startCArcD = new Circle(innerDotsRadius);
        startCArcD.setFill(defaultDark);

        loadingPane.getChildren().addAll(arcA, startCArcA, arcB, startCArcB, arcC, startCArcC, arcD, startCArcD, arcMiddleBall, arcExternalCircle, arcInternalCircle, createLoadingLabel());

        setLinearTransitionFor(startCArcA, arcA, 2000);
        setLinearTransitionFor(startCArcB, arcB, 1250);
        setLinearTransitionFor(startCArcC, arcC, 2000);
        setLinearTransitionFor(startCArcD, arcD, 1250);

        return loadingPane;
    }

    private void setLinearTransitionFor(Circle actor, Arc path, int durationInMillis) {
        PathTransition pt = new PathTransition();

        pt.setDuration(Duration.millis(durationInMillis));
        pt.setDelay(Duration.ZERO);
        pt.setNode(actor);
        pt.setPath(path);
        pt.setInterpolator(Interpolator.LINEAR); //for smoother transition between animations (otherwise it stops for a while after each cycle)
        // pt.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
        pt.setCycleCount(Timeline.INDEFINITE);
        // pt.setAutoReverse(true);
        pt.play();
    }
}
