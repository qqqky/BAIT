package com.bearlycattable.bait.commons.dataAccessors;

import java.util.Map;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ThreadComponentDataAccessor {
    private static final String CHILD_WRAPPER = "childWrapper"; //this is the main component
    private static final String PARENT_WRAPPER = "parentWrapper"; //only for parent thread

    private static final String CHILD_INFO_VBOX = "childInfoVBox";
    private static final String CHILD_PROGRESS_HBOX = "childProgressHBox";
    private static final String PARENT_VBOX_ID_PREFIX = "threadProgressParentContainer";
    private static final String SHOW_HIDE_INFO_BTN = "btnShowHideInfo";
    private static final String STOP_THREAD_BTN = "btnStopThread";
    private static final String REMOVE_BTN = "btnRemove";
    private static final String PROGRESS_BAR = "progressBar";
    private static final String PROGRESS_LABEL = "progressLabel";

    private final String threadNum;
    private final Map<String, Map<String, Object>> dataMap;


    private ThreadComponentDataAccessor() {
        throw new IllegalStateException("Cannot construct without data map");
    }

    public ThreadComponentDataAccessor(Map<String, Map<String, Object>> dataMap) {
        if (dataMap.keySet().size() != 1) {
            throw new IllegalStateException("Only 1 parent key must exist");
        }

        this.dataMap = dataMap;
        threadNum = dataMap.keySet().stream().findFirst().get();
    }

    public String getThreadNum() {
        return threadNum;
    }

    /**
     * Button which is responsible for 'Show info'/'Hide info' functionality
     * @return
     */
    public Button getShowHideInfoButton() {
        return cast(dataMap.get(threadNum).get(SHOW_HIDE_INFO_BTN));
    }

    public Button getStopThreadButton() {
        return cast(dataMap.get(threadNum).get(STOP_THREAD_BTN));
    }

    public Button getRemoveButton() {
        return cast(dataMap.get(threadNum).get(REMOVE_BTN));
    }

    public Label getProgressLabel() {
        return cast(dataMap.get(threadNum).get(PROGRESS_LABEL));
    }

    public ProgressBar getProgressBar() {
        return cast(dataMap.get(threadNum).get(PROGRESS_BAR));
    }

    public HBox getChildProgressHBox() {
        return cast(dataMap.get(threadNum).get(CHILD_PROGRESS_HBOX));
    }

    public VBox getChildInfoVBox() {
        return cast(dataMap.get(threadNum).get(CHILD_INFO_VBOX));
    }

    public VBox getChildWrapper() {
        return cast(dataMap.get(threadNum).get(CHILD_WRAPPER));
    }

    public VBox getParentWrapper() {
        return cast(dataMap.get(threadNum).get(PARENT_WRAPPER));
    }

    public void setParentWrapper(VBox parentWrapper) {
        dataMap.get(threadNum).put(PARENT_WRAPPER, parentWrapper);
    }

    public String getParentThreadId() {
        String id = getParentWrapper().getId();
        return id.substring(id.indexOf(PARENT_VBOX_ID_PREFIX) + PARENT_VBOX_ID_PREFIX.length());
    }

    public String buildDebugInfo() {
        return "Thread num: " + threadNum + System.lineSeparator()
                + "ParentHBox id: " + (getChildProgressHBox() == null ? "ParentHBox is null" : getChildProgressHBox().getId()) + System.lineSeparator()
                + "ParentWrapper id: " + (getParentWrapper() == null ? "ParentWrapper is null" : getParentWrapper().getId()) + System.lineSeparator()
                + "Parent thread id: " + getParentThreadId();
    }

    @SuppressWarnings("unchecked")
    private <T> T cast(Object o) {
        return (T) o;
    }
}
