package com.bearlycattable.bait.utility.logUtils;

import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
public abstract class LogText implements LogTextType {

    String text;
    Color color;
    FontWeight weight; //normal or bold
    int size;
}
