package com.bearlycattable.bait.commons.enums;

import lombok.Getter;

public enum BackgroundColorEnum {

    GRAY ("bcgGray"),
    LIGHT_GRAY ("bcgLightGray"),
    DARK_GRAY ("bcgDarkGray"),
    INDIAN_RED ("bcgIndianRed"),
    BURLY_WOOD ("bcgBurlyWood"),
    LIGHT_GREEN ("bcgLightGreen"),
    LIGHT_PINK ("bcgLightPink"),
    PALE_VIOLET_RED ("bcgPaleVioletRed"),
    DARK_MODE_TEST ("bcgDarkMode01");

    @Getter
    private String styleClass;

    BackgroundColorEnum(String styleClass) {
        this.styleClass = styleClass;
    }
}
