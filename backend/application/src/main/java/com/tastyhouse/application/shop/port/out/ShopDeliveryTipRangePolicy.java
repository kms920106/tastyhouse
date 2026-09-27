package com.tastyhouse.application.shop.port.out;

import java.util.Map;
import java.util.function.Function;

public record ShopDeliveryTipRangePolicy(
    int extraTipUpperBound,
    Map<String, Integer> unitMetersBySurchargeUnit,
    Function<String, RuntimeException> unknownSurchargeUnit
) {

    public ShopDeliveryTipRangePolicy {
        unitMetersBySurchargeUnit = Map.copyOf(unitMetersBySurchargeUnit);
    }

    public int unitMetersOf(String surchargeUnit) {
        Integer unitMeters = unitMetersBySurchargeUnit.get(surchargeUnit);
        if (unitMeters == null) {
            throw unknownSurchargeUnit.apply(surchargeUnit);
        }
        return unitMeters;
    }
}
