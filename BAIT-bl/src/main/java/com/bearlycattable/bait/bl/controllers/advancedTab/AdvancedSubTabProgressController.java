package com.bearlycattable.bait.bl.controllers.advancedTab;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.advancedCommons.dataAccessors.ThreadComponentDataAccessor;
import com.bearlycattable.bait.advancedCommons.helpers.AdvancedSubTabProgressComponentHelper;
import com.bearlycattable.bait.advancedCommons.helpers.DarkModeHelper;
import com.bearlycattable.bait.bl.controllers.advancedTab.proxyInterfaces.AdvancedProgressAccessProxy;
import com.bearlycattable.bait.commons.BaitConstants;
import com.bearlycattable.bait.commons.enums.BackgroundColorEnum;
import com.bearlycattable.bait.commons.enums.TextColorEnum;
import com.bearlycattable.bait.commons.helpers.BaitResourceSelectionModalHelper;
import com.bearlycattable.bait.utility.BundleUtils;
import com.bearlycattable.bait.utility.LocaleUtils;
import com.bearlycattable.bait.utility.RandomAddressGenerator;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;

public class AdvancedSubTabProgressController {

    // private static final String THREAD_IDENTIFIER = "Thread-";
    private static final String CHILD_WRAPPER_PREFIX = "childWrapper";
    private static final String CHILD_PROGRESS_HBOX_PREFIX = "childProgressHBox";
    private static final String CHILD_INFO_VBOX_PREFIX = "childInfoVBox";
    private static final String THREAD_PROGRESS_PARENT_PREFIX = "threadProgressParentContainer";
    private static final String MAIN_ACCORDION_ID = "progressMainAccordion";
    private static final String TITLED_PANE_PREFIX = "progressTitledPane";
    private static final String SCROLL_PANE_PREFIX = "progressScrollPane";

    private static final Logger LOG = Logger.getLogger(AdvancedSubTabProgressController.class.getName());
    private static final RandomAddressGenerator generator = RandomAddressGenerator.getSecureGenerator(8);
    private final ResourceBundle rb = ResourceBundle.getBundle(BundleUtils.GLOBAL_BASE_NAME + "AdvancedSubTabProgress", LocaleUtils.APP_LANGUAGE);

    private AdvancedProgressAccessProxy advancedProgressAccessProxy;

    //thread progress and info
    @FXML
    @Getter
    private VBox advancedProgressVBoxThreadProgressContainer;
    @FXML
    private Label advancedProgressLabelMessageForShowInfo; // 'show info' details

    //automerge
    @FXML
    @Getter
    private CheckBox advancedProgressCbxEnableAutomerge;
    @FXML
    @Getter
    private HBox advancedProgressHBoxAutomergePathParent;
    @FXML
    private TextField advancedProgressTextFieldAutomergePath;
    @FXML
    private Button advancedProgressBtnBrowseAutomergePath;

    //main message
    @FXML
    private Label advancedProgressLabelGeneralFinishMessage; // error/success message when work is finished

    @FXML
    void initialize() {
        System.out.println("CREATING (child, advanced): AdvancedSubTabProgressController......");
    }

    public void setAdvancedProgressAccessProxy(AdvancedProgressAccessProxy proxy) {
        this.advancedProgressAccessProxy = Objects.requireNonNull(proxy);
    }

    @FXML
    private void doBrowseAutomergePath() {
        if (!advancedProgressCbxEnableAutomerge.isSelected()) {
            return;
        }

        BaitResourceSelectionModalHelper.selectJsonResourceForSave(rb.getString("label.saveTo"), advancedProgressTextFieldAutomergePath).ifPresent(absPath -> {
            advancedProgressTextFieldAutomergePath.setText(absPath);
        });
    }

    private void showInfoLabel(String message, TextColorEnum color) {
        advancedProgressLabelGeneralFinishMessage.getStyleClass().clear();
        advancedProgressLabelGeneralFinishMessage.getStyleClass().add(color.getStyleClass());
        advancedProgressLabelGeneralFinishMessage.setText(message);
    }

    private void showErrorLabel(String error) {
        if (!advancedProgressLabelGeneralFinishMessage.getStyleClass().contains(TextColorEnum.RED.getStyleClass())) {
            advancedProgressLabelGeneralFinishMessage.getStyleClass().add(TextColorEnum.RED.getStyleClass());
        }
        advancedProgressLabelGeneralFinishMessage.setText(error);
    }

    Optional<ThreadComponentDataAccessor> addNewThreadProgressContainerToUi(String parentId, String titleMessage) {
        if (parentId == null) {
            return createThreadProgressContainerForParent(titleMessage);
        }

        return createThreadProgressContainerForChild(parentId);
    }

    private Optional<ThreadComponentDataAccessor> createThreadProgressContainerForChild(@NonNull String parentId) {
        Optional<ThreadComponentDataAccessor> maybeAccessor = addChildProgressContainer(parentId);

        maybeAccessor.ifPresent(accessor -> {
            accessor.getRemoveButton().setOnAction(event -> {
                String parentThreadNum = parentId;
                String childThreadNum =  accessor.getThreadNum();
                if (advancedProgressAccessProxy.isBackgroundThreadWorking(childThreadNum)) {
                    advancedProgressLabelMessageForShowInfo.setText("Selected background thread is still working. Cannot remove the component for thread: " + childThreadNum);
                    return;
                }
                removeThreadProgressChildContainer(parentThreadNum, childThreadNum); //remove child
                advancedProgressLabelMessageForShowInfo.setText(BaitConstants.EMPTY_STRING); //remove message
            });
        });

        return maybeAccessor;
    }

    private Optional<ThreadComponentDataAccessor> createThreadProgressContainerForParent(String titleMessage) {
        Optional<ThreadComponentDataAccessor> maybeAccessor = createNewThreadProgressContainer(true); //add as parent
        if (!maybeAccessor.isPresent()) {
            return Optional.empty();
        }
        ThreadComponentDataAccessor accessor = maybeAccessor.get();

        accessor.getRemoveButton().setOnAction(event -> {
            String parentThreadNum = accessor.getThreadNum();
            if (advancedProgressAccessProxy.isBackgroundThreadWorking(parentThreadNum)) {
                advancedProgressLabelMessageForShowInfo.setText("Selected background thread is still working. Cannot remove the component for thread: " + parentThreadNum);
                return;
            }
            removeThreadProgressChildContainer(parentThreadNum, parentThreadNum); //remove parent
            advancedProgressLabelMessageForShowInfo.setText(BaitConstants.EMPTY_STRING); //remove message
        });

        if (isAccordionPresent()) {
            insertChildTitledPaneToAnExistingAccordion(accessor, titleMessage);
        } else {
            insertChildTitledPaneToANewAccordion(accessor, titleMessage);
        }

        return Optional.of(accessor);
    }

    private void insertChildTitledPaneToAnExistingAccordion(ThreadComponentDataAccessor accessor, String titleMessage) {
        Node node = advancedProgressVBoxThreadProgressContainer.lookup("#" + MAIN_ACCORDION_ID);
        if (!(node instanceof Accordion)) {
            throw new IllegalStateException("Component found is not Accordion. Cannot add thread progress container for thread [thread=" + accessor.getThreadNum() + "]");
        }

        TitledPane titledPane = makeDefaultTitledPaneForChild(accessor, titleMessage);
        ((Accordion)node).getPanes().add(titledPane);

        //take care of dark mode styles
        DarkModeHelper.toggleDarkModeForComponent(advancedProgressAccessProxy.isDarkModeEnabled(), titledPane);
    }

    private void insertChildTitledPaneToANewAccordion(ThreadComponentDataAccessor accessor, String titleMessage) {
        Accordion accordion = new Accordion();
        accordion.setMaxHeight(512);
        accordion.setId(MAIN_ACCORDION_ID);

        TitledPane titledPane = makeDefaultTitledPaneForChild(accessor, titleMessage);
        accordion.getPanes().add(titledPane);
        advancedProgressVBoxThreadProgressContainer.getChildren().add(accordion);

        //take care of dark mode styles
        DarkModeHelper.toggleDarkModeForComponent(advancedProgressAccessProxy.isDarkModeEnabled(), accordion);
    }

    // private TitledPane insertParentTitledPaneToAnExistingAccordion(String parentId, String description) {
    //     //insert TitledPane on behalf of original parent thread
    //     TitledPane titledPane = new TitledPane();
    //     titledPane.setText(parentId + " " + description);
    //     titledPane.setId(TITLED_PANE_PREFIX + parentId);
    //
    //     ScrollPane scrollPane = new ScrollPane();
    //     scrollPane.setMinHeight(34);
    //     scrollPane.setFitToWidth(true);
    //     scrollPane.setId(SCROLL_PANE_PREFIX + parentId);
    //
    //     VBox parentWrapper = new VBox();
    //     parentWrapper.setId(THREAD_PROGRESS_PARENT_PREFIX + parentId);
    //
    //     scrollPane.setContent(parentWrapper);
    //     titledPane.setContent(scrollPane);
    //     Node accordion = advancedProgressVBoxThreadProgressContainer.lookup("#" + MAIN_ACCORDION_ID);
    //
    //     if (!(accordion instanceof Accordion)) {
    //         throw new IllegalStateException("Accordion not found at #insertParentTitledPaneToAnExistingAccordion");
    //     }
    //
    //     ((Accordion)accordion).getPanes().add(titledPane);
    //
    //     return titledPane;
    // }

    private TitledPane makeDefaultTitledPaneForChild(ThreadComponentDataAccessor accessor, String infoText) {
        if (accessor.getParentWrapper() == null) {
            throw new IllegalStateException("Parent wrapper not found at #makeDefaultTitledPaneForChild");
        }

        TitledPane titledPane = new TitledPane();
        titledPane.setText("Root: " + accessor.getThreadNum() + (infoText != null ? ", " + infoText : ""));
        titledPane.setId(TITLED_PANE_PREFIX + accessor.getThreadNum());

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setMinHeight(34);
        scrollPane.setFitToWidth(true);
        scrollPane.setId(SCROLL_PANE_PREFIX + accessor.getThreadNum());
        scrollPane.setContent(accessor.getParentWrapper());

        titledPane.setContent(scrollPane);
        return titledPane;
    }

    private boolean isAccordionPresent() {
        return advancedProgressVBoxThreadProgressContainer.getChildren().stream().anyMatch(node -> node instanceof Accordion);
    }

    private Optional<ThreadComponentDataAccessor> addChildProgressContainer(String parentThreadNum) {
        Optional<ThreadComponentDataAccessor> maybeAccessor = createNewThreadProgressContainer(false);

        maybeAccessor.ifPresent(accessor -> {
            String childThreadNum = accessor.getThreadNum();
            if (accessor.getParentWrapper() != null) {
                throw new IllegalStateException("Parent wrapper should not exist for this thread [threadId=" + childThreadNum+"]");
            }

            //add as child and set correct parent wrapper
            Node scrollPane = advancedProgressVBoxThreadProgressContainer.lookup("#" + SCROLL_PANE_PREFIX + parentThreadNum);
            Node mainVBox = ((ScrollPane)scrollPane).getContent();
            if (mainVBox instanceof VBox) {
                VBox parentWrapper = (VBox) mainVBox;
                parentWrapper.getChildren().add(accessor.getChildWrapper());
                accessor.setParentWrapper(parentWrapper);
            }
        });

        return maybeAccessor;
    }

    private Optional<ThreadComponentDataAccessor> createNewThreadProgressContainer(boolean isParentThread) {
        int totalContainersInUi = countThreadProgressContainersInUi();

        //num of total parent thread containers
        if (totalContainersInUi > Runtime.getRuntime().availableProcessors() - 1) {
            showErrorLabel(rb.getString("error.maxContainerLimitReached") + (Runtime.getRuntime().availableProcessors() - 1) + ". Please clean up the UI and try again.");
            return Optional.empty();
        }

        String currentThreadNum = generator.generateHexString(8).toLowerCase(Locale.ROOT);
        ThreadComponentDataAccessor accessor = AdvancedSubTabProgressComponentHelper.createDefaultThreadProgressContainer(currentThreadNum);

        if (advancedProgressAccessProxy != null) {
            DarkModeHelper.toggleDarkModeForComponent(advancedProgressAccessProxy.isDarkModeEnabled(), accessor.getChildWrapper());
        }

        if (isParentThread) {
            VBox parentWrapper = new VBox();
            parentWrapper.setId(THREAD_PROGRESS_PARENT_PREFIX + currentThreadNum);
            parentWrapper.getChildren().add(accessor.getChildWrapper());
            accessor.setParentWrapper(parentWrapper);
        }

        return Optional.of(accessor);
    }

    HBox findChildProgressHBox(String parentThreadId, String childThreadId) {
        Node parent = advancedProgressVBoxThreadProgressContainer.lookup("#" + THREAD_PROGRESS_PARENT_PREFIX + parentThreadId);

        if (!(parent instanceof VBox)) {
            throw new IllegalStateException("Wrong parent received at #getChildProgressHBox");
        }

        Node child = advancedProgressVBoxThreadProgressContainer.lookup("#" + CHILD_PROGRESS_HBOX_PREFIX + childThreadId);

        if (!(child instanceof HBox)) {
            throw new IllegalStateException("Wrong child received at #getChildProgressHBox");
        }

        return (HBox) child;
    }

    VBox findChildInfoVBox(String parentThreadId, String childThreadId) {
        Node parent = advancedProgressVBoxThreadProgressContainer.lookup("#" + THREAD_PROGRESS_PARENT_PREFIX + parentThreadId);

        if (!(parent instanceof VBox)) {
            throw new IllegalStateException("Wrong parent received at #getChildInfoVBox");
        }

        Node child = advancedProgressVBoxThreadProgressContainer.lookup("#" + CHILD_INFO_VBOX_PREFIX + childThreadId);

        if (!(child instanceof VBox)) {
            throw new IllegalStateException("Wrong child received at #getChildInfoVBox");
        }

        return (VBox) child;
    }

    boolean removeThreadProgressContainerFromUi(String threadId) {
        Node node = advancedProgressVBoxThreadProgressContainer.lookup("#" + CHILD_PROGRESS_HBOX_PREFIX + threadId);
        return node != null && advancedProgressVBoxThreadProgressContainer.getChildren().remove(node);
    }

    void setBackgroundColorForProgressHBox(String parentThreadId, String childThreadId, BackgroundColorEnum color) {
        Node threadProgressVBoxWrapper = advancedProgressVBoxThreadProgressContainer.lookup("#threadProgressParentContainer" + parentThreadId);
        if (!(threadProgressVBoxWrapper instanceof VBox)) {
            throw new IllegalStateException("Correct progress container not found! ");
        }

        ((VBox) threadProgressVBoxWrapper).getChildren().stream()
                .filter(VBox.class::isInstance)
                .map(VBox.class::cast)
                .filter(vbox -> vbox.getId().equals(CHILD_WRAPPER_PREFIX + childThreadId))
                .findFirst()
                .flatMap(childWrapper -> childWrapper.getChildren().stream()
                        .filter(HBox.class::isInstance)
                        .map(HBox.class::cast)
                        .filter(hbox -> hbox.getId().equals(CHILD_PROGRESS_HBOX_PREFIX + childThreadId))
                        .findFirst())
                .ifPresent(targetHBox -> changeBcgColorOfProgressHBox(targetHBox, color));
    }

    private void changeBcgColorOfProgressHBox(HBox targetHBox, BackgroundColorEnum color) {
        if (color == null) {
            return;
        }

        if (advancedProgressAccessProxy == null || !advancedProgressAccessProxy.isDarkModeEnabled()) {
            targetHBox.getStyleClass().clear();
            targetHBox.getStyleClass().add(color.getStyleClass());
            if (advancedProgressAccessProxy.isVerboseMode()) {
                System.out.println("Changed color of progress component to: " + color);
            }
            return;
        }

        //remove the default dark mode background style and all other possible
        EnumSet.allOf(BackgroundColorEnum.class).stream()
                .filter(enumColor -> enumColor != color)
                .map(BackgroundColorEnum::getStyleClass)
                .forEach(style -> targetHBox.getStyleClass().remove(style));


        DarkModeHelper.removeStyleClassesFromProgressContainer(targetHBox, Collections.singletonList("bcgPaneDark"));
        targetHBox.getStyleClass().add(color.getStyleClass());
        if (advancedProgressAccessProxy.isVerboseMode()) {
            System.out.println("Changed color of progress component to: " + color);
        }
    }

    boolean removeThreadProgressChildContainer(String parentThreadId, String childThreadId) {
        Node threadProgressVBoxWrapper = advancedProgressVBoxThreadProgressContainer.lookup("#threadProgressParentContainer" + parentThreadId);
        if (!(threadProgressVBoxWrapper instanceof VBox)) {
            return false;
        }

        Optional<VBox> childWrapper = ((VBox) threadProgressVBoxWrapper).getChildren().stream()
                .filter(VBox.class::isInstance)
                .map(VBox.class::cast)
                .filter(vbox -> vbox.getId().equals(CHILD_WRAPPER_PREFIX + childThreadId))
                .findFirst();

        boolean isRemoved = childWrapper.isPresent() && ((VBox) threadProgressVBoxWrapper).getChildren().remove(childWrapper.get());

        if (!isRemoved) {
            return false;
        }

        if (((VBox) threadProgressVBoxWrapper).getChildren().isEmpty()) {
            Node titledPane = advancedProgressVBoxThreadProgressContainer.lookup("#" + TITLED_PANE_PREFIX + parentThreadId);
            Node accordion = advancedProgressVBoxThreadProgressContainer.lookup("#" + MAIN_ACCORDION_ID);

            if (!(accordion instanceof Accordion)) {
                throw new IllegalStateException("Main accordion component not found. Id is corrupted");
            }
            if (!(titledPane instanceof TitledPane)) {
                throw new IllegalStateException("Requested TitledPane could not be found. Id is corrupted");
            }

            boolean titledPaneRemoved = ((Accordion) accordion).getPanes().remove((TitledPane) titledPane);
            if (!titledPaneRemoved) {
                throw new IllegalStateException("TitledPane could not be removed together with the last child. This should never happen");
            }

            if (((Accordion) accordion).getPanes().isEmpty()) {
                Parent parent = accordion.getParent();
                if (!(parent instanceof VBox)) {
                    throw new IllegalStateException("Unexpected parent component of Accordion at #removeThreadProgressChildContainer [Expected: VBox, received: " + parent.getClass() + "]");
                }
                if (((VBox) parent).getChildren().size() != 1) {
                    throw new IllegalStateException("Parent component of Accordion contains too many children. Accordion will not be removed");
                }
                ((VBox) parent).getChildren().clear();
            }
        }

        return true;
    }

    int countThreadProgressContainersInUi() {
        return getAllCurrentThreadIdsInUi().size();
    }

    List<String> getAllCurrentThreadIdsInUi() {
        return advancedProgressVBoxThreadProgressContainer.getChildren().stream()
                .filter(parentContainer -> parentContainer.getId() != null && parentContainer.getId().contains(CHILD_PROGRESS_HBOX_PREFIX))
                .map(container -> container.getId().substring(CHILD_PROGRESS_HBOX_PREFIX.length()))
                .collect(Collectors.toList());
    }

    Optional<String> getAutomergePath() {
        if (!advancedProgressCbxEnableAutomerge.isSelected()) {
            return Optional.empty();
        }

        String path = advancedProgressTextFieldAutomergePath.getText();
        return path.isEmpty() || !path.endsWith(".json") ? Optional.empty() : Optional.of(path);
    }

    void modifyAutomergeAccess(boolean enabled) {
        advancedProgressHBoxAutomergePathParent.setDisable(!enabled);
        advancedProgressCbxEnableAutomerge.setSelected(!enabled);
    }

    void insertErrorOrSuccessMessage(String message, TextColorEnum color) {
        removeAllOtherTextColors(advancedProgressLabelMessageForShowInfo, color);

        if (!advancedProgressLabelMessageForShowInfo.getStyleClass().contains(color.getStyleClass())) {
            advancedProgressLabelMessageForShowInfo.getStyleClass().add(color.getStyleClass());
        }

        advancedProgressLabelMessageForShowInfo.setText(message);
    }

    private void removeAllOtherTextColors(Control component, TextColorEnum color) {
        Arrays.stream(TextColorEnum.values())
                .filter(colorEnum -> colorEnum != color)
                .forEach(otherColor -> component.getStyleClass().remove(otherColor.getStyleClass()));
    }

    boolean isAutomergeEnabled() {
        return advancedProgressCbxEnableAutomerge.isSelected() && !advancedProgressTextFieldAutomergePath.getText().isEmpty();
    }

    public final boolean isParentValid() {
        return advancedProgressAccessProxy != null;
    }

}
