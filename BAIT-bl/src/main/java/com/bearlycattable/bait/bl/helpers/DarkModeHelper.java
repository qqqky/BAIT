package com.bearlycattable.bait.bl.helpers;

import java.util.List;
import java.util.function.BiConsumer;

import com.bearlycattable.bait.bl.controllers.advancedTab.AdvancedTabMainController;

import javafx.css.Styleable;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PopupControl;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class DarkModeHelper {

    private static final String specialLabelTooltipNormal = "specialLabelTooltipNormal";
    private static final String specialLabelTooltipDark = "specialLabelTooltipDark";
    private static final String labelErrorInfo = "errorInfoMessage";

    private static final String mainTabPaneNormal = "mainTabPaneNormal";
    private static final String mainTabPaneDark = "mainTabPaneDark";
    private static final String mainTabPaneTabNormal = "mainTabPaneTabNormal";
    private static final String mainTabPaneTabDark = "mainTabPaneTabDark";

    private static final String tabAdvancedMainPane = "tabAdvancedMainPane";
    private static final String tabAdvancedMainPaneDark = "tabAdvancedMainPaneDark";
    private static final String subTabAdvancedNormal = "subTabAdvancedNormal";
    private static final String subTabAdvancedDark = "subTabAdvancedDark";

    private static final String bcgPaneDark = "bcgPaneDark";
    private static final String scrollPaneDark = "scrollPaneDark";

    private static final String topicMarkerMain = "topicMarkerMain";
    private static final String topicMarkerMainDark = "topicMarkerMainDark";
    private static final String topicMarkerMainLabelTextDark = "topicMarkerMainLabelTextDark";
    private static final String topicMarkerAdditional = "topicMarkerAdditional";
    private static final String topicMarkerAdditionalDark = "topicMarkerAdditionalDark";
    private static final String topicMarkerAdditionalLabelTextDark = "topicMarkerAdditionalLabelTextDark";

    private static final String tabTopSpacer = "tabTopSpacer";
    private static final String tabTopSpacerDark = "tabTopSpacerDark";

    private static final String buttonDark = "btnDark";
    private static final String textFieldAndTextAreaDark = "textFieldAndTextAreaDarkMode";
    private static final String cbxDark = "cbxDark";
    private static final String radioDark = "radioDark";
    private static final String comboBoxDark = "comboBoxDark";
    private static final String spinnerDark = "spinnerDark";
    private static final String choiceBoxDark = "choiceBoxDark";
    private static final String progressBarDark = "progressBarDark";
    private static final String listViewDark = "listViewDark";
    private static final String accordionDark = "accordionDark";
    private static final String titledPaneDark = "titledPaneDark";
    private static final String alertDark = "alertDark";

    //ids of ignored special components (from heat comparison tab)
    private static final String containerReferenceUncompressed = "containerReferenceUncompressed";
    private static final String containerReferenceCompressed = "containerReferenceCompressed";

    //other classes that are ignored (from heat comparison tab)
    private static final String heatContainerPrivWords = "heatContainerPrivWords";
    private static final String heatContainerPrivWordsDark = "heatContainerPrivWordsDark";
    private static final String privHeatNumericValue = "privHeatNumericValue";
    private static final String privHeatNumericValueDark = "privHeatNumericValueDark";

    public static synchronized void toggleDarkModeGlobal(boolean enable, Control component, AdvancedTabMainController controller) {
        if (component == null) {
            return;
        }

        Node root = component;
        while (root.getParent() != null) {
            root = root.getParent();
        }

        if (!(root instanceof VBox)) {
            return;
        }

        VBox rootVBox = (VBox) root;
        addOrRemoveStyle(rootVBox, bcgPaneDark, enable);
        rootVBox.getChildren().stream().forEach(child -> darkModeHelper(child, enable));

        // System.out.println(root);
        controller.setDarkMode(enable);
        controller.refreshLogView();
        // System.out.println("Dark mode has been " + (enable ? "ENABLED" : "DISABLED"));
    }

    public static void toggleDarkModeForComponent(boolean enable, Node component) {
        darkModeHelper(component, enable);
        // System.out.println("Dark mode has been " + (enable ? "ENABLED" : "DISABLED") + " on this component: " + component);
    }

    public static void removeStyleClassesFromProgressContainer(HBox component, List<String> styleClasses) {
        if (component == null) {
            return;
        }

        styleClasses.stream().forEach(styleClass -> removeStyleClassIfExists(component, styleClass));
        component.getChildren().stream().forEach(child -> removeStyleClassesFromProgressContainerHelper(child, styleClasses));
    }

    private static void removeStyleClassesFromProgressContainerHelper(Node component, List<String> styleClasses) {
        if (component == null) {
            return;
        }

        if (component instanceof Label) {
            styleClasses.stream().forEach(styleClass -> removeStyleClassIfExists(component, styleClass));
            return;
        }

        if (component instanceof Pane) {
            styleClasses.stream().forEach(styleClass -> removeStyleClassIfExists(component, styleClass));
            ((Pane) component).getChildren().stream().forEach(child -> removeStyleClassesFromProgressContainerHelper(child, styleClasses));
        }
    }

    private static void darkModeHelper(Object current, boolean enable) {
        if (!(current instanceof Styleable)) {
            return;
        }

        if (current instanceof Tab) {
            processTabComponent((Tab) current, enable);
        } else if (current instanceof Pane) {
            processPaneComponent((Pane) current, enable);
        } else if (current instanceof Control) {
            processControlComponent((Control) current, enable);
        } else {
            throw new IllegalStateException("WTF???");
        }

    }

    private static void processTabComponent(Tab currentTab, boolean enable) {
        //process tabs of main TabPane
        if (currentTab.getStyleClass().contains(enable ? mainTabPaneTabNormal : mainTabPaneTabDark)) {
            removeStyleClassIfExists(currentTab, enable ? mainTabPaneTabNormal : mainTabPaneTabDark);
            addStyleClassIfNotExists(currentTab, enable ? mainTabPaneTabDark : mainTabPaneTabNormal);
            darkModeHelper(currentTab.getContent(), enable);
            return;
        }

        //process tabs inside 'Advanced' tab's TabPane
        if (currentTab.getStyleClass().contains(enable ? subTabAdvancedNormal : subTabAdvancedDark)) {
            removeStyleClassIfExists(currentTab, enable ? subTabAdvancedNormal : subTabAdvancedDark);
            addStyleClassIfNotExists(currentTab, enable ? subTabAdvancedDark : subTabAdvancedNormal);
            darkModeHelper(currentTab.getContent(), enable);
            return;
        }

        darkModeHelper(currentTab.getContent(), enable);
    }

    private static void processPaneComponent(Pane currentPane, boolean enable) {
        if (currentPane instanceof HBox) {
            processHBoxComponent((HBox) currentPane, enable);
            return;
        }
        if (currentPane instanceof VBox) {
            processVBoxComponent((VBox) currentPane, enable);
            return;
        }

        // processing of DialogPane and its children (including Grid and StackPane) is not needed
        // as separate stylesheet must be added for each created Alert(DialogPane), so we might as
        // well just add the needed class there, making Alert a 'special case'
        // if (currentPane instanceof DialogPane) {
        //     processDialogPaneComponent((DialogPane) currentPane, enable);
        //     return;
        // }

        //process any other Pane component
        Pane item = cast(currentPane);
        addOrRemoveStyle(item, bcgPaneDark, enable);
        item.getChildren().stream().forEach(child -> darkModeHelper(child, enable));
    }

    private static void processControlComponent(Control currentControl, boolean enable) {
        if (currentControl instanceof TabPane) {
            TabPane tp = cast(currentControl);
            //process parent TabPane
            if (tp.getStyleClass().contains(enable ? mainTabPaneNormal : mainTabPaneDark)) {
                removeStyleClassIfExists(tp, enable ? mainTabPaneNormal : mainTabPaneDark);
                addStyleClassIfNotExists(tp, enable ? mainTabPaneDark : mainTabPaneNormal);
            }
            //process TabPane inside 'Advanced' tab
            if (tp.getStyleClass().contains(enable ? tabAdvancedMainPane : tabAdvancedMainPaneDark)) {
                removeStyleClassIfExists(tp, enable ? tabAdvancedMainPane : tabAdvancedMainPaneDark);
                addStyleClassIfNotExists(tp, enable ? tabAdvancedMainPaneDark : tabAdvancedMainPane);
            }
            tp.getTabs().stream().forEach(tab -> darkModeHelper(tab, enable));
            return;
        }

        if (currentControl instanceof Button) {
            addOrRemoveStyle(currentControl, buttonDark, enable);
            return;
        }

        if (currentControl instanceof TextField) {
            addOrRemoveStyle(currentControl, textFieldAndTextAreaDark, enable);
            return;
        }

        if (currentControl instanceof TextArea) {
            addOrRemoveStyle(currentControl, textFieldAndTextAreaDark, enable);
            return;
        }

        if (currentControl instanceof Label) {
            Label l = cast(currentControl);
            if ("?".equals(l.getText())) { //will assume it's our standard tooltip
                removeStyleClassIfExists(l, enable ? specialLabelTooltipNormal : specialLabelTooltipDark);
                addStyleClassIfNotExists(l, enable ? specialLabelTooltipDark : specialLabelTooltipNormal);
                return;
            }
            if (l.getStyleClass().contains(labelErrorInfo)) {
                // addOrRemoveStyle(l, bcgPaneDark, enable);
                //ignore labels that are used for success/error display in tabs
                return;
            }
            if (l.getStyleClass().contains(enable ? privHeatNumericValue : privHeatNumericValueDark)) {
                removeStyleClassIfExists(l, enable ? privHeatNumericValue : privHeatNumericValueDark);
                addStyleClassIfNotExists(l, enable ? privHeatNumericValueDark : privHeatNumericValue);
                return;
            }
            addOrRemoveStyle(currentControl, bcgPaneDark, enable);
            return;
        }

        if (currentControl instanceof ComboBox) {
            addOrRemoveStyle(currentControl, comboBoxDark, enable);
            return;
        }

        if (currentControl instanceof CheckBox) {
            addOrRemoveStyle(currentControl, cbxDark, enable);
            return;
        }

        if (currentControl instanceof RadioButton) {
            addOrRemoveStyle(currentControl, radioDark, enable);
            return;
        }

        if (currentControl instanceof Spinner) {
            addOrRemoveStyle(currentControl, spinnerDark, enable);
            return;
        }

        if (currentControl instanceof ChoiceBox) {
            addOrRemoveStyle(currentControl, choiceBoxDark, enable);
            return; //id=comparisonChoiceBoxNumberFormatType, quickSearchChoiceBoxSearchMode, advancedSearchChoiceBoxSearchMode
        }

        if (currentControl instanceof ProgressBar) {
            addOrRemoveStyle(currentControl, progressBarDark, enable);
            return;
        }

        if (currentControl instanceof ScrollPane) {
            ScrollPane sp = cast(currentControl);
            addOrRemoveStyle(sp, scrollPaneDark, enable);
            darkModeHelper(sp.getContent(), enable);
            return;
        }

        if (currentControl instanceof ListView) {
            addOrRemoveStyle(currentControl, listViewDark, enable);
            return;
        }

        if (currentControl instanceof Accordion) {
            Accordion ac = cast(currentControl);
            addOrRemoveStyle(ac, accordionDark, enable);
            ac.getPanes().stream().forEach(pane -> darkModeHelper(pane, enable));
            return;
        }

        if (currentControl instanceof TitledPane) {
            TitledPane tp = cast(currentControl);
            addOrRemoveStyle(tp, titledPaneDark, enable);
            darkModeHelper(tp.getContent(), enable);
            return;
        }

        //children components of Alert class (children of DialogPane)
        if (currentControl instanceof ButtonBar) {
            ButtonBar bb = cast(currentControl);
            bb.getButtons().stream().forEach(btn -> darkModeHelper(btn, enable));
            return;
        }

        Control item = cast(currentControl);
        addOrRemoveStyle(item, bcgPaneDark, enable);
    }

    /**
     * Add style class to 'enable', remove style class to 'disable'
     * @param node
     * @param styleClass
     * @param enable
     */
    private static void addOrRemoveStyle(Node node, String styleClass, boolean enable) {
        BiConsumer<Node,String> biConsumer;

        if (enable) {
            biConsumer = (n, s) -> addStyleClassIfNotExists(node, styleClass);
        } else {
            biConsumer = (n, s) -> removeStyleClassIfExists(node, styleClass);
        }

        biConsumer.accept(node, styleClass);
    }

    private static void addOrRemoveStyle(PopupControl popup, String styleClass, boolean enable) {
        BiConsumer<PopupControl, String> biConsumer;

        if (enable) {
            biConsumer = (n, s) -> addStyleClassIfNotExists(popup, styleClass);
        } else {
            biConsumer = (n, s) -> removeStyleClassIfExists(popup, styleClass);
        }

        biConsumer.accept(popup, styleClass);
    }

    private static void addDesiredRemoveUndesiredStyle(Node node, String desiredStyle, String undesiredStyle) {
        if (!node.getStyleClass().contains(desiredStyle)) {
            addStyleClassIfNotExists(node, desiredStyle);
        }
        removeStyleClassIfExists(node, undesiredStyle);
    }

    private static void processHBoxComponent(HBox hb, boolean enable) {
        //default labels of 'reference' PKH in 'Comparison' tab
        if (containerReferenceUncompressed.equals(hb.getId()) || containerReferenceCompressed.equals(hb.getId())) {
            return;
        }

        //default styles for priv comparison containers in 'Comparison' tab
        if (hb.getStyleClass().contains(enable ? heatContainerPrivWords : heatContainerPrivWordsDark)) {
            removeStyleClassIfExists(hb, enable ? heatContainerPrivWords : heatContainerPrivWordsDark);
            addStyleClassIfNotExists(hb, enable ? heatContainerPrivWordsDark : heatContainerPrivWords);
            return;
        }

        if (hb.getStyleClass().contains(enable ? topicMarkerMain : topicMarkerMainDark)) {
            removeStyleClassIfExists(hb, enable ? topicMarkerMain : topicMarkerMainDark);
            addStyleClassIfNotExists(hb, enable ? topicMarkerMainDark : topicMarkerMain);
            hb.getChildren().stream().forEach(child -> darkModeHelperSpecialLabelOnly(child, topicMarkerMainLabelTextDark, enable));
            return;
        }

        if (hb.getStyleClass().contains(enable ? topicMarkerAdditional : topicMarkerAdditionalDark)) {
            removeStyleClassIfExists(hb, enable ? topicMarkerAdditional : topicMarkerAdditionalDark);
            addStyleClassIfNotExists(hb, enable ? topicMarkerAdditionalDark : topicMarkerAdditional);
            hb.getChildren().stream().forEach(child -> darkModeHelperSpecialLabelOnly(child, topicMarkerAdditionalLabelTextDark, enable));
            return;
        }

        addOrRemoveStyle(hb, bcgPaneDark, enable);
        hb.getChildren().stream().forEach(child -> darkModeHelper(child, enable));
    }

    private static void processVBoxComponent(VBox vb, boolean enable) {
        if (vb.getStyleClass().contains(enable ? tabTopSpacer : tabTopSpacerDark)) {
            removeStyleClassIfExists(vb, enable ? tabTopSpacer : tabTopSpacerDark);
            addStyleClassIfNotExists(vb, enable ? tabTopSpacerDark : tabTopSpacer);
            vb.getChildren().stream().forEach(child -> darkModeHelper(child, enable));
            return;
        }

        addOrRemoveStyle(vb, bcgPaneDark, enable);
        vb.getChildren().stream().forEach(child -> darkModeHelper(child, enable));
    }

    private static void processDialogPaneComponent(DialogPane cp, boolean enable) {
        addOrRemoveStyle(cp, alertDark, enable);
        cp.getChildren().stream().forEach(child -> darkModeHelper(child, enable));
    }


    //only used for topic and subtopic components. Here we only process labels (including special label-tooltips)
    private static void darkModeHelperSpecialLabelOnly(Object current, String styleClass, boolean enable) {
        if (current instanceof Pane) {
            ((Pane) current).getChildren().stream().forEach(child -> darkModeHelperSpecialLabelOnly(child, styleClass, enable));
            return;
        }
        if (!(current instanceof Label)) {
            return;
        }

        Label l = cast(current);
        if ("?".equals(l.getText())) { //will assume it's our standard tooltip
            removeStyleClassIfExists(l, enable ? specialLabelTooltipNormal : specialLabelTooltipDark);
            addStyleClassIfNotExists(l, enable ? specialLabelTooltipDark : specialLabelTooltipNormal);
            return;
        }

        addOrRemoveStyle(l, styleClass, enable);
    }

    private static void removeStyleClassIfExists(Tab component, String styleClass) {
        component.getStyleClass().remove(styleClass);
    }

    private static void removeStyleClassIfExists(Pane component, String styleClass) {
        component.getStyleClass().remove(styleClass);
    }

    private static void removeStyleClassIfExists(Control component, String styleClass) {
        component.getStyleClass().remove(styleClass);
    }

    private static void removeStyleClassIfExists(Object node, String styleClass) {
        if (node instanceof Control) {
            removeStyleClassIfExists((Control) node, styleClass);
            return;
        }
        if (node instanceof Pane) {
            removeStyleClassIfExists((Pane) node, styleClass);
            return;
        }
        if (node instanceof Tab) {
            removeStyleClassIfExists((Tab) node, styleClass);
            return;
        }

        throw new IllegalStateException("Wrong node at #removeStyleClassIfExists: " + node + " (" + node.getClass()+ ")");
    }


    private static void addStyleClassIfNotExists(Object node, String styleClass) {
        if (node instanceof Control) {
            addStyleClassIfNotExists((Control) node, styleClass);
            return;
        }
        if (node instanceof Pane) {
            addStyleClassIfNotExists((Pane) node, styleClass);
            return;
        }
        if (node instanceof Tab) {
            addStyleClassIfNotExists((Tab) node, styleClass);
            return;
        }

        if (node instanceof PopupControl) {
            addStyleClassIfNotExists((PopupControl) node, styleClass);
        }

        throw new IllegalStateException("Wrong node at #addStyleClassIfNotExists: " + node + " (" + node.getClass()+ ")");
    }

    private static void addStyleClassIfNotExists(Control component, String styleClass) {
        if (!component.getStyleClass().contains(styleClass)) {
            component.getStyleClass().add(styleClass);
        }
    }

    private static void addStyleClassIfNotExists(Pane component, String styleClass) {
        if (!component.getStyleClass().contains(styleClass)) {
            component.getStyleClass().add(styleClass);
        }
    }

    private static void addStyleClassIfNotExists(Tab component, String styleClass) {
        if (!component.getStyleClass().contains(styleClass)) {
            component.getStyleClass().add(styleClass);
        }
    }

    private static void addStyleClassIfNotExists(PopupControl component, String styleClass) {
        if (!component.getStyleClass().contains(styleClass)) {
            component.getStyleClass().add(styleClass);
        }
    }

    public static void toggleDarkModeForPopupItem(PopupControl popup, String styleClass, boolean enabled) {
        if (popup == null) {
            return;
        }

        addOrRemoveStyle(popup, styleClass, enabled);
    }

    @SuppressWarnings("unchecked")
    private static <T> T cast(Object o) {
        return (T) o;
    }
}
