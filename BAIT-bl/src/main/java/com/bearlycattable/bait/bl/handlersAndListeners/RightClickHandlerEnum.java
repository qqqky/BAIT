package com.bearlycattable.bait.bl.handlersAndListeners;

import java.util.HashMap;
import java.util.Map;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

public enum RightClickHandlerEnum implements EventHandler<MouseEvent> {
    INSTANCE;

    private static final ContextMenu contextMenu = new ContextMenu();
    private static final String contextMenuDark = "contextMenuDark";
    private static final String menuItemDark = "menuItemDark";

    @Override
    public void handle(MouseEvent event) {
        if (MouseButton.SECONDARY == event.getButton()) {
            contextMenu.hide();
            contextMenu.getItems().clear();

            String text = retrieveImportantTextFromEvent(event);
            boolean darkModeEnabled = retrieveDarkModeInfoFromEvent(event);
            addMenuItemWithActionEvent("Copy text: " + text , text, darkModeEnabled);

            if (!darkModeEnabled) {
                contextMenu.getStyleClass().remove(contextMenuDark);
            }

            if (!contextMenu.getStyleClass().contains(contextMenuDark)) {
                contextMenu.getStyleClass().add(contextMenuDark);
            }

            //we pass null to make the contextMenu centered properly on the event's X/Y coords
            contextMenu.show((Node) event.getTarget(), null, event.getX(), event.getY());
            return;
        }
        contextMenu.hide();
        contextMenu.getItems().clear();
    }

    private void addMenuItemWithActionEvent(String name, String bareValue, boolean darkModeEnabled) {
        MenuItem menuItem = new MenuItem(name);
        if (darkModeEnabled) {
            menuItem.getStyleClass().add(menuItemDark);
        }

        menuItem.setOnAction(buildMenuActionCopy(bareValue));
        contextMenu.getItems().add(menuItem);
    }

    private EventHandler<ActionEvent> buildMenuActionCopy(String text) {
        return event -> {
            if (text == null) {
                return;
            }

            Map<DataFormat, Object> content = new HashMap<>();
            content.put(DataFormat.PLAIN_TEXT, text);
            Clipboard.getSystemClipboard().setContent(content);
            // System.out.println("The following text has been copied to clipboard: " + content.get(DataFormat.PLAIN_TEXT));
        };
    }

    private String retrieveImportantTextFromEvent(MouseEvent event) {
        if (event.getTarget() instanceof ListCell) {
            return ((ListCell<?>) event.getTarget()).getText();
        // } else if (event.getTarget() instanceof LabeledText) {
        //     return  ((LabeledText) event.getTarget()).getText();
        } else if (event.getTarget() instanceof Text) {
            return ((Text) event.getTarget()).getText();
        } else {
            return null;
        }
    }

    private boolean retrieveDarkModeInfoFromEvent(MouseEvent event) {
        Object data = false;
        if (event.getTarget() instanceof ListCell) {
            data = ((ListCell<?>) event.getTarget()).getUserData();
        // } else if (event.getTarget() instanceof LabeledText) {
        //     data = ((LabeledText) event.getTarget()).getParent().getUserData();
        } else if (event.getTarget() instanceof Text) {
            data = ((Text) event.getTarget()).getUserData();
        }


        if (data instanceof Boolean) {
            return (boolean) data;
        }

        return false;
    }
}
