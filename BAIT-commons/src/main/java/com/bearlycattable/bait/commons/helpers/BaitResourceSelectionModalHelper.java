package com.bearlycattable.bait.commons.helpers;

import java.io.File;
import java.util.Optional;

import org.checkerframework.checker.nullness.qual.NonNull;

import javafx.scene.control.Control;
import javafx.stage.FileChooser;

public class BaitResourceSelectionModalHelper {

    public static synchronized Optional<String> selectJsonResourceForOpen(@NonNull String title, @NonNull Control component) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File selectedFile = fileChooser.showOpenDialog(component.getScene().getWindow());
        return selectedFile != null ? Optional.of(selectedFile.getAbsolutePath()) : Optional.empty();
    }

    public static synchronized Optional<String> selectTxtResourceForOpen(@NonNull String title, @NonNull Control component) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        File selectedFile = fileChooser.showOpenDialog(component.getScene().getWindow());
        return selectedFile != null ? Optional.of(selectedFile.getAbsolutePath()) : Optional.empty();
    }

    public static synchronized Optional<String> selectJsonResourceForSave(@NonNull String title, @NonNull Control component) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File selectedFile = fileChooser.showSaveDialog(component.getScene().getWindow());
        return selectedFile != null ? Optional.of(selectedFile.getAbsolutePath()) : Optional.empty();
    }

    public static synchronized Optional<String> selectTxtResourceForSave(@NonNull String title, @NonNull Control component) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        File selectedFile = fileChooser.showSaveDialog(component.getScene().getWindow());
        return selectedFile != null ? Optional.of(selectedFile.getAbsolutePath()) : Optional.empty();
    }
}
