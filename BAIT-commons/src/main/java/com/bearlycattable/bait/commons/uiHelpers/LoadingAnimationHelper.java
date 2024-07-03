package com.bearlycattable.bait.commons.uiHelpers;

import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;

import javafx.animation.Interpolator;
import javafx.animation.PathTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

public class LoadingAnimationHelper {

    public static Alert createLoadingAlert(@Nullable Window owner) {
        Alert alert = new Alert(Alert.AlertType.NONE);

        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.initModality(Modality.NONE);
        alert.initStyle(StageStyle.TRANSPARENT); //no outer panel with minimize/restore/close buttons
        alert.setGraphic(null); //no default icon on the right side

        // alert.setHeaderText("Loading Header?");
        //We are using AlertType.NONE, which comes without any buttons. We must add at least one button to be able
        // to use alert.close(). So we add the button and hide it:
        alert.getButtonTypes().add(ButtonType.CLOSE);
        alert.getDialogPane().lookupButton(ButtonType.CLOSE).setVisible(false);

        alert.getDialogPane().getScene().setFill(Color.TRANSPARENT);
        alert.getDialogPane().setContent(createLoadingAnimationPane(Color.RED));

        alert.getDialogPane().getStylesheets().add(Objects.requireNonNull(LoadingAnimationHelper.class.getClassLoader().getResource("com.bearlycattable.bait.ui.css/styles.css")).toExternalForm());
        alert.getDialogPane().getStyleClass().add("alertLoading");
        alert.setResizable(false);

        return alert;
    }

    public static Pane createLoadingAnimationPane(@Nullable Color backgroundColor) {
        Pane loadingPane = new Pane();
        // loadingPane.setBorder(new Border(new BorderStroke(Color.VIOLET, BorderStrokeStyle.SOLID, null, null)));
        if (backgroundColor != null) {
            loadingPane.setBackground(new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));
        }
        loadingPane.setMinSize(82, 82);
        loadingPane.setPrefSize(82, 82);

        Color defaultViolet = Color.web("#772ce8");
        Color defaultDark = Color.web("#0e0e11");

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

    private static Node createLoadingLabel() {
        Label loadingLabel = new Label("Loading...");
        loadingLabel.setTextFill(Color.YELLOW);
        loadingLabel.setStyle("-fx-font-size: 20; -fx-alignment: center; -fx-font-weight: bold");
        loadingLabel.setTranslateY(26);
        loadingLabel.setTranslateX(-4);

        return loadingLabel;
    }

    private static void setLinearTransitionFor(Circle actor, Arc path, int durationInMillis) {
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
