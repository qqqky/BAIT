package com.bearlycattable.bait.ui.launcher;

import javafx.animation.FadeTransition;
import javafx.application.Preloader;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class FadeInPreloader extends Preloader {
    Group topGroup;
    Parent preloaderParent;

    /* Contact interface between application and preloader */
    public interface SharedScene {
        /* Parent node of the application */
        Parent getParentNode();
    }

    private Scene createPreloaderScene() {
        //our preloader is simple static green rectangle
        Rectangle r = new Rectangle(300, 150);
        r.setFill(Color.GREEN);
        preloaderParent = new Group(r);
        topGroup = new Group(preloaderParent);
        return new Scene(topGroup, 300, 150);
    }

    public void start(Stage stage) throws Exception {
        stage.setScene(createPreloaderScene());
        stage.show();
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification evt) {
        if (evt.getType() == StateChangeNotification.Type.BEFORE_START) {
            //its time to start fading into application ...
            SharedScene appScene = (SharedScene) evt.getApplication();
            fadeInTo(appScene.getParentNode());
        }
    }

    private void fadeInTo(Parent p) {
        //add application scene to the preloader group
        // (visualized "behind" preloader at this point)
        //Note: list is back to front
        topGroup.getChildren().add(0, p);

        //setup fade transition for preloader part of scene
        // fade out over 5s
        FadeTransition ft = new FadeTransition(
                Duration.millis(5000),
                preloaderParent);
        ft.setFromValue(1.0);
        ft.setToValue(0.5);
        ft.setOnFinished(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                //After fade is done, remove preloader content
                topGroup.getChildren().remove(preloaderParent);
            }
        });
        ft.play();
    }
}