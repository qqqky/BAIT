package com.bearlycattable.bait.commons.enums;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

public enum ScaleFactorEnum {
    //represents no scale factor
    DISABLED (new BigDecimal("1.0"), new BigDecimal("640")),

    MEDIUM(new BigDecimal("1.1"), new BigDecimal("2440")),
    HIGH(new BigDecimal("1.2"), new BigDecimal("8200")),
    HIGHEST(new BigDecimal("1.3"), new BigDecimal("25200"));

    private final BigDecimal scaleFactor;
    private final BigDecimal maxPoints;

    ScaleFactorEnum(BigDecimal scaleFactor, BigDecimal maxPoints) {
        this.scaleFactor = scaleFactor;
        this.maxPoints = maxPoints;
    }

    public BigDecimal getScaleFactor() {
        return scaleFactor;
    }

    public BigDecimal getMaxPoints() {
        return maxPoints;
    }

    public String getScaleFactorAsString() {
        return scaleFactor.toString();
    }

    public static Optional<ScaleFactorEnum> getByStringValue(String scaleFactorString) {
        if (scaleFactorString == null) {
            return Optional.empty();
        }

        return Arrays.stream(ScaleFactorEnum.values())
                .filter(currentEnum -> scaleFactorString.equals(currentEnum.getScaleFactorAsString()))
                .findAny();
    }

    public static Optional<ScaleFactorEnum> getByBigDecimalValue(BigDecimal scaleFactorBigDecimal) {
        if (scaleFactorBigDecimal == null) {
            return Optional.empty();
        }

        return Arrays.stream(ScaleFactorEnum.values())
                .filter(currentEnum -> scaleFactorBigDecimal.equals(currentEnum.getScaleFactor()))
                .findAny();
    }

    public static JsonResultScaleFactorEnum toJsonScaleFactorEnum(ScaleFactorEnum scaleFactor) {
        if (scaleFactor == null) {
            return JsonResultScaleFactorEnum.SF0;
        }

        switch (scaleFactor) {
            case DISABLED:
                return JsonResultScaleFactorEnum.SF0;
            case MEDIUM:
                return JsonResultScaleFactorEnum.SF1;
            case HIGH:
                return JsonResultScaleFactorEnum.SF2;
            case HIGHEST:
                return JsonResultScaleFactorEnum.SF3;
            default:
                throw new IllegalArgumentException("Scale factor [scaleFactor=" + scaleFactor + "] not supported");
        }
    }
}
