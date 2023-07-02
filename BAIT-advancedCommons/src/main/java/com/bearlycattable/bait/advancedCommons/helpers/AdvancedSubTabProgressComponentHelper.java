package com.bearlycattable.bait.advancedCommons.helpers;

import java.util.HashMap;
import java.util.Map;

import com.bearlycattable.bait.advancedCommons.dataAccessors.ThreadComponentDataAccessor;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AdvancedSubTabProgressComponentHelper {

    private static final String THREAD_IDENTIFIER = "Thread-";
    private static final String CHILD_WRAPPER_PREFIX = "childWrapper";
    private static final String CHILD_PROGRESS_HBOX_PREFIX = "childProgressHBox";
    private static final String CHILD_INFO_VBOX_PREFIX = "childInfoVBox";
    // private static final String THREAD_PROGRESS_PARENT_PREFIX = "threadProgressParentContainer";
    // private static final String MAIN_ACCORDION_ID = "progressMainAccordion";
    // private static final String TITLED_PANE_PREFIX = "progressTitledPane";
    // private static final String SCROLL_PANE_PREFIX = "progressScrollPane";

    private static final String SHOW_HIDE_INFO_BTN = "btnShowHideInfo";
    private static final String REMOVE_BTN = "btnRemove";
    private static final String STOP_THREAD_BTN = "btnStopThread";
    private static final String PROGRESS_BAR = "progressBar";
    private static final String PROGRESS_LABEL = "progressLabel";

    public static ThreadComponentDataAccessor createDefaultThreadProgressContainer(String currentThreadNum) {
        Map<String, Map<String, Object>> handles = new HashMap<>();
        handles.put(currentThreadNum, new HashMap<>());

        //main VBox of every new thread - wraps HBox for progress/nav components and VBox for info components
        VBox childWrapper = new VBox();
        childWrapper.setId(CHILD_WRAPPER_PREFIX + currentThreadNum); //used for removal
        handles.get(currentThreadNum).put(CHILD_WRAPPER_PREFIX, childWrapper);

        //main HBox for progress/nav components (goes into generalThreadDataParent)
        HBox threadProgressHBox = new HBox();
        threadProgressHBox.setPadding(new Insets(2,0,2,0));
        threadProgressHBox.setId(CHILD_PROGRESS_HBOX_PREFIX + currentThreadNum); //used for removal
        threadProgressHBox.setAlignment(Pos.CENTER);
        threadProgressHBox.setPrefHeight(38.0);
        threadProgressHBox.setPrefWidth(1226.0);
        handles.get(currentThreadNum).put(CHILD_PROGRESS_HBOX_PREFIX, threadProgressHBox);

        //create a child progressBarHbox and its children
        HBox progressBarHbox = new HBox();
        progressBarHbox.setAlignment(Pos.CENTER);
        progressBarHbox.setPrefHeight(71.0);
        progressBarHbox.setPrefWidth(855.0);

        Label threadNameLabel = new Label();
        threadNameLabel.setAlignment(Pos.CENTER_LEFT);
        threadNameLabel.setPrefHeight(32.0);
        threadNameLabel.setPrefWidth(120.0);
        threadNameLabel.setText(THREAD_IDENTIFIER + currentThreadNum);

        progressBarHbox.getChildren().add(threadNameLabel);
        progressBarHbox.getChildren().add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(5, true));

        ProgressBar progressBar = new ProgressBar();
        progressBar.setProgress(0);
        progressBar.setPrefHeight(32.0);
        progressBar.setPrefWidth(670.0);
        // progressBar.setId("browseResultsProgressBar" + currentThreadNum);
        handles.get(currentThreadNum).put(PROGRESS_BAR, progressBar);
        progressBarHbox.getChildren().add(progressBar);

        //label will display current progress in %
        Label threadProgressPercentLabel = new Label();
        threadProgressPercentLabel.setAlignment(Pos.CENTER);
        threadProgressPercentLabel.setPrefHeight(32.0);
        threadProgressPercentLabel.setPrefWidth(60.0);
        handles.get(currentThreadNum).put(PROGRESS_LABEL, threadProgressPercentLabel);
        progressBarHbox.getChildren().add(threadProgressPercentLabel);

        threadProgressHBox.getChildren().add(progressBarHbox);

        //create a child button (ShowInfo). Listener is set later when thread starts work
        Button btnShowHideInfo = new Button();
        btnShowHideInfo.setPrefHeight(32.0);
        btnShowHideInfo.setText("Show info");
        // btnShowHideInfo.setId("progressBtnShowHideInfo" + THREAD_IDENTIFIER + currentThreadNum);
        btnShowHideInfo.setDisable(true);
        handles.get(currentThreadNum).put(SHOW_HIDE_INFO_BTN, btnShowHideInfo);
        threadProgressHBox.getChildren().add(btnShowHideInfo);

        //spacer HBox
        threadProgressHBox.getChildren().add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(5, false));

        //create a child button (StopThread). Listener is set later when thread starts work
        Button btnStopThread = new Button();
        btnStopThread.setPrefHeight(32.0);
        btnStopThread.setText("Stop thread");
        btnStopThread.setDisable(true);
        handles.get(currentThreadNum).put(STOP_THREAD_BTN, btnStopThread);
        threadProgressHBox.getChildren().add(btnStopThread);

        //spacer HBox
        threadProgressHBox.getChildren().add(HeatVisualizerComponentHelper.createEmptyHBoxSpacer(5, false));

        //create a child button (Remove). Listener is set later when thread starts work
        Button btnRemove = new Button();
        btnRemove.setPrefHeight(32.0);
        btnRemove.setText("Remove");
        // btnRemove.setId("progressBtnRemove" + currentThreadNum);
        btnRemove.setDisable(true);
        handles.get(currentThreadNum).put(REMOVE_BTN, btnRemove);
        threadProgressHBox.getChildren().add(btnRemove);

        //VBox for info components (goes into generalThreadDataParent)
        VBox threadInfoVBox = new VBox();
        threadInfoVBox.setId(CHILD_INFO_VBOX_PREFIX + currentThreadNum); //used for removal?
        threadInfoVBox.setAlignment(Pos.CENTER);
        threadInfoVBox.setPrefWidth(1226.0);
        handles.get(currentThreadNum).put(CHILD_INFO_VBOX_PREFIX, threadInfoVBox);

        childWrapper.getChildren().add(threadProgressHBox);
        childWrapper.getChildren().add(threadInfoVBox);

        return new ThreadComponentDataAccessor(handles);
    }
}
