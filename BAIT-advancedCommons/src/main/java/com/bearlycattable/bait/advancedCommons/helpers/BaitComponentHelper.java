package com.bearlycattable.bait.advancedCommons.helpers;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class BaitComponentHelper {

    private static final String specialLabelTooltipNormal = "specialLabelTooltipNormal";
    private static final String specialLabelTooltipDark = "specialLabelTooltipDark";

    public void initializeComponentMappingsForPubs(HBox parentComponent, Map<Integer, Label> mappingCollection, int startIndex) {
        if (mappingCollection == null || startIndex < 0) {
            return;
        }

        mappingCollection.clear();

        List<HBox> directHboxChildren = extractDirectChildContainersOfType(parentComponent, HBox.class);
        List<Label> allNestedLabels = extractDirectChildContainersOfTypeFromList(directHboxChildren, Label.class);

        for (int i = startIndex; startIndex != 0 ? i <= allNestedLabels.size() : i < allNestedLabels.size(); i++) {
            mappingCollection.put(i, allNestedLabels.get(i));
        }
    }

    public <T extends Pane, U extends Pane> List<U> extractDirectChildContainersOfType(T parent, Class<U> targetContainerClass) {
        return parent.getChildren().stream()
                .map(childNode -> targetContainerClass.isInstance(childNode) ? childNode : null)
                .filter(Objects::nonNull)
                .map(targetContainerClass::cast)
                .collect(Collectors.toList());
    }

    public <T extends Pane, U extends Control> List<U> extractDirectChildControlObjectsOfType(T parent, Class<U> targetContainerClass) {
        return parent.getChildren().stream()
                .map(childNode -> targetContainerClass.isInstance(childNode) ? childNode : null)
                .filter(Objects::nonNull)
                .map(targetContainerClass::cast)
                .collect(Collectors.toList());
    }

    public <T extends Pane, U extends Control> List<U> extractDirectChildContainersOfTypeFromList(List<T> parentContainersCollection, Class<U> targetContainerClass) {
        return parentContainersCollection.stream()
                .flatMap(hbox -> hbox.getChildren()
                        .stream()
                        .map(node -> targetContainerClass.isInstance(node) ? node : null)
                        .filter(Objects::nonNull))
                .map(targetContainerClass::cast)
                .collect(Collectors.toList());
    }

    public <T extends Pane> void putComponentListResultsToMap(List<T> sourceList, Map<Integer, T> targetMap, int startIndex) {
        for (int i = startIndex; i < sourceList.size() + startIndex; i++) {
            targetMap.put(i, sourceList.get(i - startIndex));
        }
    }

    public <T extends Control> void putControlObjectListResultsToMap(List<T> sourceList, Map<Integer, T> targetMap, int startIndex) {
        for (int i = startIndex; i < sourceList.size() + startIndex; i++) {
            targetMap.put(i, sourceList.get(i - startIndex));
        }
    }

    public static HBox createEmptyHBoxSpacer(int width, boolean forcedWidth) {
        HBox hbox = new HBox();

        if (forcedWidth) {
            hbox.setMinWidth(width);
        }

        hbox.setPrefWidth(width);
        return hbox;
    }

    public static VBox createEmptyVBoxSpacer(int height, boolean forcedHeight) {
        VBox vbox = new VBox();

        if (forcedHeight) {
            vbox.setMinHeight(height);
        }

        vbox.setPrefHeight(height);
        return vbox;
    }

    public static HBox createHBoxWithLengthLabel(String id) {
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);

        Label start = new Label("[");
        Label main = new Label("0");
        main.setId(id);
        Label end = new Label("]");

        hbox.getChildren().add(start);
        hbox.getChildren().add(main);
        hbox.getChildren().add(end);

        return hbox;
    }

    public static Label createLabel(String text, int fontSize, boolean wrapText) {
        Label label = new Label();
        label.setStyle("-fx-font-size:" + fontSize + ";");
        label.setMinWidth(30);
        label.setWrapText(wrapText);
        label.setText(text);
        return label;
    }

    public static Label createPrettyLabelWithTooltip(String text) {
        Label tooltipLabel = new Label();
        tooltipLabel.setAlignment(Pos.CENTER);
        tooltipLabel.setMinWidth(25.0);
        tooltipLabel.setMinHeight(25.0);
        tooltipLabel.setText("?");
        tooltipLabel.getStyleClass().add(specialLabelTooltipNormal);
        Tooltip tooltip = new Tooltip();
        tooltip.setText(text);
        tooltip.setWrapText(true);
        tooltipLabel.setTooltip(tooltip);
        return tooltipLabel;
    }

}
