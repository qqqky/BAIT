package com.bearlycattable.bait.commons.enums;

public enum JsonResultScaleFactorEnum {
    SF0, //disabled (1.0)
    SF1, //medium (1.1)
    SF2, //high (1.2)
    SF3; //highest (1.3)

    public static ScaleFactorEnum toScaleFactorEnum(JsonResultScaleFactorEnum jsonEnum) {
        if (jsonEnum == null) {
            return ScaleFactorEnum.DISABLED;
        }

        switch (jsonEnum) {
            case SF0:
                return ScaleFactorEnum.DISABLED;
            case SF1:
                return ScaleFactorEnum.MEDIUM;
            case SF2:
                return ScaleFactorEnum.HIGH;
            case SF3:
                return ScaleFactorEnum.HIGHEST;
            default:
                throw new IllegalArgumentException("This enum is not supported at #toScaleFactorEnum [enum=" + jsonEnum + "]");
        }
    }
}
