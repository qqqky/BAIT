package com.bearlycattable.bait.advancedCommons.pubKeyComparison;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.commons.enums.JsonResultTypeEnum;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class AdvancedPubComparisonResultB {

    public static final ScaleFactorEnum DEFAULT_SCALE_FACTOR = ScaleFactorEnum.MEDIUM;
    private static final int IGNORED_HEAT_DIFFERENCE = 2;

    //points; max points depend on scale factor, eg. for "1.3" max possible points = 25200
    int pointsPosUPKH;
    int pointsNegUPKH;
    int pointsPosCPKH;
    int pointsNegCPKH;
    final ScaleFactorEnum forScaleFactor;
    final byte[] referencePKH;
    byte[] currentUPKH;
    byte[] currentCPKH;

    public int getHighestPoints() {
        return Math.max(pointsPosUPKH, Math.max(pointsNegUPKH, Math.max(pointsPosCPKH, pointsNegCPKH)));
    }

    public int getResultPointsByType(@NonNull JsonResultTypeEnum type) {
        switch (type) {
            case UHP:
                return getPointsPosUPKH();
            case UHN:
                return getPointsNegUPKH();
            case CHP:
                return getPointsPosCPKH();
            case CHN:
                return getPointsNegCPKH();
            default:
                throw new IllegalArgumentException("Type not supported at PubComparisonResultWrapper#getResultByType [type=" + type + "]");
        }
    }

    public ScaleFactorEnum getScaleFactorOrDefault() {
        return forScaleFactor == null ? DEFAULT_SCALE_FACTOR : forScaleFactor;
    }

    public void validateForBlindComparison() {
        if (referencePKH == null || referencePKH.length != 20) {
            throw new IllegalStateException("Reference PKH is not valid at #validateForBlindComparison");
        }

        if (currentUPKH == null || currentUPKH.length != 20) {
            throw new IllegalStateException("Current UPKH is not valid at #validateForBlindComparison");
        }

        if (currentCPKH == null || currentCPKH.length != 20) {
            throw new IllegalStateException("Current CPKH is not valid at #validateForBlindComparison");
        }
    }

}