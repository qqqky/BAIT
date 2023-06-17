package com.bearlycattable.bait.commons.enums;

import lombok.Getter;

public enum TextColorEnum {

    GREEN ("textGreen"),
    YELLOW ("textYellow"),
    DARK_ORANGE ("textDarkOrange"),
    RED ("textRed"),
    DARK_MODE_01 ("textDarkMode01"),
    LIGHT_GRAY ("textLightGray"),
    DARK_GRAY ("textDarkGray"),
    WHEAT ("textWheat");

    @Getter
    private final String styleClass;

    TextColorEnum(String styleClass) {
        this.styleClass = styleClass;
    }
}
