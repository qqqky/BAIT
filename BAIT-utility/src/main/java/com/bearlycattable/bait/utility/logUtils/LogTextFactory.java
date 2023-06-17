package com.bearlycattable.bait.utility.logUtils;

import com.bearlycattable.bait.commons.enums.LogTextTypeEnum;

import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;

public class LogTextFactory {

    private static final int DEFAULT_LOG_TEXT_SIZE = 16;

    public static LogText build(String textPiece, Color color, int size, FontWeight weight, LogTextTypeEnum type) {

        LogText logText;

        switch (type) {
            case START_OF_SEARCH:
                logText =  LogTextStartOfSearch.builder().build();
                break;
            case END_OF_SEARCH:
                logText = LogTextEndOfSearch.builder().build();
                break;
            case POINTS_GAINED:
                logText = LogTextPointsGained.builder().build();
                break;
            case KEY_SWAP:
                logText = LogTextKeySwap.builder().build();
                break;
            case SEARCH_PROGRESS:
                logText = LogTextSearchProgress.builder().build();
                break;
            case GENERAL:
                logText = LogTextGeneral.builder().build();
                break;
            case LOG_CLEAR:
                logText = LogTextLogClear.builder().build();
                break;
            default:
                throw new IllegalArgumentException("Type is not supported at LogTextFactory#build [type=" + type + "]");
        }

        logText.setText(textPiece);
        logText.setColor(color);
        logText.setWeight(weight != null ? weight : FontWeight.NORMAL);
        logText.setSize(size > 0 ? size : DEFAULT_LOG_TEXT_SIZE);

        return logText;
    }
}
