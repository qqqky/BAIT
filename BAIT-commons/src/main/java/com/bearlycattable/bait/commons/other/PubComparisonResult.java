package com.bearlycattable.bait.commons.other;

import java.util.Arrays;
import java.util.List;

import com.bearlycattable.bait.commons.enums.PubTypeEnum;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;

import lombok.Builder;

@Builder
public class PubComparisonResult {

    int positive; //points; max points depends on scale factor, eg. for "1.3" max possible points = 25200
    int negative;
    String forPriv;
    ScaleFactorEnum forScaleFactor;
    PubTypeEnum type;

    public int getPositive() {
        return positive;
    }

    public int getNegative() {
        return negative;
    }

    public String getForPriv() {
        return forPriv;
    }

    public void setForPriv(String priv) {
        this.forPriv = priv;
    }

    public ScaleFactorEnum getForScaleFactor() {
        return forScaleFactor;
    }

    public void setForScaleFactor(ScaleFactorEnum scaleFactor) {
        this.forScaleFactor = scaleFactor;
    }

    public PubTypeEnum getType() {
        return type;
    }

    public List<Integer> getValueList() {
        return Arrays.asList(positive, negative);
    }

    public int getHighest() {
        if (positive >= negative) {
            return positive;
        }
        return negative;
    }

    public int getLowest() {
        if (positive <= negative) {
            return positive;
        }
        return negative;
    }

    public static PubComparisonResult empty(PubTypeEnum type) {
        return PubComparisonResult.builder()
                .positive(0)
                .negative(0)
                .forPriv("0000")
                .type(type)
                .build();
    }

    @Override
    public boolean equals(Object another) {
        if (this == another) {
            return true;
        }

        if (!(another instanceof PubComparisonResult)) {
            return false;
        }

        PubComparisonResult obj = (PubComparisonResult) another;

        return this.type == obj.getType()
                && (this.forPriv != null && obj.getForPriv() != null)
                && this.forPriv.equals(obj.getForPriv())
                && this.forScaleFactor == obj.getForScaleFactor()
                && this.positive == obj.getPositive()
                && this.negative == obj.getNegative();
    }

    @Override
    public int hashCode() {
        final int prime = 7;
        int result = 1;
        result = prime * (result
                + (type == null ? 0 : type.hashCode())
                + (forPriv == null ? 0 : forPriv.hashCode())
                + (forScaleFactor == null ? 0 : forScaleFactor.hashCode())
                + positive
                + negative);

        return result;
    }
}
