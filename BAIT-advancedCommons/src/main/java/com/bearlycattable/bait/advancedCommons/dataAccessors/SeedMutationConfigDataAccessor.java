package com.bearlycattable.bait.advancedCommons.dataAccessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.bearlycattable.bait.commons.enums.SeedMutationTypeEnum;

import javafx.util.Pair;

public class SeedMutationConfigDataAccessor {

    private final Map<SeedMutationTypeEnum, Object> dataMap; //maybe EnumMap would be ok here

    private SeedMutationConfigDataAccessor() {
        throw new IllegalStateException("Cannot construct without data map");
    }

    public SeedMutationConfigDataAccessor(@NonNull Map<SeedMutationTypeEnum, Object> dataMap) {
        this.dataMap = dataMap;
    }

    public void validate() {
        if (dataMap.keySet().stream().filter(SeedMutationTypeEnum::isIncDecGroup).count() > 1L) {
            throw new IllegalStateException("Too many options for inc/dec selected. Can only be 1");
        }

        if (dataMap.keySet().stream().filter(SeedMutationTypeEnum::isRotateHGroup).count() > 1L) {
            throw new IllegalStateException("Too many options for horizontal rotation selected. Can only be 1");
        }

        if (!dataMap.containsKey(SeedMutationTypeEnum.ROTATE_VERTICAL)) {
            return;
        }

        Object vRot = dataMap.get(SeedMutationTypeEnum.ROTATE_VERTICAL);

        if (vRot == null) {
            dataMap.remove(SeedMutationTypeEnum.ROTATE_VERTICAL);
            return;
        }

        if (vRot.getClass() != Pair.class) {
            throw new IllegalStateException("Wrong data type for vertical rotation options. Must be javafx.util.Pair");
        }

        Pair<?,?> pair = cast(vRot);
        if (!(pair.getKey() instanceof String)) {
            throw new IllegalStateException("Wrong key class in VRotationData's pair (must be String)");
        }
        if (!(pair.getValue() instanceof List)) {
            throw new IllegalStateException("Wrong value class in VRotationData's pair (must be List)");
        }
    }

    public boolean isEmpty() {
        return dataMap.isEmpty();
    }

    public List<SeedMutationTypeEnum> getMutationList() {
        List<SeedMutationTypeEnum> list = new ArrayList<>();
        getIncDecType().ifPresent(list::add);
        getHRotationType().ifPresent(list::add);
        getVRotationType().ifPresent(list::add);

        return list;
    }

    // public Pair<SeedMutationTypeEnum, String> getIncDecData() {
    //     SeedMutationTypeEnum incDecType =  dataMap.keySet().stream()
    //             .filter(SeedMutationTypeEnum::isIncDecGroup)
    //             .findAny()
    //             .orElse(null);
    //
    //     if (incDecType == null) {
    //         return null;
    //     }
    //
    //     String value = cast(dataMap.get(incDecType));
    //
    //     if (value == null || value.isEmpty()) {
    //         return null;
    //     }
    //
    //     return new Pair<>(incDecType, value);
    // }
    public Optional<Pair<SeedMutationTypeEnum, String>> getIncDecPair() {
        return getData(SeedMutationTypeEnum::isIncDecGroup);
    }

    public Optional<Pair<SeedMutationTypeEnum, String>> getHRotationPair() {
        return getData(SeedMutationTypeEnum::isRotateHGroup);
    }

    public Optional<Pair<String, List<Integer>>> getVRotationPair() {
        Optional<Pair<SeedMutationTypeEnum, Pair>> gg = getData(SeedMutationTypeEnum::isRotateVGroup);
        return gg.map(Pair::getValue);
    }

    public Optional<SeedMutationTypeEnum> getIncDecType() {
        Optional<Pair<SeedMutationTypeEnum, String>> data = getData(SeedMutationTypeEnum::isIncDecGroup);

        return data.map(Pair::getKey);
    }

    public Optional<String> getIncDecValue() {
        Optional<Pair<SeedMutationTypeEnum, String>> data = getData(SeedMutationTypeEnum::isIncDecGroup);

        return data.map(Pair::getValue);
    }

    // public Pair<SeedMutationTypeEnum, String> getHRotationData() {
    //     SeedMutationTypeEnum hRotationType =  dataMap.keySet().stream()
    //             .filter(SeedMutationTypeEnum::isRotateHGroup)
    //             .findAny()
    //             .orElse(null);
    //
    //     if (hRotationType == null) {
    //         return null;
    //     }
    //
    //     String value = cast(dataMap.get(hRotationType));
    //
    //     if (value == null || value.isEmpty()) {
    //         return null;
    //     }
    //
    //     return new Pair<>(hRotationType, value);
    // }

    public <V> Optional<Pair<SeedMutationTypeEnum, V>> getData(Predicate<SeedMutationTypeEnum> filterPredicate) {
        Optional<SeedMutationTypeEnum> type =  dataMap.keySet().stream()
                .filter(filterPredicate)
                .findAny();

        Optional<V> valueOpt = type.map(mutationType -> cast(dataMap.get(mutationType)));
        return valueOpt.map(value -> new Pair<>(type.get(), value));
    }

    public Optional<SeedMutationTypeEnum> getHRotationType() {
        Optional<Pair<SeedMutationTypeEnum, String>> data = getData(SeedMutationTypeEnum::isRotateHGroup);

        return data.map(Pair::getKey);
    }

    public Optional<String> getHRotationValue() {
        Optional<Pair<SeedMutationTypeEnum, String>> data = getData(SeedMutationTypeEnum::isRotateHGroup);

        return data.map(Pair::getValue);
    }

    // public Pair<SeedMutationTypeEnum, Pair<String, List<Integer>>> getVRotationData() {
    //     SeedMutationTypeEnum vRotationType =  dataMap.keySet().stream()
    //             .filter(SeedMutationTypeEnum::isRotateVGroup)
    //             .findAny()
    //             .orElse(null);
    //
    //     if (vRotationType == null) {
    //         return null;
    //     }
    //
    //     Pair<String, List<Integer>> valuePair = cast(dataMap.get(vRotationType));
    //
    //     if (valuePair.getKey().isEmpty() || valuePair.getValue().isEmpty()) {
    //         return null;
    //     }
    //
    //     return new Pair<>(vRotationType, valuePair);
    // }

    public Optional<SeedMutationTypeEnum> getVRotationType() {
        Optional<Pair<SeedMutationTypeEnum, Pair>> data = getData(SeedMutationTypeEnum::isRotateVGroup);

        return data.map(Pair::getKey);
    }

    public Optional<String> getVRotationValue() {
        Optional<Pair<SeedMutationTypeEnum, Pair>> data = getData(SeedMutationTypeEnum::isRotateVGroup);
        Optional<Pair> vRotationData = data.map(t -> cast(dataMap.get(t.getKey())));

        return vRotationData.map(pair -> cast(pair.getKey()));
    }

    public Optional<List<Integer>> getVRotationIndexes() {
        Optional<Pair<SeedMutationTypeEnum, Pair>> data = getData(SeedMutationTypeEnum::isRotateVGroup);
        Optional<Pair> vRotationData = data.map(t -> cast(dataMap.get(t.getKey())));

        return vRotationData.map(pair -> cast(pair.getValue()));
    }

    @SuppressWarnings("unchecked")
    private <T> T cast(Object o) {
        return (T) o;
    }
}
