package com.bearlycattable.bait.commons.wrappers;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import com.bearlycattable.bait.commons.HeatVisualizerConstants;
import com.bearlycattable.bait.commons.enums.JsonResultTypeEnum;
import com.bearlycattable.bait.commons.enums.PubTypeEnum;
import com.bearlycattable.bait.commons.enums.ScaleFactorEnum;
import com.bearlycattable.bait.commons.other.PubComparisonResult;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PubComparisonResultWrapper {
    private PubComparisonResult resultForUncompressed;
    private PubComparisonResult resultForCompressed;
    //TODO: we can add some message here with run stats (eg: 'result found in X iterations')


    public static PubComparisonResultWrapper empty() {
        return PubComparisonResultWrapper.builder()
                .resultForUncompressed(PubComparisonResult.empty(PubTypeEnum.UNCOMPRESSED))
                .resultForCompressed(PubComparisonResult.empty(PubTypeEnum.COMPRESSED))
                .build();
    }

    public Stream<PubComparisonResult> resultStream() {
        return Stream.of(resultForUncompressed, resultForCompressed);
    }

    public boolean equalsEmpty() {
        return resultStream().anyMatch(currentResult -> HeatVisualizerConstants.EMPTY_RESULT_WRAPPER.resultStream()
                .anyMatch(emptyItem -> emptyItem.equals(currentResult)));
    }

    public boolean isBothPrivsValidAndNonNull() {
        return resultStream().allMatch(Objects::nonNull) && isBothPrivsIdentical();
    }

    public boolean isBothScaleFactorsValidAndNonNull() {
        return resultStream().allMatch(Objects::nonNull) && isBothScaleFactorsSame();
    }

    public boolean isBothPrivsEmpty() {
        return resultStream().allMatch(result -> result != null && result.getForPriv().isEmpty());
    }

    private boolean isBothPrivsIdentical() {
        return resultForUncompressed.getForPriv() != null &&
               resultForCompressed.getForPriv() != null &&
               resultForUncompressed.getForPriv().equals(resultForCompressed.getForPriv());

    }

    private boolean isBothScaleFactorsSame() {
        return resultForUncompressed.getForScaleFactor() != null &&
               resultForCompressed.getForScaleFactor() != null &&
               resultForUncompressed.getForScaleFactor() == resultForCompressed.getForScaleFactor();
    }

    private boolean isBothPrivsNull() {
        return resultStream().allMatch(result -> result.getForPriv() == null);
    }

    public int getHighestPointsUncomp() {
        return resultForUncompressed.getHighest();
    }

    public int getHighestPointsComp() {
        return resultForCompressed.getHighest();
    }

    public int getUHP() {
        return resultForUncompressed.getPositive();
    }

    public int getUHN() {
        return resultForUncompressed.getNegative();
    }

    public int getCHP() {
        return resultForCompressed.getPositive();
    }

    public int getCHN() {
        return resultForCompressed.getNegative();
    }

    public int getResultByType(JsonResultTypeEnum type) {
        switch (type) {
            case UHP:
                return getUHP();
            case UHN:
                return getUHN();
            case CHP:
                return getCHP();
            case CHN:
                return getCHN();
            default:
                throw new IllegalArgumentException("Type not supported at PubComparisonResultWrapper#getResultByType [type="+type+"]");
        }
    }

    public int getHighestPoints() {
        return Math.max(getHighestPointsUncomp(), getHighestPointsComp());
    }

    public boolean isHigherResultThan(PubComparisonResultWrapper another) {
        return another != null && this.getHighestPoints() > another.getHighestPoints();
    }

    public String getPrivIfValidData() {
        if (isBothPrivsValidAndNonNull()) {
            return resultForUncompressed.getForPriv();
        }
        throw new IllegalStateException("Matching privs do not exist or are not valid at PubComparisonResultWrapper#getPrivIfValidData");
    }

    public Optional<PubComparisonResult> getResultAsOptionalForUncompressed() {
        return Optional.ofNullable(resultForUncompressed);
    }

    public Optional<PubComparisonResult> getResultAsOptionalForCompressed() {
        return Optional.ofNullable(resultForCompressed);
    }

    public String getCommonPriv() {
        if (!isBothPrivsValidAndNonNull()) {
            throw new IllegalStateException("Privs do not exist or do not match at #getCommonPriv. Use a different method");
        }
        return resultForUncompressed.getForPriv();
    }

    public ScaleFactorEnum getCommonScaleFactor() {
        if (!isBothScaleFactorsValidAndNonNull()) {
            throw new IllegalStateException("Scale factors do not exist or do not match at #getCommonScaleFactor. Use a different method");
        }

        return resultForUncompressed.getForScaleFactor();
    }

}
