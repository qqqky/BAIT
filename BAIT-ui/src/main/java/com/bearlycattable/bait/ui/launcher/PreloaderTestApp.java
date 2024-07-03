package com.bearlycattable.bait.ui.launcher;

import javafx.animation.Interpolator;
import javafx.animation.PathTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class PreloaderTestApp extends Application
        implements FadeInPreloader.SharedScene {
    private Parent parentNode;
    private Rectangle rect;

    public Parent getParentNode() {
        return parentNode;
    }

    public void init() {
        //prepare application scene
        rect = new Rectangle(0, 0, 40, 40);
        rect.setArcHeight(10);
        rect.setArcWidth(10);
        rect.setFill(Color.ORANGE);
        parentNode = new Group(rect);
    }

    private Button createCloseButton() {
        Button b = new Button("Close");
        b.setMinSize(50,20);
        b.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, null, null)));
        b.setVisible(true);

        // StackPane.setAlignment(b, Pos.BOTTOM_CENTER);

        b.setOnAction(event -> Platform.exit());

        return b;
    }

    public void start(Stage primaryStage) {
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

        HBox margin = new HBox();
        margin.setMinWidth(12);
        margin.setMaxWidth(12);
        // margin.setBorder(new Border(new BorderStroke(Color.GREEN, BorderStrokeStyle.SOLID, null, null)));

        // animationWrapper.setBorder(new Border(new BorderStroke(Color.CYAN, BorderStrokeStyle.SOLID, null, null)));
        animationWrapper.setAlignment(Pos.CENTER);
        animationWrapper.getChildren().add(margin);
        animationWrapper.getChildren().add(createLoadingPane());

        loadingAnimationContainer.getChildren().add(animationWrapper);
        loadingAnimationContainer.getChildren().add(createCloseButton());

        // pane.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, null, null)));
        // pane.setBackground(new Background(new BackgroundFill(Color.web("#abcaaa"), CornerRadii.EMPTY, Insets.EMPTY)));

        //reminder: coordinates are ignored for StackPane

        pane.getChildren().addAll(loadingAnimationContainer);

        Scene scene = new Scene(pane, 600, 600);
        scene.setFill(Color.TRANSPARENT);
        // primaryStage.setOpacity(0.25);
        primaryStage.initStyle(StageStyle.TRANSPARENT); //this means frameless scene (no borders and no minimize/maximize/close buttons)
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Node createLoadingLabel() {
        Label l = new Label("Loading...");
        l.setTextFill(Color.YELLOW);
        l.setStyle("-fx-font-size: 20; -fx-alignment: center; -fx-font-weight: bold");
        l.setTranslateY(26);
        l.setTranslateX(-4);

        return l;
    }

    private Pane createLoadingPane() {
        Pane loadingPane = new Pane();
        loadingPane.setMinSize(82, 82);

        // loadingPane.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, null, null)));
        int innerDotRadius = 3;
        int outerDotRadius = 4;
        int centerXY = 41;

        Arc arcExtra1 = new Arc(centerXY, centerXY, 33, 33, 0, -360);
        Arc arcA = new Arc(centerXY, centerXY, 29, 29, 0, 360);
        Arc arcB = new Arc(centerXY, centerXY, 24, 24, 0, -360);
        Arc arcExtra2 = new Arc(centerXY, centerXY, 20, 20, 0, -360);
        Arc arcC = new Arc(centerXY, centerXY, 17, 17, 180, 360);
        Arc arcD = new Arc(centerXY, centerXY, 12.5, 12.5, 180, -360);
        Arc arcE = new Arc(centerXY, centerXY, 9.5, 9.5, 0, 360);

        arcA.setFill(Color.TRANSPARENT);
        arcB.setFill(Color.TRANSPARENT);
        arcC.setFill(Color.TRANSPARENT);
        arcExtra1.setFill(Color.TRANSPARENT);
        arcExtra2.setFill(Color.TRANSPARENT);
        arcD.setFill(Color.TRANSPARENT);
        arcE.setFill(Color.web("#772ce8"));

        //For testing:
        // arcA.setStroke(Color.BLACK); //this adds an outline for transparent arcs (otherwise it would be invisible)
        // arcB.setStroke(Color.BLACK); //this adds an outline for transparent arcs (otherwise it would be invisible)
        // arcC.setStroke(Color.BLACK); //this adds an outline for transparent arcs (otherwise it would be invisible)
        arcExtra1.setStroke(Color.web("772ce8"));
        arcExtra1.setStrokeWidth(2);
        arcExtra2.setStroke(Color.web("772ce8"));
        arcExtra2.setStrokeWidth(2);

        Circle startCArcA = new Circle(outerDotRadius);
        startCArcA.setFill(Color.web("#0e0e11"));
        Circle startCArcB = new Circle(outerDotRadius);
        startCArcB.setFill(Color.web("#0e0e11"));
        Circle startCArcC = new Circle(innerDotRadius);
        startCArcC.setFill(Color.web("#0e0e11"));
        Circle startCArcD = new Circle(innerDotRadius);
        startCArcD.setFill(Color.web("#0e0e11"));

        loadingPane.getChildren().addAll(arcA, startCArcA, arcB, startCArcB, arcC, startCArcC, arcD, startCArcD, arcE, arcExtra1, arcExtra2, createLoadingLabel());

        setLinearTransitionFor(startCArcA, arcA, 2000);
        setLinearTransitionFor(startCArcB, arcB, 1250);
        setLinearTransitionFor(startCArcC, arcC, 2000);
        setLinearTransitionFor(startCArcD, arcD, 1250);

        // loadingPane.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, null, null)));
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