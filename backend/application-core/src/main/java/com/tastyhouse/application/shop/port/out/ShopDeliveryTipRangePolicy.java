package com.tastyhouse.application.shop.port.out;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public record ShopDeliveryTipRangePolicy(
    int extraTipUpperBound,
    Map<String, Integer> unitMetersBySurchargeUnit,
    Function<String, RuntimeException> unknownSurchargeUnit,
    String distanceExtraTipType,
    String regionExtraTipType
) {

    public ShopDeliveryTipRangePolicy {
        unitMetersBySurchargeUnit = Map.copyOf(unitMetersBySurchargeUnit);
        Objects.requireNonNull(distanceExtraTipType);
        Objects.requireNonNull(regionExtraTipType);
    }

    public boolean isDistanceExtraTip(String extraTipType) {
        return distanceExtraTipType.equals(extraTipType);
    }

    public boolean isRegionExtraTip(String extraTipType) {
        return regionExtraTipType.equals(extraTipType);
    }

    public int unitMetersOf(String surchargeUnit) {
        Integer unitMeters = unitMetersBySurchargeUnit.get(surchargeUnit);
        if (unitMeters == null) {
            throw unknownSurchargeUnit.apply(surchargeUnit);
        }
        return unitMeters;
    }
}
