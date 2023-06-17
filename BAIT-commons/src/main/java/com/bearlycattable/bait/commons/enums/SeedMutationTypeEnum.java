package com.bearlycattable.bait.commons.enums;

public enum SeedMutationTypeEnum {

    INCREMENT_ABSOLUTE (MutationGroup.INC_DEC),
    INCREMENT_WORDS (MutationGroup.INC_DEC),
    DECREMENT_ABSOLUTE (MutationGroup.INC_DEC),
    DECREMENT_WORDS (MutationGroup.INC_DEC),
    ROTATE_NORMAL (MutationGroup.ROTATE_H),
    ROTATE_WORDS (MutationGroup.ROTATE_H),
    ROTATE_PREFIXED (MutationGroup.ROTATE_H),
    ROTATE_VERTICAL (MutationGroup.ROTATE_V);

    private final MutationGroup mutationGroup;

    public enum MutationGroup { INC_DEC, ROTATE_H, ROTATE_V };

    SeedMutationTypeEnum(MutationGroup mutationGroup) {
        this.mutationGroup = mutationGroup;
    }

    public MutationGroup getMutationGroup() {
        return mutationGroup;
    }

    public boolean isIncDecGroup() {
        return mutationGroup == MutationGroup.INC_DEC;
    }

    public boolean isRotateHGroup() {
        return mutationGroup == MutationGroup.ROTATE_H;
    }

    public boolean isRotateVGroup() {
        return mutationGroup == MutationGroup.ROTATE_V;
    }

}
