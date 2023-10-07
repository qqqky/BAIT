package com.bearlycattable.bait.bl.controllers.advancedTab;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import com.bearlycattable.bait.bl.controllers.advancedTab.proxyInterfaces.AdvancedLogAccessProxy;
import com.bearlycattable.bait.commons.dataStructures.CustomFixedSizeQueue;
import com.bearlycattable.bait.commons.enums.LogTextTypeEnum;
import com.bearlycattable.bait.utility.BundleUtils;
import com.bearlycattable.bait.utility.DurationUtils;
import com.bearlycattable.bait.utility.LocaleUtils;
import com.bearlycattable.bait.utility.logUtils.LogText;
import com.bearlycattable.bait.utility.logUtils.LogTextFactory;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import lombok.Getter;

public class AdvancedSubTabLogController {

    private static final int MAX_LOG_TEXT_SIZE = 100;
    private static final int MAX_LOG_ENTRIES = 5000;
    private final ResourceBundle rb = ResourceBundle.getBundle(BundleUtils.GLOBAL_BASE_NAME + "AdvancedSubTabLog", LocaleUtils.APP_LANGUAGE);

    private final Set<LogTextTypeEnum> disabledTypes = new HashSet<>();
    private final Queue<LogText> unfilteredQueue = new CustomFixedSizeQueue<>(MAX_LOG_ENTRIES);

    @FXML
    @Getter
    private CheckBox advancedLogCbxInfoStartOfSearch;
    @FXML
    @Getter
    private CheckBox advancedLogCbxInfoEndOfSearch;
    @FXML
    @Getter
    private CheckBox advancedLogCbxShowPointsGained;
    @FXML
    @Getter
    private CheckBox advancedLogCbxShowProgress;
    @FXML
    @Getter
    private CheckBox advancedLogCbxShowKeySwaps;
    @FXML
    @Getter
    private CheckBox advancedLogCbxGeneralMessages;
    @FXML
    @Getter
    private ListView<LogText> advancedLogListView;
    @FXML
    private Button advancedLogBtnClearAll;

    // private AdvancedTabMainController parentController;
    private AdvancedLogAccessProxy advancedLogAccessProxy;

    @FXML
    void initialize() {
        System.out.println("CREATING (child, advanced): AdvancedSubTabLogController......");
    }

    public void setAdvancedLogAccessProxy(AdvancedLogAccessProxy proxy) {
        this.advancedLogAccessProxy = Objects.requireNonNull(proxy);
    }

    synchronized void log(String textPiece, Color color, int size, FontWeight weight, LogTextTypeEnum type) {
        if (textPiece == null || type == null || size < 0 || size > MAX_LOG_TEXT_SIZE) {
            return;
        }

        LogText text = LogTextFactory.build(textPiece, color, size, weight, type);

        if (isTypeDisabled(type)) {
            unfilteredQueue.add(text);
            return;
        }

        while (advancedLogListView.getItems().size() >= MAX_LOG_ENTRIES) {
            advancedLogListView.getItems().remove(0);
        }

        advancedLogListView.getItems().add(text);
        unfilteredQueue.add(text);
    }

    public void addOrRemoveFilterType(CheckBox cbx, LogTextTypeEnum type) {
        if (cbx.isSelected()) {
            disabledTypes.remove(type);
            return;
        }

        disabledTypes.add(type);
    }

    public void filterLog() {
        List<LogText> list = new ArrayList<>(unfilteredQueue);
        list.removeIf(item -> disabledTypes.contains(item.getType()));

        advancedLogListView.getItems().clear();
        advancedLogListView.getItems().addAll(list);
    }

    boolean isTypeDisabled(LogTextTypeEnum type) {
        return disabledTypes.contains(type);
    }

    @FXML
    private void doScrollToBottom() {
        advancedLogListView.scrollTo(advancedLogListView.getItems().size() - 1);
        advancedLogListView.getSelectionModel().select(advancedLogListView.getItems().size() - 1);
    }

    @FXML
    private void doClearAll() {
        int numTotalEntries = unfilteredQueue.size();
        long numLogClearMessages = unfilteredQueue.stream().filter(item -> LogTextTypeEnum.LOG_CLEAR == item.getType()).count();
        if (numTotalEntries - numLogClearMessages == 0) {
            advancedLogBtnClearAll.setDisable(false);
            return;
        }
        advancedLogBtnClearAll.setDisable(true);
        showLogClearConfirmationModal();
        advancedLogBtnClearAll.setDisable(false);
    }

    private void showLogClearConfirmationModal() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.initModality(Modality.APPLICATION_MODAL);
        alert.getDialogPane().setHeaderText(rb.getString("label.warningLogWillBeCleared"));
        alert.getDialogPane().setContentText(rb.getString("label.doYouReallyWantToClear") + System.lineSeparator() + rb.getString("label.logClearMessagesWillNotBeCleared") + System.lineSeparator() + System.lineSeparator());

        //add our default stylesheet for Alert
        alert.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getClassLoader().getResource("com.bearlycattable.bait.ui.css/styles.css")).toExternalForm());

        if (advancedLogAccessProxy.isDarkModeEnabled()) {
            alert.getDialogPane().getStyleClass().add("textRed");
            alert.getDialogPane().getStyleClass().add("alertDark");
        }

        alert.showAndWait().ifPresent(result -> {
            if (ButtonType.OK != result) {
                advancedLogBtnClearAll.setDisable(false);
                return;
            }

            int numTotalEntries = unfilteredQueue.size();
            long numLogClearMessages = unfilteredQueue.stream().filter(item -> LogTextTypeEnum.LOG_CLEAR == item.getType()).count();

            advancedLogListView.getItems().removeIf(item -> LogTextTypeEnum.LOG_CLEAR != item.getType());

            List<LogText> logClearedItems = unfilteredQueue.stream()
                    .filter(logText -> LogTextTypeEnum.LOG_CLEAR == logText.getType())
                    .collect(Collectors.toList());
            unfilteredQueue.clear();
            unfilteredQueue.addAll(logClearedItems);

            log(MessageFormat.format(rb.getString("info.logClearedWithDate"), (numTotalEntries - numLogClearMessages), DurationUtils.getCurrentDateTime()), Color.RED, 0, FontWeight.BOLD, LogTextTypeEnum.LOG_CLEAR);
        });
    }

    public final boolean isParentValid() {
        return advancedLogAccessProxy != null;
    }

    public boolean isDarkModeEnabled() {
        return advancedLogAccessProxy != null && advancedLogAccessProxy.isDarkModeEnabled();
    }
}
